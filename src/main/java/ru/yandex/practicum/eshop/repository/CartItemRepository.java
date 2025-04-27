package ru.yandex.practicum.eshop.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.eshop.entity.Cart;
import ru.yandex.practicum.eshop.entity.CartItem;
import ru.yandex.practicum.eshop.entity.Item;

/**
 * Получение данных из таблицы CartItem.
 */
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

  /**
   * Получение записи соотношения товара к корзине.
   *
   * @param cart - объект корзины.
   * @param item - объект товара.
   * @return запись соотношения товара к корзине.
   */
  CartItem findCartItemByCartAndItem(Cart cart, Item item);

  /**
   * Получение всех записей по cart id.
   *
   * @param cart - объект cart, который содержит cart id.
   * @return список всех записей, полученных по cart id.
   */
  List<CartItem> findCartItemsByCart(Cart cart);
}