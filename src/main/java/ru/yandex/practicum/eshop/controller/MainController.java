package ru.yandex.practicum.eshop.controller;

import ch.qos.logback.core.joran.spi.ActionException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.eshop.dto.PagingDto;
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
  public static final String REDIRECT_ORDERS = "redirect:/orders";
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
   * @param search     - строка с поисков по названию/описанию товара (по умолчанию, пустая строка -
   *                   все товары)
   * @param sort       - сортировка перечисление NO, ALPHA, PRICE (по умолчанию, NO - не
   *                   использовать сортировку).
   * @param model      - модель данных.
   * @return главная страница.
   */
  @GetMapping("/main/items")
  public Mono<String> getItems(@RequestParam(defaultValue = "") String search,
                               @RequestParam(defaultValue = "NO") Sorting sort,
                               @RequestParam(defaultValue = "10") int pageSize,
                               @RequestParam(defaultValue = "0") int pageNumber,
                               Model model) {

    log.info(
        "Получен запрос на получение списка товаров для главной страницы. pageNumber={} pageSize={} sort={}",
        pageNumber, pageSize, sort);

    return itemService.getItems(search, sort, pageNumber, pageSize)
                      .flatMap(page -> {
                        model.addAttribute("items", page.getContent());
                        model.addAttribute("search", search);
                        model.addAttribute("paging", new PagingDto(
                            pageNumber + 1,
                            pageSize,
                            page.hasPrevious(),
                            page.hasNext()
                        ));
                        return Mono.just("main");
                      })
                      .onErrorResume(e -> {
                        log.error("Ошибка при получении списка товаров", e);
                        return Mono.just("error");
                      });
  }

  /**
   * Изменение количества товаров в корзине на главной странице.
   *
   * @param itemId - id товара.
   * @param action - действие с товаром в корзине.
   * @return перенаправляет на главную страницу.
   */
  @PostMapping("/main/items/{id}")
  public Mono<String> updateMainCartItems(@PathVariable(name = "id") @NotNull Long itemId,
                                          @RequestParam(defaultValue = "") @NotBlank String action)
      throws ActionException {

    log.info("Получен запрос на изменение корзины: {} для товара id = {}", action, itemId);

    return itemService.editCart(itemId, action)
                      .then(Mono.just(REDIRECT_MAIN));
  }

  /**
   * Обрабатывает GET-запросы на получение списка товаров в корзине.
   *
   * @param model - модель данных.
   * @return главная страница.
   */
  @GetMapping("/cart/items")
  public Mono<String> getCartItems(Model model) {
    return Mono.just(model)
               .doOnNext(m -> log.info("Получен запрос на получение списка товаров в корзине"))
               .flatMap(m -> itemService.getCartItems()
                                        .doOnNext(dto -> log.info(
                                            "Получен список товаров в корзине размером: {}",
                                            dto.items().size()))
                                        .flatMap(dto -> {
                                          if (dto.items().isEmpty()) {
                                            m.addAttribute("empty", "true");
                                          } else {
                                            m.addAttribute("items", dto.items());
                                            m.addAttribute("total", dto.total());
                                          }
                                          return Mono.just(m);
                                        })
               )
               .thenReturn("cart");
  }

  /**
   * Изменение количества товаров в корзине.
   *
   * @param itemId - id товара.
   * @param action - действие с товаром в корзине.
   * @return перенаправляет на страницу корзины.
   */
  @PostMapping("/cart/items/{id}")
  public Mono<String> updateCartItems(@PathVariable(name = "id") @NotNull Long itemId,
                                      @RequestParam(defaultValue = "") @NotBlank String action)
      throws ActionException {

    log.info("Получен запрос на изменение корзины: {} для товара id = {}", action, itemId);

    return itemService.editCart(itemId, action)
                      .then(Mono.just(REDIRECT_CART));
  }

  /**
   * Обрабатывает GET-запросы на получение карточки товара по id.
   *
   * @param id    - id товара.
   * @param model - модель данных.
   * @return страница карточки товара.
   */
  @GetMapping("/items/{id}")
  public Mono<String> getItemById(@PathVariable @NotNull Long id, Model model) {
    log.info("Получен запрос на получение карточки товара для id = {}", id);

    return itemService.getItem(id)
                      .doOnNext(item -> {
                        log.info("Из базы данных получен объект товара с id: {}", id);
                        model.addAttribute("item", item);
                      })
                      .thenReturn("item");
  }

  /**
   * Изменение количества товаров в корзине из карточки товара.
   *
   * @param id     - id товара.
   * @param action - действие с товаром в корзине.
   * @return перенаправляет на страницу карточки товара.
   */
  @PostMapping("/items/{id}")
  public Mono<String> updateItems(@PathVariable(name = "id") Long id,
                                  @RequestParam(defaultValue = "") String action)
      throws ActionException {
    log.info("Получен запрос из карточки товара на изменение корзины: {} для товара id = {}",
             action, id);


    return itemService.editCart(id, action)
                      .then(Mono.just(REDIRECT_ITEMS + id));
  }

  /**
   * Купить товары в корзине.
   *
   * @return перенаправляет на страницу заказов.
   */
  @PostMapping("/buy")
  public Mono<String> buyItems(RedirectAttributes redirectAttributes) {
    log.info("Получен запрос на покупку товаров в корзине");

    return itemService.buyItems()
                      .doOnNext(orderId -> {
                        log.info("Создан новый заказ = {}", orderId);
                        redirectAttributes.addAttribute("orderId", orderId);
                        redirectAttributes.addFlashAttribute("newOrder", true);
                      })
                      .thenReturn(REDIRECT_ORDERS);
  }

  /**
   * Обрабатывает GET-запросы на получение списка заказов.
   *
   * @param model - модель данных.
   * @return страница заказов.
   */
  @GetMapping("/orders")
  public Mono<String> getOrders(Model model) {
    return Mono.just(model)
               .doOnNext(m -> log.info("Получен запрос на получение списка заказов"))
               .flatMap(m -> itemService.getOrders()
                                        .doOnNext(order -> log.info("Получен список заказов"))
                                        .doOnComplete(
                                            () -> log.info("Завершено получение списка заказов"))
                                        .collectList()
                                        .doOnNext(
                                            list -> log.info("Получен список заказов размером: {}",
                                                             list.size()))
                                        .flatMap(orders -> {
                                          m.addAttribute("orders", orders);
                                          return Mono.just(m);
                                        })
               )
               .thenReturn("orders");
  }

  /**
   * Обрабатывает GET-запросы на получение карточки заказа.
   *
   * @param id       - id заказа.
   * @param newOrder - флаг нового заказа.
   * @param model    - модель данных.
   * @return страница заказа.
   */
  @GetMapping("/orders/{id}")
  public Mono<String> getOrderById(@PathVariable @NotNull Long id,
                                   @RequestParam(defaultValue = "false") Boolean newOrder,
                                   Model model) {
    return Mono.just(model)
               .doOnNext(
                   m -> log.info("Получен запрос на получение карточки заказа для id = {}", id))
               .flatMap(m -> itemService.getOrderItems(id)
                                        .doOnNext(order -> log.info(
                                            "Из базы данных получен объект товара с id: {}", id))
                                        .flatMap(order -> {
                                          m.addAttribute("order", order);
                                          m.addAttribute("newOrder", newOrder);
                                          return Mono.just(m);
                                        })
               )
               .onErrorResume(e -> {
                 log.error("Ошибка при получении заказа для id = {}", id, e);
                 return Mono.error(new ResponseStatusException(
                     HttpStatus.INTERNAL_SERVER_ERROR,
                     "Ошибка получения заказа"
                 ));
               })
               .thenReturn("order");
  }
}