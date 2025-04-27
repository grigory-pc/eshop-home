package ru.yandex.practicum.eshop.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;
import ru.yandex.practicum.eshop.dto.CartDto;
import ru.yandex.practicum.eshop.dto.ItemDto;
import ru.yandex.practicum.eshop.dto.OrderDto;
import ru.yandex.practicum.eshop.dto.PagingDto;
import ru.yandex.practicum.eshop.enums.Action;
import ru.yandex.practicum.eshop.enums.Sorting;
import ru.yandex.practicum.eshop.service.ItemService;

/**
 * Контроллер обрабатывает запросы на главной странице витрины магазина.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class MainController {
  public static final String REDIRECT_MAIN = "redirect:/main/items";
  public static final String REDIRECT_CART = "redirect:/cart/items";
  public static final String REDIRECT_ITEMS = "redirect:/items/";
  public static final String LOG_CART_UPDATED = "Изменение корзины для товара id={} произведено";
  private final ItemService itemService;

  /**
   * Перенаправление запросов с "/" на "/main/items".
   *
   * @return redirect /main/items.
   */
  @GetMapping("/")
  public RedirectView redirectToMainItems() {
    return new RedirectView("/main/items");
  }

  /**
   * Обрабатывает GET-запросы на получение списка товаров для главной страницы.
   *
   * @param pageNumber - номер текущей страницы (по умолчанию, 1)
   * @param pageSize   - максимальное число товаров на странице (по умолчанию, 10)
   * @param search   - строка с поисков по названию/описанию товара (по умолчанию, пустая строка - все товары)
   * @param sort   - сортировка перечисление NO, ALPHA, PRICE (по умолчанию, NO - не использовать сортировку).
   * @param model - модель данных.
   * @return главная страница.
   */
  @GetMapping("/main/items")
  public String getItems(@RequestParam(defaultValue = "") String search,
                         @RequestParam(defaultValue = "NO") Sorting sort,
                         @RequestParam(defaultValue = "10") int pageSize,
                         @RequestParam(defaultValue = "0") int pageNumber,
                         Model model) {

    log.info(
        "Получен запрос на получение списка товаров для главной страницы. pageNumber={} pageSize={}",
        pageNumber, pageSize);

    Page<ItemDto> items = itemService.getItems(search, sort, pageNumber, pageSize);

    model.addAttribute("items", items.getContent());
    model.addAttribute("search", search);
    model.addAttribute("paging", new PagingDto(
        pageNumber + 1,
        pageSize,
        items.hasPrevious(),
        items.hasNext()
    ));

    return "main";
  }

  /**
   * Изменение количества товаров в корзине на главной странице.
   *
   * @param itemId - id товара.
   * @param action - действие с товаром в корзине.
   * @return перенаправляет на главную страницу.
   */
  @PostMapping("/main/items/{id}")
  public String updateMainCartItems(@PathVariable(name = "id") Long itemId,
                                    @RequestParam(defaultValue = "") Action action) {

    log.info("Получен запрос на изменение корзины: {} для товара id = {}", action, itemId);

    itemService.editCart(itemId, action);

    log.info(LOG_CART_UPDATED, itemId);

    return REDIRECT_MAIN;
  }

  /**
   * Обрабатывает GET-запросы на получение списка товаров в корзине.
   *
   * @param model - модель данных.
   * @return главная страница.
   */
  @GetMapping("/cart/items")
  public String getCartItems(Model model) {
    log.info("Получен запрос на получение списка товаров в корзине");

    CartDto cartDto = itemService.getCartItems();
    log.info("Получен список товаров в корзине размеров: {}", cartDto.getItems().size());

    if (cartDto.getItems().isEmpty()) {
      model.addAttribute("empty", ("true"));
    } else {
      model.addAttribute("items", cartDto.getItems());
      model.addAttribute("total", cartDto.getTotal());
    }

    return "cart";
  }

  /**
   * Изменение количества товаров в корзине.
   *
   * @param id - id товара.
   * @param action - действие с товаром в корзине.
   * @return перенаправляет на страницу корзины.
   */
  @PostMapping("/cart/items/{id}")
  public String updateCartItems(@PathVariable(name = "id") Long id,
                                @RequestParam(defaultValue = "") Action action) {

    log.info("Получен запрос на изменение корзины: {} для товара id = {}", action, id);

    itemService.editCart(id, action);
    log.info(LOG_CART_UPDATED, id);

    return REDIRECT_CART;
  }

  /**
   * Обрабатывает GET-запросы на получение карточки товара по id.
   *
   * @param id - id товара.
   * @param model - модель данных.
   * @return страница карточки товара.
   */
  @GetMapping("/items/{id}")
  public String getItemById(@PathVariable Long id, Model model) {
    log.info("Получен запрос на получение карточки товара для id = {}", id);

    ItemDto itemDto = itemService.getItem(id);
    log.info("Из базы данных получен объект товара с id: {}", id);

    model.addAttribute("item", itemDto);

    return "item";
  }

  /**
   * Изменение количества товаров в корзине из карточки товара.
   *
   * @param id - id товара.
   * @param action - действие с товаром в корзине.
   * @return перенаправляет на страницу карточки товара.
   */
  @PostMapping("/items/{id}")
  public String updateItems(@PathVariable(name = "id") Long id,
                            @RequestParam(defaultValue = "") Action action) {
    log.info("Получен запрос из карточки товара на изменение корзины: {} для товара id = {}",
             action, id);

    itemService.editCart(id, action);
    log.info(LOG_CART_UPDATED, id);

    return REDIRECT_ITEMS + id;
  }

  /**
   * Купить товары в корзине.
   *
   * @param id - id корзины.
   * @return перенаправляет на страницу заказов.
   */
  @PostMapping("/buy/{id}")
  public String buyItems(@PathVariable(name = "id") Long id) {
    log.info("Получен запрос на покупку товаров в корзине");

    Long orderId = itemService.buyItems(id);
    log.info("Создан новый заказ = {}", orderId);

    return String.format("/orders/%s?newOrder=true", orderId);
  }

  /**
   * Обрабатывает GET-запросы на получение списка заказов.
   *
   * @param model - модель данных.
   * @return страница заказов.
   */
  @GetMapping("/orders")
  public String getOrders(Model model) {
    log.info("Получен запрос на получение списка заказов");

    List<OrderDto> orders = itemService.getOrders();
    log.info("Получен список заказов размером: {}", orders.size());

    model.addAttribute("orders", orders);

    return "orders";
  }

  /**
   * Обрабатывает GET-запросы на получение карточки заказа.
   *
   * @param id - id заказа.
   * @param newOrder - флаг нового заказа.
   * @param model - модель данных.
   * @return страница заказа.
   */
  @GetMapping("/orders/{id}")
  public String getOrderById(@PathVariable Long id,
                             @RequestParam(defaultValue = "false") Boolean newOrder,
                             Model model) {
    log.info("Получен запрос на получение карточки заказа для id = {}", id);

    OrderDto orderDto = itemService.getOrder(id);
    log.info("Из базы данных получен объект товара с id: {}", id);

    model.addAttribute("order", orderDto);
    model.addAttribute("newOrder", newOrder);

    return "order";
  }
}