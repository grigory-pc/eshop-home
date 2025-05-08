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
 * Класс связи товара с заказом.
 */
@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@IdClass(OrderItemKey.class)
@Table(name = "order_item")
public class OrderItem {
  @Id
  @Column(name = "order_id", nullable = false)
  private Long orderId;
  @Id
  @Column(name = "item_id", nullable = false)
  private Long itemId;
  private Integer count;
}