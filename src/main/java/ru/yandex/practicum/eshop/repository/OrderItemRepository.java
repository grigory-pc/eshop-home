package ru.yandex.practicum.eshop.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import ru.yandex.practicum.eshop.entity.OrderItem;

/**
 * Получение данных из таблицы OrderItem.
 */
@Repository
public interface OrderItemRepository extends R2dbcRepository<OrderItem, Long> {
  /**
   * Получение всех записей по order id.
   *
   * @param orderId - id заказа.
   * @return список всех записей, полученных по order id.
   */
  Flux<OrderItem> findOrderItemsByOrderId(Long orderId);
}