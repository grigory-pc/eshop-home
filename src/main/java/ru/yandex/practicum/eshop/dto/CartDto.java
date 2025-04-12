package ru.yandex.practicum.eshop.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * DTO корзины с товарами.
 */
@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class CartDto {
  private Long id;
  private List<ItemDto> items;
  private double total;
}