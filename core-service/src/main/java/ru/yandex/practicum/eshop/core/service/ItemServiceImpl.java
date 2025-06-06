package ru.yandex.practicum.eshop.core.service;

import ru.yandex.practicum.eshop.core.entity.Orders;
import ru.yandex.practicum.eshop.core.enums.Action;
import ru.yandex.practicum.eshop.core.exceptions.ActionException;
import ru.yandex.practicum.eshop.core.exceptions.DataBaseRequestException;
import ru.yandex.practicum.eshop.core.exceptions.SortingException;
import ru.yandex.practicum.eshop.core.mappers.ItemMapper;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import ru.yandex.practicum.eshop.core.dto.CartDto;
import ru.yandex.practicum.eshop.core.dto.ItemDto;
import ru.yandex.practicum.eshop.core.dto.OrderDto;
import ru.yandex.practicum.eshop.core.entity.Cart;
import ru.yandex.practicum.eshop.core.entity.CartItem;
import ru.yandex.practicum.eshop.core.entity.Item;
import ru.yandex.practicum.eshop.core.entity.OrderItem;
import ru.yandex.practicum.eshop.core.enums.Sorting;
import ru.yandex.practicum.eshop.core.exceptions.ItemNotFoundException;
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
public class ItemServiceImpl implements ItemService {
  private static final Long CART_ID = 1L;
  private static final Double TOTAL_INIT = 0.00;
  private final ItemMapper itemMapper;
  private final ItemRepository itemRepository;
  private final CartRepository cartRepository;
  private final OrderRepository orderRepository;
  private final CartItemRepository cartItemRepository;
  private final OrderItemRepository orderItemRepository;

  @Override
  public Mono<PageImpl<ItemDto>> getItems(String search, Sorting sort, int pageNumber,
                                          int pageSize) {
    Pageable pageableItems = getPageableItemsRequest(sort, pageNumber, pageSize);
    log.info(MESSAGE_LOG_DB_GET_REQUEST.getMessage());

    if (search.isEmpty()) {
      return itemRepository.findAll()
                           .collectList()
                           .flatMap(items -> {
                             long total = items.size();
                             return Mono.just(
                                 new PageImpl<>(itemMapper.toListDto(items),
                                                pageableItems,
                                                total));
                           })
                           .onErrorResume(e -> {
                             log.error(MESSAGE_LOG_DB_RESPONSE_ERROR.getMessage(), e);
                             return Mono.error(new DataBaseRequestException(
                                 MESSAGE_LOG_DB_RESPONSE_ERROR.getMessage(), e));
                           });

    } else {
      return itemRepository.findByTitleContainingIgnoreCase(search, pageableItems)
                           .skip((long) pageNumber * pageSize)
                           .limitRate(pageSize)
                           .collectList()
                           .flatMap(items -> {
                             long total = items.size();
                             return Mono.just(
                                 new PageImpl<>(itemMapper.toListDto(items),
                                                pageableItems,
                                                total));
                           })
                           .onErrorResume(e -> {
                             log.error(MESSAGE_LOG_DB_RESPONSE_ERROR.getMessage(), e);
                             return Mono.error(new DataBaseRequestException(
                                 MESSAGE_LOG_DB_RESPONSE_ERROR.getMessage(), e));
                           });
    }
  }

  @Override
  public Mono<Void> editCart(Long itemId, String actionRequest) {
    Action action = Action.getValueOf(actionRequest);

    return cartRepository.findById(CART_ID)
                         .flatMap(existingCart -> handleCartItem(existingCart, itemId, action))
                         .onErrorResume(this::handleDatabaseError);
  }

