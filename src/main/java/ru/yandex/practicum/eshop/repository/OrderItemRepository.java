package ru.yandex.practicum.eshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.eshop.entity.OrderItem;

/**
 * Получение данных из таблицы OrderItem.
 */
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
