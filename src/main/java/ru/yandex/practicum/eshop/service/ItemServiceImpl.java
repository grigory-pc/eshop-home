package ru.yandex.practicum.eshop.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.eshop.dto.CartDto;
import ru.yandex.practicum.eshop.dto.ItemDto;
import ru.yandex.practicum.eshop.dto.OrderDto;
import ru.yandex.practicum.eshop.entity.Item;
import ru.yandex.practicum.eshop.enums.Action;
import ru.yandex.practicum.eshop.enums.Sorting;
import ru.yandex.practicum.eshop.exceptions.SortingException;
import ru.yandex.practicum.eshop.mappers.ItemMapper;
import ru.yandex.practicum.eshop.repository.ItemRepository;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
  private final ItemRepository repository;
  private final ItemMapper itemMapper;

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
      items = repository.findAll(pageableItems);
    } else {
      items = repository.findByTitleContainingIgnoreCase(search, pageableItems);
    }

    return itemMapper.toDtoPage(items);
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