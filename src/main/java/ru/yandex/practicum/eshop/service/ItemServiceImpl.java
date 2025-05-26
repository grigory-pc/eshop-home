package ru.yandex.practicum.eshop.service;

import ch.qos.logback.core.joran.spi.ActionException;
import java.util.ArrayList;
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
import ru.yandex.practicum.eshop.dto.CartDto;
import ru.yandex.practicum.eshop.dto.ItemDto;
import ru.yandex.practicum.eshop.dto.OrderDto;
import ru.yandex.practicum.eshop.entity.Cart;
import ru.yandex.practicum.eshop.entity.CartItem;
import ru.yandex.practicum.eshop.entity.Item;
import ru.yandex.practicum.eshop.entity.Order;
import ru.yandex.practicum.eshop.entity.OrderItem;
import ru.yandex.practicum.eshop.enums.Action;
import ru.yandex.practicum.eshop.enums.Sorting;
import ru.yandex.practicum.eshop.exceptions.DataBaseRequestException;
import ru.yandex.practicum.eshop.exceptions.ItemNotFoundException;
import ru.yandex.practicum.eshop.exceptions.SortingException;
import ru.yandex.practicum.eshop.mappers.ItemMapper;
import ru.yandex.practicum.eshop.repository.CartItemRepository;
import ru.yandex.practicum.eshop.repository.CartRepository;
import ru.yandex.practicum.eshop.repository.ItemRepository;
import ru.yandex.practicum.eshop.repository.OrderItemRepository;
import ru.yandex.practicum.eshop.repository.OrderRepository;

