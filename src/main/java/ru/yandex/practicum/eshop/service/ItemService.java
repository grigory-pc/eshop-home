package ru.yandex.practicum.eshop.service;

import ch.qos.logback.core.joran.spi.ActionException;
import java.util.List;
import org.springframework.data.domain.Page;
import ru.yandex.practicum.eshop.dto.CartDto;
import ru.yandex.practicum.eshop.dto.ItemDto;
import ru.yandex.practicum.eshop.dto.OrderDto;
import ru.yandex.practicum.eshop.enums.Sorting;

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
  Page<ItemDto> getItems(String search, Sorting sort, int pageNumber, int pageSize);

  /**
   * Изменение состава корзины товаров.
   *
   * @param itemId - id товара.
   * @param action - действие с товаром в корзине.
   *
   * @throws ActionException - исключение в случае некорректного значения в запросе для action.
   */
  void editCart(Long itemId, String action) throws ActionException;

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
   * @return id заказа.
   */
  Long buyItems();

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
  OrderDto getOrderItems(Long id);
}