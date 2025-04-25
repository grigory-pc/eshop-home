package ru.yandex.practicum.eshop.util;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.eshop.entity.Item;
import ru.yandex.practicum.eshop.repository.ItemRepository;

/**
 * Класс для загрузки данных в БД при запуске приложения.
 */
@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {
  private static final String ITEM_SHORTS_IMG_PATH = "/images/shorts.jpg";
  private static final String ITEM_SUNGLASSES_IMG_PATH = "/images/sunglasses.jpg";
  private static final String ITEM_TSHIRT_IMG_PATH = "/images/tshirt.jpg";

  private final ItemRepository itemRepository;

  @Override
  public void run(String... args) {

    Item shorts = Item.builder()
                      .title("Шорты")
                      .imagePath(ITEM_SHORTS_IMG_PATH)
                      .description("Летние шорты карго")
                      .price(1399.99)
                      .count(11)
                      .build();

    Item sunglasses = Item.builder()
                          .title("Солцезащитные очки")
                          .imagePath(ITEM_SUNGLASSES_IMG_PATH)
                          .description("Солцезащитные очки с UV и поляризацией")
                          .price(3410.00)
                          .count(6)
                          .build();

    Item tShirt = Item.builder()
                      .title("Футболка")
                      .imagePath(ITEM_TSHIRT_IMG_PATH)
                      .description("Футболка с рукавом")
                      .price(567.99)
                      .count(1)
                      .build();

    itemRepository.saveAll(List.of(shorts, sunglasses, tShirt));
  }
}