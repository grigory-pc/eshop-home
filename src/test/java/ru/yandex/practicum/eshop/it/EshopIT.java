package ru.yandex.practicum.eshop.it;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.yandex.practicum.eshop.entity.CartItem;
import ru.yandex.practicum.eshop.entity.Item;
import ru.yandex.practicum.eshop.entity.Order;
import ru.yandex.practicum.eshop.entity.OrderItem;
import ru.yandex.practicum.eshop.repository.ItemRepository;
import ru.yandex.practicum.eshop.repository.OrderRepository;
import ru.yandex.practicum.eshop.utils.Data;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class EshopIT {
  private static final Long ITEM_T_SHIRT_ID = 1L;
  private static final Long CART_ID = 1L;
  private static final Long ORDER_ID = 1L;
  private static final Double TOTAL_INIT = 0.00;
  private static final String ACTION = "plus";

  @Autowired
  private WebApplicationContext webApplicationContext;
  @Autowired
  private JdbcTemplate jdbcTemplate;
  @Autowired
  private ItemRepository itemRepository;
  @Autowired
  private OrderRepository orderRepository;

  private MockMvc mockMvc;

  @BeforeAll
  public static void initTables(@Autowired DataSource dataSource) {
    try (Connection conn = dataSource.getConnection()) {
      ScriptUtils.executeSqlScript(conn, new ClassPathResource("/schema.sql"));
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

    jdbcTemplate = webApplicationContext.getBean(JdbcTemplate.class);
  }

  @AfterEach
  void flushData() {
    String sqlUpdateCart = "UPDATE carts SET total = ? WHERE id = ?";
    jdbcTemplate.update(sqlUpdateCart, CART_ID, TOTAL_INIT);

    String sqlItems = "UPDATE items SET count = ? WHERE id = ?";
    jdbcTemplate.update(sqlItems, 0, ITEM_T_SHIRT_ID);

    jdbcTemplate.execute("DELETE FROM cart_item");
    jdbcTemplate.execute("DELETE FROM orders");
    jdbcTemplate.execute("DELETE FROM order_item");
  }

  @Test
  @DisplayName("Позитивный тест - проверяем перенаправление на url = /main/items")
  void redirectToMainItems() throws Exception {
    String url = "/";
    String expectedRedirectUrl = "/main/items";

    mockMvc.perform(get(url))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl(expectedRedirectUrl));
  }

  @Test
  @DisplayName(
      "Позитивный тест - проверяем получение списка товаров с пагинацией и сортировкой из базы данных")
  void positiveTest_shouldGetItems() throws Exception {
    String url = "/main/items";
    String expectedView = "main";
    String pageNumber = "0";
    String pageSize = "10";
    String search = "";
    String sort = "ALPHA";

    mockMvc.perform(get(url)
                        .param("search", search)
                        .param("sort", sort)
                        .param("pageSize", pageSize)
                        .param("pageNumber", pageNumber))
           .andExpect(status().isOk())
           .andExpect(content().contentType("text/html;charset=UTF-8"))
           .andExpect(view().name(expectedView))
           .andExpect(model().attributeExists("items"))
           .andExpect(model().attribute("items", hasSize(3)))
           .andExpect(model().attributeExists("paging"))
           .andExpect(model().attributeExists("search"));
  }

  @Test
  @DisplayName("Позитивный тест - проверяем добавление товара в корзину с главной страницы")
  void positiveTest_shouldUpdateMainCartItems() throws Exception {
    String url = "/main/items";
    String expectedRedirectUrl = "/main/items";
    Item expectedItem = Data.getItemPlusCount();

    mockMvc.perform(post(url + "/" + ITEM_T_SHIRT_ID)
                        .param("action", ACTION))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl(expectedRedirectUrl));

    Optional<Item> actualItem = itemRepository.findById(ITEM_T_SHIRT_ID);

    assertTrue(actualItem.isPresent());
    assertEquals(expectedItem.getId(), actualItem.get().getId());
    assertEquals(expectedItem.getCount(), actualItem.get().getCount());
  }

  @Test
  @DisplayName("Позитивный тест - проверяем получение страницы корзины с товарами")
  void positiveTest_shouldGetCartItems() throws Exception {
    String url = "/cart/items";
    String expectedView = "cart";

    insertCartItemWithOneItem();

    mockMvc.perform(get(url))
           .andExpect(status().isOk())
           .andExpect(content().contentType("text/html;charset=UTF-8"))
           .andExpect(view().name(expectedView))
           .andExpect(model().attributeExists("items"))
           .andExpect(model().attribute("items", hasSize(1)))
           .andExpect(model().attributeExists("total"));
  }

  @Test
  @DisplayName("Позитивный тест - проверяем увеличение количества товара из корзины")
  void positiveTest_shouldUpdateCartItems() throws Exception {
    String url = "/cart/items";
    String expectedRedirectUrl = "/cart/items";
    Item expectedItem = Data.getItemPlusCount();

    mockMvc.perform(post(url + "/" + ITEM_T_SHIRT_ID)
                        .param("action", ACTION))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl(expectedRedirectUrl));

    Optional<Item> actualItem = itemRepository.findById(ITEM_T_SHIRT_ID);

    assertTrue(actualItem.isPresent());
    assertEquals(expectedItem.getId(), actualItem.get().getId());
    assertEquals(expectedItem.getCount(), actualItem.get().getCount());
  }

  @Test
  @DisplayName("Позитивный тест - проверяем получение страницы карточки товара")
  void positiveTest_shouldGetItemById() throws Exception {
    String url = "/items";
    String expectedView = "item";

    mockMvc.perform(get(url + "/" + ITEM_T_SHIRT_ID))
           .andExpect(status().isOk())
           .andExpect(content().contentType("text/html;charset=UTF-8"))
           .andExpect(view().name(expectedView))
           .andExpect(model().attributeExists("item"));
  }

  @Test
  @DisplayName("Позитивный тест - проверяем увеличение количества товара из карточки товара")
  void positiveTest_shouldUpdateItems() throws Exception {
    String url = "/items";
    String expectedRedirectUrl = url + "/" + ITEM_T_SHIRT_ID;
    Item expectedItem = Data.getItemPlusCount();

    mockMvc.perform(post(url + "/" + ITEM_T_SHIRT_ID)
                        .param("action", ACTION))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl(expectedRedirectUrl));

    Optional<Item> actualItem = itemRepository.findById(ITEM_T_SHIRT_ID);

    assertTrue(actualItem.isPresent());
    assertEquals(expectedItem.getId(), actualItem.get().getId());
    assertEquals(expectedItem.getCount(), actualItem.get().getCount());
  }

  @Test
  @DisplayName("Позитивный тест - формирования заказа из корзины")
  void positiveTest_shouldBuyItems() throws Exception {
    String url = "/buy";
    String expectedRedirectUrl = "/orders?orderId=" + ORDER_ID;
    Item item = Data.getItemPlusCount();

    insertCartItemWithOneItem();

    mockMvc.perform(post(url))
           .andExpect(model().attributeExists("orderId"))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl(expectedRedirectUrl));


    List<Order> actualOrders = orderRepository.findAll();

    assertEquals(1, actualOrders.size());
    assertEquals(ORDER_ID, actualOrders.get(0).getId());
    assertEquals(item.getTitle(), actualOrders.get(0).getItems().get(0).getTitle());
  }

  @Test
  @DisplayName("Позитивный тест - получение списка заказов")
  void positiveTest_shouldGetOrders() throws Exception {
    String url = "/orders";
    String expectedView = "orders";

    insertOrder();
    insertOrderItemWithOneItem();

    mockMvc.perform(get(url))
           .andExpect(status().isOk())
           .andExpect(content().contentType("text/html;charset=UTF-8"))
           .andExpect(view().name(expectedView))
           .andExpect(model().attributeExists("orders"))
           .andExpect(model().attribute("orders", hasSize(1)));

  }

  @Test
  @DisplayName("Позитивный тест - получение заказа по ID")
  void positiveTest_shouldGetOrderById() throws Exception {
    String url = "/orders";
    String expectedView = "order";

    mockMvc.perform(get(url + "/" + ORDER_ID))
           .andExpect(status().isOk())
           .andExpect(content().contentType("text/html;charset=UTF-8"))
           .andExpect(view().name(expectedView))
           .andExpect(model().attributeExists("order"));
  }

  private void insertCartItemWithOneItem() {
    CartItem cartItem = Data.getCartItem();

    String sqlCartItems = "INSERT INTO cart_item (cart_id, item_id, count)VALUES (?, ?, ?)";

    jdbcTemplate.update(sqlCartItems,
                        cartItem.getCartId(),
                        cartItem.getItemId(),
                        cartItem.getCount());
  }

  private void insertOrder() {
    Order order = Data.getOrder();

    String sqlOrder = "INSERT INTO orders (id, total_sum)VALUES (?, ?)";

    jdbcTemplate.update(sqlOrder,
                        order.getId(),
                        order.getTotalSum());
  }


  private void insertOrderItemWithOneItem() {
    OrderItem orderItem = Data.getOrderItem();

    String sqlOrderItems = "INSERT INTO order_item (order_id, item_id, count)VALUES (?, ?, ?)";

    jdbcTemplate.update(sqlOrderItems,
                        orderItem.getOrderId(),
                        orderItem.getItemId(),
                        orderItem.getCount());
  }
}