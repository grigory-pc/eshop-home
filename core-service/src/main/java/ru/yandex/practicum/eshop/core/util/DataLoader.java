package ru.yandex.practicum.eshop.core.util;

import ru.yandex.practicum.eshop.core.entity.Cart;
import ru.yandex.practicum.eshop.core.entity.Item;
import ru.yandex.practicum.eshop.core.repository.CartRepository;
import ru.yandex.practicum.eshop.core.repository.ItemRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Класс для загрузки данных в БД при запуске приложения.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {
  private static final String ITEM_SHORTS_IMG_PATH = "/images/shorts.jpg";
  private static final String ITEM_SUNGLASSES_IMG_PATH = "/images/sunglasses.jpg";
  private static final String ITEM_TSHIRT_IMG_PATH = "/images/tshirt.jpg";
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

    Cart newCart = Cart.builder()
                       .total(TOTAL_INIT)
                       .build();
    cartRepository.save(newCart)
                  .doOnNext(cart -> System.out.println(
                      "Сохранена корзина с ID: " + cart.getId()))
                  .subscribe(System.out::println);

    itemRepository.saveAll(List.of(shorts, sunglasses, tShirt))
                  .doOnNext(item -> System.out.println(
                      "Сохранены товары "))
                  .subscribe(System.out::println);
  }
}