  @Override
  public Mono<CartDto> getCartItems() {
    return Mono.defer(() -> {
      log.info(MESSAGE_LOG_DB_GET_REQUEST.getMessage());

      return getItemsFromCart()
          .map(items -> {
            log.info(MESSAGE_LOG_ITEMS_SIZE.getMessage(), items.size());
            return items;
          })
          .map(items -> {
            double total = items.stream()
                                .mapToDouble(item -> item.getPrice() * item.getCount())
                                .sum();

            return CartDto.builder()
                          .id(CART_ID)
                          .items(itemMapper.toListDto(items))
                          .total(total)
                          .build();
          })
          .onErrorResume(e -> {
            log.error(MESSAGE_LOG_DB_RESPONSE_ERROR.getMessage(), e);
            return Mono.error(
                new DataBaseRequestException(MESSAGE_LOG_DB_RESPONSE_ERROR.getMessage(), e));
          });
    });
  }

  @Override
  public Mono<ItemDto> getItem(Long id) {
    return Mono.defer(() -> {
      log.info(MESSAGE_LOG_DB_GET_REQUEST.getMessage());

      return itemRepository.findById(id)
                           .map(itemMapper::toDto)
                           .switchIfEmpty(Mono.error(new ItemNotFoundException("Товар не найден")))
                           .onErrorResume(e -> {
                             log.error(MESSAGE_LOG_DB_RESPONSE_ERROR.getMessage(), e);
                             return Mono.error(new DataBaseRequestException(
                                 MESSAGE_LOG_DB_RESPONSE_ERROR.getMessage(), e));
                           });
    });
  }

  @Override
  public Mono<Long> buyItems() {
    return Mono.defer(() -> {
      log.info(MESSAGE_LOG_DB_SAVE_REQUEST.getMessage());

      return cartRepository.findById(CART_ID)
                           .flatMap(this::fetchAndProcessCartItems)
                           .flatMap(this::createAndSaveOrder)
                           .flatMap(this::saveOrderItems)
                           .flatMap(this::flushItemAndCart) // Изменен метод
                           .map(Orders::getId)
                           .onErrorResume(e -> {
                             log.error(MESSAGE_LOG_DB_RESPONSE_ERROR.getMessage(), e);
                             return Mono.error(new DataBaseRequestException(
                                 MESSAGE_LOG_DB_RESPONSE_ERROR.getMessage(), e));
                           });
    });
  }

  @Override
  public Flux<OrderDto> getOrders() {
    return orderRepository.findAll()
                          .flatMap(orders -> getOrderItems(orders.getId()));
  }

  @Override
  public Mono<OrderDto> getOrderItems(Long id) {
    return getItemsCountFromOrderItems(id)
        .flatMapMany(this::getItemsFromOrder)
        .collectList()
        .map(items -> {
          double total = items.stream()
                              .mapToDouble(item -> item.getPrice() * item.getCount())
                              .sum();

          return OrderDto.builder()
                         .id(id)
                         .items(itemMapper.toListDto(items))
                         .totalSum(total)
                         .build();
        });
  }

  private Mono<Void> handleCartItem(Cart existingCart, Long itemId, Action action) {
    return switch (action) {
      case PLUS -> incrementItem(itemId, existingCart);
      case MINUS -> decrementItem(itemId, existingCart);
      case DELETE -> cartItemRepository.findCartItemByCartIdAndItemId(CART_ID, itemId)
                                       .map(Optional::ofNullable)
                                       .switchIfEmpty(Mono.error(
                                           new ActionException("Товар не найден в корзине")))
                                       .flatMap(optionalCartItem -> optionalCartItem.map(
                                                                                        cartItemRepository::delete)
                                                                                    .orElse(
                                                                                        Mono.error(
                                                                                            new ActionException(
                                                                                                "Товар не найден в корзине"))));
    };
  }

  private Mono<Void> handleDatabaseError(Throwable e) {
    return Mono.error(new DataBaseRequestException(
        MESSAGE_LOG_DB_RESPONSE_ERROR.getMessage(),
        e));
  }

