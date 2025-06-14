package ru.yandex.practicum.eshop.payment.service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;
import ru.yandex.practicum.eshop.core.dto.CartDto;
import ru.yandex.practicum.eshop.core.dto.ItemDto;
import ru.yandex.practicum.eshop.core.dto.OrderDto;
import ru.yandex.practicum.eshop.core.entity.Cart;
import ru.yandex.practicum.eshop.core.entity.CartItem;
import ru.yandex.practicum.eshop.core.entity.Item;
import ru.yandex.practicum.eshop.core.entity.OrderItem;
import ru.yandex.practicum.eshop.core.entity.Orders;
import ru.yandex.practicum.eshop.core.enums.Action;
import ru.yandex.practicum.eshop.core.enums.Sorting;
import ru.yandex.practicum.eshop.core.exceptions.ActionException;
import ru.yandex.practicum.eshop.core.exceptions.DataBaseRequestException;
import ru.yandex.practicum.eshop.core.exceptions.ItemNotFoundException;
import ru.yandex.practicum.eshop.core.exceptions.SortingException;
import ru.yandex.practicum.eshop.core.mappers.ItemMapper;
import ru.yandex.practicum.eshop.core.repository.CartItemRepository;
import ru.yandex.practicum.eshop.core.repository.CartRepository;
import ru.yandex.practicum.eshop.core.repository.ItemRepository;
import ru.yandex.practicum.eshop.core.repository.OrderItemRepository;
import ru.yandex.practicum.eshop.core.repository.OrderRepository;

import static ru.yandex.practicum.eshop.core.enums.MessagesLog.MESSAGE_LOG_DB_GET_REQUEST;
import static ru.yandex.practicum.eshop.core.enums.MessagesLog.MESSAGE_LOG_DB_RESPONSE_ERROR;
import static ru.yandex.practicum.eshop.core.enums.MessagesLog.MESSAGE_LOG_DB_SAVE_REQUEST;
import static ru.yandex.practicum.eshop.core.enums.MessagesLog.MESSAGE_LOG_FLUSH_CART;
import static ru.yandex.practicum.eshop.core.enums.MessagesLog.MESSAGE_LOG_FLUSH_CART_SUCCESS;
import static ru.yandex.practicum.eshop.core.enums.MessagesLog.MESSAGE_LOG_ITEMS_SIZE;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
  private static final Long CART_ID = 1L;
  private static final Double TOTAL_INIT = 0.00;
  private final ItemRepository itemRepository;
  private final CartRepository cartRepository;
  private final OrderRepository orderRepository;
  private final CartItemRepository cartItemRepository;
  private final OrderItemRepository orderItemRepository;

  @Override
  public Mono<Long> buyItems() {
    return Mono.defer(() -> {
      log.info(MESSAGE_LOG_DB_SAVE_REQUEST.getMessage());

      return cartRepository.findById(CART_ID)
                           .flatMap(this::fetchAndProcessCartItems)
                           .flatMap(this::createAndSaveOrder)
                           .flatMap(this::saveOrderItems)
                           .flatMap(this::flushItemAndCart)
                           .map(Orders::getId)
                           .onErrorResume(e -> {
                             log.error(MESSAGE_LOG_DB_RESPONSE_ERROR.getMessage(), e);
                             return Mono.error(new DataBaseRequestException(
                                 MESSAGE_LOG_DB_RESPONSE_ERROR.getMessage(), e));
                           });
    });
  }

  private Mono<Orders> flushItemAndCart(Orders orders) {
    return Mono.defer(() -> {
      log.info(MESSAGE_LOG_FLUSH_CART.getMessage());

      Cart cart = new Cart(CART_ID, TOTAL_INIT);
      return cartRepository.save(cart)
                           .then(cartItemRepository.deleteAllByCartId(CART_ID))
                           .then(itemRepository.updateAllCountToZero())
                           .doOnSuccess(v -> log.info(MESSAGE_LOG_FLUSH_CART_SUCCESS.getMessage()))
                           .thenReturn(orders)
                           .onErrorResume(e -> {
                             log.error(MESSAGE_LOG_DB_RESPONSE_ERROR.getMessage(), e);
                             return Mono.error(new DataBaseRequestException(
                                 MESSAGE_LOG_DB_RESPONSE_ERROR.getMessage(), e));
                           });
    });
  }

  private Mono<Tuple2<Cart, List<CartItem>>> fetchAndProcessCartItems(Cart cart) {
    return cartItemRepository.findCartItemsByCartId(cart.getId())
                             .collectList()
                             .map(items -> Tuples.of(cart, items))
                             .onErrorResume(e -> {
                               log.error(MESSAGE_LOG_DB_RESPONSE_ERROR.getMessage(), e);
                               return Mono.error(new DataBaseRequestException(
                                   MESSAGE_LOG_DB_RESPONSE_ERROR.getMessage(), e));
                             });
  }

  private Mono<Tuple2<Orders, List<CartItem>>> createAndSaveOrder(
      Tuple2<Cart, List<CartItem>> cartAndItems) {
    Cart cart = cartAndItems.getT1();
    List<CartItem> cartItems = cartAndItems.getT2();

    Orders orders = Orders.builder()
                          .totalSum(cart.getTotal())
                          .build();

    return orderRepository.save(orders)
                          .map(ordersSaved -> Tuples.of(ordersSaved, cartItems));
  }

  private Mono<Orders> saveOrderItems(Tuple2<Orders, List<CartItem>> orderAndCartItems) {
    Orders orders = orderAndCartItems.getT1();
    List<CartItem> cartItems = orderAndCartItems.getT2();

    return Flux.fromIterable(cartItems)
               .flatMap(cartItem -> itemRepository.findById(cartItem.getItemId())
                                                  .map(item -> createOrderItem(orders, item,
                                                                               cartItem.getCount())))
               .collectList()
               .flatMap(orderItems -> orderItemRepository.saveAll(orderItems)
                                                         .collectList()
                                                         .thenReturn(orders))
               .onErrorResume(e -> {
                 log.error(MESSAGE_LOG_DB_RESPONSE_ERROR.getMessage(), e);
                 return Mono.error(new DataBaseRequestException(
                     MESSAGE_LOG_DB_RESPONSE_ERROR.getMessage(), e));
               });
  }

  private OrderItem createOrderItem(Orders orders, Item item, int count) {
    return OrderItem.builder()
                    .orderId(orders.getId())
                    .itemId(item.getId())
                    .count(count)
                    .build();
  }
}