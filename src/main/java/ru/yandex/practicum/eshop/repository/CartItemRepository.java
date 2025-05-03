package ru.yandex.practicum.eshop.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.eshop.entity.CartItem;

/**
 * Получение данных из таблицы CartItem.
 */
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

  /**
   * Получение записи соотношения товара к корзине.
   *
   * @param cartId - id корзины.
   * @param itemId - id товара.
   * @return запись соотношения товара к корзине.
   */
  Optional<CartItem> findCartItemByCartIdAndItemId(Long cartId, Long itemId);

  /**
   * Получение всех записей по cart id.
   *
   * @param cartId - id корзины.
   * @return список всех записей, полученных по cart id.
   */
  List<CartItem> findCartItemsByCartId(Long cartId);
}