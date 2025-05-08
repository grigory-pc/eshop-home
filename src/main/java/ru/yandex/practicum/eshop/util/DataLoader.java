package ru.yandex.practicum.eshop.util;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.eshop.entity.Cart;
import ru.yandex.practicum.eshop.entity.Item;
import ru.yandex.practicum.eshop.repository.CartRepository;
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
  private static final Long CART_ID = 1L;
  private static final Double TOTAL_INIT = 0.00;

  private final ItemRepository itemRepository;
  private final CartRepository cartRepository;

  @Override
  public void run(String... args) {

    Item shorts = Item.builder()
                      .title("Шорты")
                      .imgPath(ITEM_SHORTS_IMG_PATH)
                      .description("Летние шорты карго")
                      .price(1399.99)
                      .count(0)
                      .build();

    Item sunglasses = Item.builder()
                          .title("Солцезащитные очки")
                          .imgPath(ITEM_SUNGLASSES_IMG_PATH)
                          .description("Солцезащитные очки с UV и поляризацией")
                          .price(3410.00)
                          .count(0)
                          .build();

    Item tShirt = Item.builder()
                      .title("Футболка")
                      .imgPath(ITEM_TSHIRT_IMG_PATH)
                      .description("Футболка с рукавом")
                      .price(567.99)
                      .count(0)
                      .build();

    itemRepository.saveAll(List.of(shorts, sunglasses, tShirt));
    cartRepository.save(new Cart(CART_ID, TOTAL_INIT, new ArrayList<>()));
  }
}