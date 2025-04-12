package ru.yandex.practicum.eshop.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * DTO заказа с товарами.
 */
@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class OrderDto {
  private Long id;
  private List<ItemDto> title;
  private double totalSum;
}