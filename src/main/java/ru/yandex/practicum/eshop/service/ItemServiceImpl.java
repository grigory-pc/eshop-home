package ru.yandex.practicum.eshop.service;

import ch.qos.logback.core.joran.spi.ActionException;
import java.util.List;
import java.util.Optional;
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
import ru.yandex.practicum.eshop.enums.Action;
import ru.yandex.practicum.eshop.enums.Sorting;
import ru.yandex.practicum.eshop.exceptions.SortingException;
import ru.yandex.practicum.eshop.mappers.ItemMapper;
import ru.yandex.practicum.eshop.repository.CartItemRepository;
import ru.yandex.practicum.eshop.repository.CartRepository;
import ru.yandex.practicum.eshop.repository.ItemRepository;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
  private static final Long CART_ID = 1L;
  private static final Long ORDER_ID = 1L;
  private final ItemRepository itemRepository;
  private final ItemMapper itemMapper;
  private final CartRepository cartRepository;
  private final CartItemRepository cartItemRepository;

  @Override
  public Page<ItemDto> getItems(String search, Sorting sort, int pageNumber, int pageSize) {
    Page<Item> items;
    Pageable pageableItems;

    switch (sort) {
      case ALPHA ->
          pageableItems = PageRequest.of(pageNumber, pageSize, Sort.by("title").ascending());
      case PRICE ->
          pageableItems = PageRequest.of(pageNumber, pageSize, Sort.by("price").ascending());
      case NO -> pageableItems = PageRequest.of(pageNumber, pageSize);
      default -> throw new SortingException("Некорректный тип сортировки: " + sort);
    }

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
      case PLUS -> {
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
      case MINUS -> {
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
      case DELETE -> existingCartItem.ifPresent(cartItemRepository::delete);

    }

    double total = cartItemRepository.findCartItemsByCartId(CART_ID)
                                     .stream()
                                     .mapToDouble(cartItem -> {
                                       Item product = itemRepository.getReferenceById(
                                           cartItem.getItemId());
                                       return product.getPrice() * cartItem.getCount();
                                     })
                                     .sum();

    existingCart.setTotal(total);
    cartRepository.save(existingCart);
  }

  @Override
  public CartDto getCartItems() {
    return null;
  }

  @Override
  public ItemDto getItem(Long id) {
    return null;
  }

  @Override
  public Long buyItems(Long id) {
    return null;
  }

  @Override
  public List<OrderDto> getOrders() {
    return null;
  }

  @Override
  public OrderDto getOrder(Long id) {
    return null;
  }
}