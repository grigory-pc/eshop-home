package ru.yandex.practicum.eshop.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

  /**
   * Увеличение количества товаров для отображения на главной странице после добавления в корзину.
   *
   * @param id - id товара.
   */
  @Transactional
  @Modifying
  @Query("UPDATE Item i SET i.count = i.count + 1 WHERE i.id = :id")
  void incrementCount(@Param("id") Long id);

  /**
   * Уменьшение количества товаров для отображения на главной странице после добавления в корзину.
   *
   * @param id - id товара.
   */
  @Transactional
  @Modifying
  @Query("UPDATE Item i SET i.count = i.count - 1 WHERE i.id = :id")
  void decrementCount(@Param("id") Long id);

  /**
   * Сброс количества товаров.
   */
  @Transactional
  @Modifying
  @Query("UPDATE Item i SET i.count = 0")
  void updateAllCountToZero();
}