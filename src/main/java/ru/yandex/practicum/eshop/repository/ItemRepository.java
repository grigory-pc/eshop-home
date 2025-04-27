package ru.yandex.practicum.eshop.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.eshop.entity.Item;

/**
 * Получение данных из таблицы Items.
 */
public interface ItemRepository extends JpaRepository<Item, Long> {
  /**
   * Поиск товаров на базе строки поиска.
   *
   * @param search          - строка для поиска товаров с буквами, с которых начинается название
   *                        товаров.
   * @param pageableRequest - содержит from - с какой страницы и size - количество записей + тип
   *                        сортировки.
   * @return коллекция товаров с параметрами пагинации.
   */
  Page<Item> findByTitleContainingIgnoreCase(String search, Pageable pageableRequest);
}
