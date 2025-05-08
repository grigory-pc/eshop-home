package ru.yandex.practicum.eshop.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.eshop.entity.OrderItem;

/**
 * Получение данных из таблицы OrderItem.
 */
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
  /**
   * Получение всех записей по order id.
   *
   * @param orderId - id заказа.
   * @return список всех записей, полученных по order id.
   */
  List<OrderItem> findOrderItemsByOrderId(Long orderId);
}