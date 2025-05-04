package ru.yandex.practicum.eshop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Класс связи товара с корзиной.
 */
@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@IdClass(CartItemKey.class)
@Table(name = "cart_item")
public class CartItem {
  @Id
  @Column(name = "cart_id", nullable = false)
  private Long cartId;
  @Id
  @Column(name = "item_id", nullable = false)
  private Long itemId;
  private Integer count;
}