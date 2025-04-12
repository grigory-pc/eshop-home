package ru.yandex.practicum.eshop.service;

import java.util.List;
import org.springframework.data.domain.Page;
import ru.yandex.practicum.eshop.dto.CartDto;
import ru.yandex.practicum.eshop.dto.ItemDto;
import ru.yandex.practicum.eshop.dto.OrderDto;
import ru.yandex.practicum.eshop.entity.Action;
import ru.yandex.practicum.eshop.entity.Sort;

/**
 * Сервис для работы с товарами.
 */
public interface ItemService {
  /**
   * Получение всех товаров.
   *
   * @param search      - строка поиска.
   * @param sort        - критерий сортировка.
   * @param pageNumber  - с какой страницы
   * @param pageSize    - количество записей.
   * @return список товаров.
   */
  Page<ItemDto> getItems(String search, Sort sort, int pageNumber, int pageSize);

  /**
   * Изменение состава корзины товаров.
   *
   * @param id - id товара.
   * @param action - действие с товаром в корзине.
   */
  void editCart(Long id, Action action);

  /**
   * Получение всех товаров корзины.
   *
   * @return список товаров в корзине.
   */
  CartDto getCartItems();

  /**
   * Получение объекта товара по id.
   *
   * @param id - id товара.
   * @return объекта товара.
   */
  ItemDto getItem(Long id);

  /**
   * Формирование заказа для товаров в корзине.
   *
   * @param id - id корзины.
   * @return id заказа.
   */
  Long buyItems(Long id);

  /**
   * Получение всех заказов.
   *
   * @return список заказов.
   */
  List<OrderDto> getOrders();

  /**
   * Получение объекта заказа по id.
   *
   * @param id - id заказа.
   * @return объекта заказа.
   */
  OrderDto getOrder(Long id);
}