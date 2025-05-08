package ru.yandex.practicum.eshop.entity;

import java.io.Serializable;
import lombok.Data;

@Data
public class CartItemKey implements Serializable {
  private Long cartId;
  private Long itemId;
}