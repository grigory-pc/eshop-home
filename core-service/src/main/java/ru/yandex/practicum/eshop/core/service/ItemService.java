package ru.yandex.practicum.eshop.core.service;

import ch.qos.logback.core.joran.spi.ActionException;
import ru.yandex.practicum.eshop.core.dto.CartDto;
import ru.yandex.practicum.eshop.core.enums.Sorting;
import org.springframework.data.domain.PageImpl;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.eshop.core.dto.ItemDto;
import ru.yandex.practicum.eshop.core.dto.OrderDto;

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
  Mono<PageImpl<ItemDto>> getItems(String search, Sorting sort, int pageNumber, int pageSize);

  /**
   * Изменение состава корзины товаров.
   *
   * @param itemId - id товара.
   * @param action - действие с товаром в корзине.
   *
   * @throws ActionException - исключение в случае некорректного значения в запросе для action.
   */
  Mono<Void> editCart(Long itemId, String action) throws ActionException;

  /**
   * Получение всех товаров корзины.
   *
   * @return список товаров в корзине.
   */
  Mono<CartDto> getCartItems();

  /**
   * Получение объекта товара по id.
   *
   * @param id - id товара.
   * @return объекта товара.
   */
  Mono<ItemDto> getItem(Long id);

  /**
   * Формирование заказа для товаров в корзине.
   *
   * @return id заказа.
   */
  Mono<Long> buyItems();

  /**
   * Получение всех заказов.
   *
   * @return список заказов.
   */
  Flux<OrderDto> getOrders();

  /**
   * Получение объекта заказа по id.
   *
   * @param id - id заказа.
   * @return объекта заказа.
   */
  Mono<OrderDto> getOrderItems(Long id);
}