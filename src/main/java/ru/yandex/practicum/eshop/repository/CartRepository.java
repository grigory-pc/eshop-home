package ru.yandex.practicum.eshop.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.eshop.entity.Cart;

/**
 * Получение данных из таблицы Carts.
 */
@Repository
public interface CartRepository extends R2dbcRepository<Cart, Long> {
}