import static ru.yandex.practicum.eshop.enums.MessagesLog.MESSAGE_LOG_DB_GET_REQUEST;
import static ru.yandex.practicum.eshop.enums.MessagesLog.MESSAGE_LOG_DB_RESPONSE_ERROR;
import static ru.yandex.practicum.eshop.enums.MessagesLog.MESSAGE_LOG_DB_SAVE_REQUEST;
import static ru.yandex.practicum.eshop.enums.MessagesLog.MESSAGE_LOG_FLUSH_CART;
import static ru.yandex.practicum.eshop.enums.MessagesLog.MESSAGE_LOG_FLUSH_CART_SUCCESS;
import static ru.yandex.practicum.eshop.enums.MessagesLog.MESSAGE_LOG_ITEMS_SIZE;

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
  public Mono<Void> editCart(Long itemId, String actionRequest) throws ActionException {
    Action action = Action.getValueOf(actionRequest);

    //    return cartRepository.findById(CART_ID)
    Mono<Void> actions = cartRepository.findById(CART_ID)
                                       .flatMap(existingCart ->
                                                    cartItemRepository.findCartItemByCartIdAndItemId(
                                                                          CART_ID,
                                                                          itemId)
                                                                      .flatMap(optionalCartItem -> {
                                                                        switch (action) {
                                                                          case PLUS -> {
                                                                            return incrementItem(
                                                                                itemId,
                                                                                Mono.just(
                                                                                    optionalCartItem));
                                                                          }
                                                                          case MINUS -> {
                                                                            return decrementItem(
                                                                                itemId,
                                                                                Mono.just(
                                                                                    optionalCartItem));
                                                                          }
                                                                          case DELETE -> {
                                                                            return optionalCartItem.map(
                                                                                                       cartItemRepository::delete)
                                                                                                   .orElseGet(
                                                                                                       Mono::empty);
                                                                          }
                                                                          default -> {
                                                                            return Mono.error(
                                                                                new ActionException(
                                                                                    "Некорректное действие"));
                                                                          }
                                                                        }
                                                                      })
                                                                      .then(calculateTotal())
                                                                      .doOnNext(
                                                                          existingCart::setTotal)
                                                                      .then(cartRepository.save(
                                                                          existingCart))
                                                                      .then(Mono.empty())
                                       )
                                       .onErrorResume(e ->
                                                          Mono.error(new DataBaseRequestException(
                                                              MESSAGE_LOG_DB_RESPONSE_ERROR.getMessage(),
                                                              e))).then();
    return actions;

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
                           .flatMap(cart -> {
                             //ToDo скорректировать в чатси получения товаров
                             //                             List<Item> items = new ArrayList<>(cart.getItems());
                             List<Item> items = new ArrayList<>();

                             Order order = Order.builder()
                                                .items(items)
                                                .totalSum(cart.getTotal())
                                                .build();

                             return orderRepository.save(order)
                                                   .flatMap(savedOrder -> {
                                                     List<OrderItem> orderItems
                                                         = createAndGetOrderItems(items,
                                                                                  savedOrder);
                                                     return orderItemRepository.saveAll(orderItems)
                                                                               .collectList()
                                                                               .thenReturn(
                                                                                   savedOrder);
                                                   });
                           })
                           .flatMap(order -> flushItemAndCart().thenReturn(order))
                           .map(Order::getId)
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
                          .flatMap(order -> getOrderItems(order.getId()));
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

  private List<OrderItem> createAndGetOrderItems(List<Item> items, Order savedOrder) {
    return items.stream()
                .map(item -> {
                  int count = item.getCount() != null ? item.getCount() : 0;
                  return OrderItem.builder()
                                  .orderId(savedOrder.getId())
                                  .itemId(item.getId())
                                  .count(count)
                                  .build();
                })
                .toList();
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


  private Mono<Void> flushItemAndCart() {
    return Mono.defer(() -> {
      log.info(MESSAGE_LOG_FLUSH_CART.getMessage());

      Cart cart = new Cart(CART_ID, TOTAL_INIT);
      return cartRepository.save(cart)
                           .then(cartItemRepository.deleteAllByCartId(CART_ID))
                           .then(itemRepository.updateAllCountToZero())
                           .doOnSuccess(v -> log.info(MESSAGE_LOG_FLUSH_CART_SUCCESS.getMessage()))
                           .then();
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

  private Mono<Double> calculateTotal() {
    return cartItemRepository.findCartItemsByCartId(CART_ID)
                             .flatMap(cartItem -> itemRepository.findById(cartItem.getItemId())
                                                                .map(item -> item.getPrice()
                                                                             * cartItem.getCount()))
                             .reduce(0.0, Double::sum);
  }

  private Mono<Void> decrementItem(Long itemId, Mono<Optional<CartItem>> existingCartItem) {
    return existingCartItem
        .flatMap(optionalCartItem -> {
          if (optionalCartItem.isPresent()) {
            CartItem cartItem = optionalCartItem.get();
            if (cartItem.getCount() >= 1) {
              cartItem.setCount(cartItem.getCount() - 1);
              return cartItemRepository.save(cartItem)
                                       .then(itemRepository.decrementCount(itemId));
            } else {
              return cartItemRepository.delete(cartItem);
            }
          }
          return Mono.empty();
        });
  }

  private Mono<Void> incrementItem(Long itemId, Mono<Optional<CartItem>> existingCartItem) {
    return existingCartItem
        .flatMap(optionalCartItem -> {
          if (optionalCartItem.isPresent()) {
            CartItem cartItem = optionalCartItem.get();
            cartItem.setCount(cartItem.getCount() + 1);
            return cartItemRepository.save(cartItem);
          } else {
            CartItem newItem = new CartItem();
            newItem.setCartId(CART_ID);
            newItem.setItemId(itemId);
            newItem.setCount(1);
            return cartItemRepository.save(newItem);
          }
        })
        .then(itemRepository.incrementCount(itemId));
  }


  private Mono<List<Item>> getItemsFromCart() {
    return cartItemRepository.findCartItemsByCartId(CART_ID)
                             .map(CartItem::getItemId)
                             .collectList()
                             .flatMap(ids -> itemRepository.findAllById(ids)
                                                           .collectList());
  }
}