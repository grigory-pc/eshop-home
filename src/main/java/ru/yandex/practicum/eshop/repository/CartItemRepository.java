package ru.yandex.practicum.eshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.eshop.entity.CartItem;

/**
 * Получение данных из таблицы CartItem.
 */
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}