package ru.yandex.practicum.eshop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * DTO товаров.
 */
@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class ItemDto {
  private Long id;
  private String title;
  private String imagePath;
  private String description;
  private double price;
  private int count;
}