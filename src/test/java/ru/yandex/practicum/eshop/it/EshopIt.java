package ru.yandex.practicum.eshop.it;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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
import ru.yandex.practicum.eshop.repository.CartItemRepository;
import ru.yandex.practicum.eshop.repository.CartRepository;
import ru.yandex.practicum.eshop.repository.ItemRepository;
import ru.yandex.practicum.eshop.repository.OrderItemRepository;
import ru.yandex.practicum.eshop.repository.OrderRepository;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class EshopIt {
  @Autowired
  private WebApplicationContext webApplicationContext;
  @Autowired
  private JdbcTemplate jdbcTemplate;
  @Autowired
  private ItemRepository itemRepository;
  @Autowired
  private CartRepository cartRepository;
  @Autowired
  private OrderRepository orderRepository;
  @Autowired
  private CartItemRepository cartItemRepository;
  @Autowired
  private OrderItemRepository orderItemRepository;

  private MockMvc mockMvc;

  @BeforeAll
  public static void initTables(@Autowired DataSource dataSource) {
    try (Connection conn = dataSource.getConnection()) {
      ScriptUtils.executeSqlScript(conn, new ClassPathResource("/schema.sql"));
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void redirectToMainItems() {
  }

  @Test
  void getItems() {
  }

  @Test
  void updateMainCartItems() {
  }

  @Test
  void getCartItems() {
  }

  @Test
  void updateCartItems() {
  }

  @Test
  void getItemById() {
  }

  @Test
  void updateItems() {
  }

  @Test
  void buyItems() {
  }

  @Test
  void getOrders() {
  }

  @Test
  void getOrderById() {
  }
}