package ru.yandex.practicum.eshop.repository;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.eshop.entity.Item;

/**
 * Получение данных из таблицы Items.
 */
@Repository
public interface ItemRepository extends R2dbcRepository<Item, Long> {
  /**
   * Поиск товаров на базе строки поиска.
   *
   * @param search          - строка для поиска товаров с буквами, с которых начинается название
   *                        товаров.
   * @param pageableRequest - содержит from - с какой страницы и size - количество записей + тип
   *                        сортировки.
   * @return коллекция товаров с параметрами пагинации.
   */
  Flux<Item> findByTitleContainingIgnoreCase(String search, Pageable pageableRequest);

  /**
   * Увеличение количества товаров для отображения на главной странице после добавления в корзину.
   *
   * @param id - id товара.
   */
  @Transactional
  @Modifying
  @Query("UPDATE Item i SET i.count = i.count + 1 WHERE i.id = :id")
  Mono<Void> incrementCount(@Param("id") Long id);

  /**
   * Уменьшение количества товаров для отображения на главной странице после добавления в корзину.
   *
   * @param id - id товара.
   */
  @Transactional
  @Modifying
  @Query("UPDATE Item i SET i.count = i.count - 1 WHERE i.id = :id")
  Mono<Void> decrementCount(@Param("id") Long id);

  /**
   * Сброс количества товаров.
   */
  @Transactional
  @Modifying
  @Query("UPDATE Item i SET i.count = 0")
  Mono<Void> updateAllCountToZero();
}