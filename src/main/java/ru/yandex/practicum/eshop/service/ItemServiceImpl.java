package ru.yandex.practicum.eshop.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.eshop.dto.CartDto;
import ru.yandex.practicum.eshop.dto.ItemDto;
import ru.yandex.practicum.eshop.dto.OrderDto;
import ru.yandex.practicum.eshop.enums.Action;
import ru.yandex.practicum.eshop.enums.Sort;

@Service
public class ItemServiceImpl implements ItemService{
  @Override
  public Page<ItemDto> getItems(String search, Sort sort, int pageNumber, int pageSize) {
    return null;
  }

  @Override
  public void editCart(Long id, Action action) {

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