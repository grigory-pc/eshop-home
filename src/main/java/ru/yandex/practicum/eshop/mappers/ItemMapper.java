package ru.yandex.practicum.eshop.mappers;

import java.util.List;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.eshop.dto.ItemDto;
import ru.yandex.practicum.eshop.entity.Item;

/**
 * Маппер между объектами DAO Item и DTO ItemDto.
 */
@Mapper(componentModel = "spring")
public interface ItemMapper {
  Item toItem(ItemDto dto);

  ItemDto toPreviewDto(Item post);

  ItemDto toDto(Item post);

  default Page<ItemDto> toDtoPage(Page<Item> items) {
    List<ItemDto> dtos = items.stream()
                              .map(this::toPreviewDto)
                              .toList();
    return new PageImpl<>(dtos, pageable(items), items.getTotalElements());
  }

  default Pageable pageable(Page<Item> items) {
    return items.getPageable();
  }
}