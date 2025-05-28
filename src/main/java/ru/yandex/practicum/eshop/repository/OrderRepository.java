package ru.yandex.practicum.eshop.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.eshop.entity.Orders;

/**
 * Получение данных из таблицы Orders.
 */
@Repository
public interface OrderRepository extends R2dbcRepository<Orders, Long> {
}