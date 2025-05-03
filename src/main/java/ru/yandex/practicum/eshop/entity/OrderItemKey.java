package ru.yandex.practicum.eshop.entity;

import java.io.Serializable;
import lombok.Data;

@Data
public class OrderItemKey implements Serializable {
  private Long orderId;
  private Long itemId;
}