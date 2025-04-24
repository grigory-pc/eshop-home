package ru.yandex.practicum.eshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.eshop.entity.Item;

/**
 * Получение данных из таблицы Items.
 */
public interface ItemRepository extends JpaRepository<Item, Long> {
}
