package ru.yandex.practicum.eshop.service;

import ch.qos.logback.core.joran.spi.ActionException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
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
import ru.yandex.practicum.eshop.exceptions.SortingException;
import ru.yandex.practicum.eshop.mappers.ItemMapper;
import ru.yandex.practicum.eshop.repository.CartItemRepository;
import ru.yandex.practicum.eshop.repository.CartRepository;
import ru.yandex.practicum.eshop.repository.ItemRepository;
import ru.yandex.practicum.eshop.repository.OrderItemRepository;
import ru.yandex.practicum.eshop.repository.OrderRepository;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
  private static final Long CART_ID = 1L;
  private static final Double TOTAL_INIT = 0.00;
  private final ItemRepository itemRepository;
  private final ItemMapper itemMapper;
  private final CartRepository cartRepository;
  private final OrderRepository orderRepository;
  private final CartItemRepository cartItemRepository;
  private final OrderItemRepository orderItemRepository;

  @Override
  public Page<ItemDto> getItems(String search, Sorting sort, int pageNumber, int pageSize) {
    Page<Item> items;

    Pageable pageableItems = getPageableItemsRequest(sort, pageNumber, pageSize);

    if (search.isEmpty()) {
      items = itemRepository.findAll(pageableItems);
    } else {
      items = itemRepository.findByTitleContainingIgnoreCase(search, pageableItems);
    }

    return itemMapper.toDtoPage(items);
  }

  @Override
  public void editCart(Long itemId, String actionRequest) throws ActionException {
    Action action = Action.getValueOf(actionRequest);

    Cart existingCart = cartRepository.getReferenceById(CART_ID);
    Optional<CartItem> existingCartItem = cartItemRepository.findCartItemByCartIdAndItemId(CART_ID,
                                                                                           itemId);

    switch (action) {
      case PLUS -> incrementItem(itemId, existingCartItem);
      case MINUS -> decrementItem(itemId, existingCartItem);
      case DELETE -> existingCartItem.ifPresent(cartItemRepository::delete);
    }

    existingCart.setTotal(calculateTotal());
    cartRepository.save(existingCart);
  }

  @Override
  public CartDto getCartItems() {
    List<Item> items = getItemsFromCart();

    double total = items.stream()
                        .mapToDouble(item -> item.getPrice() * item.getCount())
                        .sum();

    return CartDto.builder()
                  .id(CART_ID)
                  .items(itemMapper.toListDto(items))
                  .total(total)
                  .build();
  }

  @Override
  public ItemDto getItem(Long id) {
    return itemMapper.toDto(itemRepository.getReferenceById(id));
  }

  @Override
  public Long buyItems() {
    Cart cart = cartRepository.getReferenceById(CART_ID);
    List<Item> items = new ArrayList<>(cart.getItems());

    Order order = Order.builder()
                       .items(items)
                       .totalSum(cart.getTotal())
                       .build();

    Order savedOrder = orderRepository.save(order);

    orderItemRepository.saveAll(createAndGetOrderItems(items, savedOrder));

    flushItemAndCart(cart.getId());

    return savedOrder.getId();
  }

  @Override
  public List<OrderDto> getOrders() {
    List<Order> orders = orderRepository.findAll();

    return orders.stream()
                 .map(order -> getOrderItems(order.getId()))
                 .toList();
  }

  @Override
  public OrderDto getOrderItems(Long id) {
    Map<Long, Integer> orderItemCounts = getItemsCountFromOrderItems(id);

    List<Item> items = getItemsFromOrder(orderItemCounts);

    double total = items.stream()
                        .mapToDouble(item -> item.getPrice() * item.getCount())
                        .sum();

    return OrderDto.builder()
                   .id(id)
                   .items(itemMapper.toListDto(items))
                   .totalSum(total)
                   .build();
  }

  private static List<OrderItem> createAndGetOrderItems(List<Item> items, Order savedOrder) {
    return items.stream()
                .map(item -> {
                  int count = item.getCount() != null ? item.getCount() : 0;
                  return new OrderItem(savedOrder.getId(), item.getId(),
                                       count);
                })
                .toList();
  }

  private List<Item> getItemsFromOrder(Map<Long, Integer> orderItemCounts) {
    return itemRepository.findAllById(orderItemCounts.keySet())
                         .stream()
                         .peek(item -> {
                           Integer countFromOrder = orderItemCounts.get(item.getId());
                           if (countFromOrder != null) {
                             item.setCount(countFromOrder);
                           }
                         })
                         .toList();
  }

  private Map<Long, Integer> getItemsCountFromOrderItems(Long id) {
    return orderItemRepository.findOrderItemsByOrderId(id)
                              .stream()
                              .collect(Collectors.toMap(
                                  OrderItem::getItemId,
                                  OrderItem::getCount
                              ));
  }

  private void flushItemAndCart(Long cartId) {
    Cart cart = new Cart(cartId, TOTAL_INIT, new ArrayList<>());
    cartRepository.save(cart);

    cartItemRepository.deleteAllByCartId(cartId);

    itemRepository.updateAllCountToZero();
  }

  private static Pageable getPageableItemsRequest(Sorting sort, int pageNumber, int pageSize) {
    Pageable pageableItems;
    switch (sort) {
      case ALPHA ->
          pageableItems = PageRequest.of(pageNumber, pageSize, Sort.by("title").ascending());
      case PRICE ->
          pageableItems = PageRequest.of(pageNumber, pageSize, Sort.by("price").ascending());
      case NO -> pageableItems = PageRequest.of(pageNumber, pageSize);
      default -> throw new SortingException("Некорректный тип сортировки: " + sort);
    }
    return pageableItems;
  }

  private double calculateTotal() {
    return cartItemRepository.findCartItemsByCartId(CART_ID)
                             .stream()
                             .mapToDouble(cartItem -> {
                               Item product = itemRepository.getReferenceById(
                                   cartItem.getItemId());
                               return product.getPrice() * cartItem.getCount();
                             })
                             .sum();
  }

  private void decrementItem(Long itemId, Optional<CartItem> existingCartItem) {
    if (existingCartItem.isPresent()) {
      CartItem cartItem = existingCartItem.get();

      if (cartItem.getCount() >= 1) {
        cartItem.setCount(cartItem.getCount() - 1);
        cartItemRepository.save(cartItem);

        itemRepository.decrementCount(itemId);
      } else {
        cartItemRepository.delete(cartItem);
      }
    }
  }

  private void incrementItem(Long itemId, Optional<CartItem> existingCartItem) {
    if (existingCartItem.isPresent()) {
      CartItem cartItem = existingCartItem.get();

      cartItem.setCount(cartItem.getCount() + 1);
      cartItemRepository.save(cartItem);
    } else {
      CartItem newItem = new CartItem();
      newItem.setCartId(CART_ID);
      newItem.setItemId(itemId);
      newItem.setCount(1);

      cartItemRepository.save(newItem);
    }
    itemRepository.incrementCount(itemId);
  }

  private List<Item> getItemsFromCart() {
    return cartItemRepository.findCartItemsByCartId(CART_ID)
                             .stream()
                             .map(CartItem::getItemId)
                             .collect(Collectors.collectingAndThen(
                                 Collectors.toList(),
                                 itemRepository::findAllById
                             ));
  }
}