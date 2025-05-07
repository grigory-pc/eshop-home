package ru.yandex.practicum.eshop.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.yandex.practicum.eshop.dto.ItemDto;
import ru.yandex.practicum.eshop.entity.Cart;
import ru.yandex.practicum.eshop.entity.CartItem;
import ru.yandex.practicum.eshop.entity.Item;
import ru.yandex.practicum.eshop.enums.Sorting;
import ru.yandex.practicum.eshop.mappers.ItemMapper;
import ru.yandex.practicum.eshop.repository.*;
import ru.yandex.practicum.eshop.utils.Data;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.atLeastOnce;


@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ItemServiceImpl.class)
class ItemServiceImplTest {
    public static final String MESSAGE_FAIL = "Не ожидали получить исключение";
    private static final Long ITEM_ID = 1L;
    private static final Long CART_ID = 1L;
    private static final Long ORDER_ID = 1L;

    @MockBean
    private ItemMapper itemMapper;
    @MockBean
    private ItemRepository itemRepository;
    @MockBean
    private CartRepository cartRepository;
    @MockBean
    private OrderRepository orderRepository;
    @MockBean
    private CartItemRepository cartItemRepository;
    @MockBean
    private OrderItemRepository orderItemRepository;
    @Autowired
    private ItemServiceImpl itemService;

    @Test
    @DisplayName("Позитивный тест - проверяем получение списка товаров")
    void positiveTest_shouldGetItems() {
        try {
            String search = "";
            Sorting sort = Sorting.ALPHA;
            int pageNumber = 0;
            int pageSize = 10;

            Page<Item> itemPage = new PageImpl<>(List.of(Data.getItemPlusCount()));
            ItemDto expectedItemDto = Data.getItemDto();

            doReturn(itemPage)
                    .when(itemRepository).findAll(any(Pageable.class));
            doReturn(new PageImpl<>(List.of(expectedItemDto)))
                    .when(itemMapper).toDtoPage(any());

            Page<ItemDto> actualItemDto = itemService.getItems(search, sort, pageNumber, pageSize);

            assertEquals(1, actualItemDto.getTotalElements());
            assertEquals(expectedItemDto, actualItemDto.getContent().get(0));

        } catch (Exception e) {
            fail(MESSAGE_FAIL);
        }
    }

    @Test
    @DisplayName("Позитивный тест - проверяем изменение(увеличение) количества товаров в корзине")
    void positiveTest_shouldEditCart() {
        try {
            String action = "plus";
            Cart cart = Data.getCart();
            CartItem cartItem = Data.getCartItem();

            doReturn(cart)
                    .when(cartRepository).getReferenceById(anyLong());
            doReturn(Optional.of(cartItem))
                    .when(cartItemRepository).findCartItemByCartIdAndItemId(anyLong(), anyLong());
            doReturn(cartItem)
                    .when(cartItemRepository).save(any(CartItem.class));
            doNothing().when(itemRepository)
                    .incrementCount(anyLong());
            doReturn(cart)
                    .when(cartRepository).save(any(Cart.class));

            itemService.editCart(ITEM_ID, action);

            verify(cartRepository, atLeastOnce()).getReferenceById(anyLong());
            verify(cartItemRepository, atLeastOnce()).findCartItemByCartIdAndItemId(anyLong(), anyLong());
            verify(cartItemRepository, atLeastOnce()).save(any(CartItem.class));
            verify(itemRepository, atLeastOnce()).incrementCount(anyLong());
            verify(cartRepository, atLeastOnce()).save(any(Cart.class));

        } catch (Exception e) {
            fail(MESSAGE_FAIL);
        }
    }

    @Test
    @DisplayName("Позитивный тест - проверяем получение корзины с товарами")
    void positiveTest_shouldGetCartItems() {
    }

    @Test
    @DisplayName("Позитивный тест - проверяем получение данных товара")
    void positiveTest_shouldGetItem() {
    }

    @Test
    @DisplayName("Позитивный тест - проверяем формирование заказа из корзины с товарами")
    void positiveTest_shouldBuyItems() {
    }

    @Test
    @DisplayName("Позитивный тест - проверяем получение списка заказов")
    void positiveTest_shouldGetOrders() {
    }

    @Test
    @DisplayName("Позитивный тест - проверяем получение данных заказа")
    void positiveTest_shouldGetOrderItems() {
    }
}