  private Flux<Item> getItemsFromOrder(Map<Long, Integer> orderItemCounts) {
    return itemRepository.findAllById(orderItemCounts.keySet())
                         .map(item -> {
                           Integer countFromOrder = orderItemCounts.get(item.getId());
                           if (countFromOrder != null) {
                             item.setCount(countFromOrder);
                           }
                           return item;
                         });
  }

  private Mono<Map<Long, Integer>> getItemsCountFromOrderItems(Long id) {
    return orderItemRepository.findOrderItemsByOrderId(id)
                              .collectMap(OrderItem::getItemId, OrderItem::getCount);
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


  private static Pageable getPageableItemsRequest(Sorting sort, int pageNumber, int pageSize) {
    return switch (sort) {
      case ALPHA -> PageRequest.of(pageNumber, pageSize, Sort.by("title").ascending());
      case PRICE -> PageRequest.of(pageNumber, pageSize, Sort.by("price").ascending());
      case NO -> PageRequest.of(pageNumber, pageSize);
      default -> throw new SortingException("Некорректный тип сортировки: " + sort);
    };
  }

  private Mono<Void> incrementItem(Long itemId, Cart existingCart) {
    return cartItemRepository.findCartItemByCartIdAndItemId(CART_ID, itemId)
                             .map(Optional::ofNullable)
                             .switchIfEmpty(
                                 cartItemRepository.save(
                                     CartItem.builder()
                                             .cartId(CART_ID)
                                             .itemId(itemId)
                                             .count(1)
                                             .build()
                                 ).singleOptional()
                             )
                             .flatMap(optionalCartItem -> optionalCartItem.map(cartItem -> {
                               cartItem.setCount(cartItem.getCount() + 1);
                               return cartItemRepository.save(cartItem);
                             }).orElseGet(Mono::empty))
                             .then(itemRepository.incrementCount(itemId))
                             .then(updateCartTotal(existingCart))
                             .onErrorResume(e -> {
                               if (e instanceof Exception) {
                                 return Mono.error(
                                     new DataBaseRequestException("Ошибка доступа к базе данных",
                                                                  e));
                               } else {
                                 return Mono.error(new DataBaseRequestException(
                                     "Произошла ошибка при обновлении корзины", e));
                               }
                             });
  }

  private Mono<Void> decrementItem(Long itemId, Cart existingCart) {
    return cartItemRepository.findCartItemByCartIdAndItemId(CART_ID, itemId)
                             .map(Optional::ofNullable)
                             .switchIfEmpty(
                                 Mono.error(new ActionException("Товар не найден в корзине")))
                             .flatMap(optionalCartItem -> optionalCartItem.map(cartItem -> {
                               if (cartItem.getCount() > 1) {
                                 cartItem.setCount(cartItem.getCount() - 1);
                                 return cartItemRepository.save(cartItem)
                                                          .then(itemRepository.decrementCount(
                                                              itemId));
                               } else {
                                 return cartItemRepository.delete(cartItem);
                               }
                             }).orElse(
                                 Mono.error(new ActionException("Товар не найден в корзине"))))
                             .then(updateCartTotal(existingCart));
  }

  private Mono<Void> updateCartTotal(Cart existingCart) {
    return calculateTotal()
        .doOnNext(existingCart::setTotal)
        .then(cartRepository.save(existingCart))
        .then();
  }

  private Mono<Double> calculateTotal() {
    return cartItemRepository.findCartItemsByCartId(CART_ID)
                             .flatMap(cartItem -> itemRepository.findById(cartItem.getItemId())
                                                                .map(item -> item.getPrice()
                                                                             * cartItem.getCount()))
                             .reduce(0.0, Double::sum);
  }

  private Mono<List<Item>> getItemsFromCart() {
    return cartItemRepository.findCartItemsByCartId(CART_ID)
                             .map(CartItem::getItemId)
                             .collectList()
                             .flatMap(ids -> itemRepository.findAllById(ids)
                                                           .collectList());
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