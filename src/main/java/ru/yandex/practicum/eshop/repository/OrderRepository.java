package ru.yandex.practicum.eshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.eshop.entity.Order;

/**
 * Получение данных из таблицы Orders.
 */
public interface OrderRepository extends JpaRepository<Order, Long> {
}
