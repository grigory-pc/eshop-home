package ru.yandex.practicum.eshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.eshop.entity.Cart;

/**
 * Получение данных из таблицы Carts.
 */public interface CartRepository extends JpaRepository<Cart, Long> {
}
