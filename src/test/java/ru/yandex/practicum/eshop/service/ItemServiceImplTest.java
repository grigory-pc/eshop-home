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
import ru.yandex.practicum.eshop.dto.CartDto;
import ru.yandex.practicum.eshop.dto.ItemDto;
import ru.yandex.practicum.eshop.dto.OrderDto;
import ru.yandex.practicum.eshop.entity.*;
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
        CartItem cartItem = Data.getCartItem();
        Item item = Data.getItemPlusCount();
        ItemDto itemDto = Data.getItemDto();
        CartDto expectedCartDto = Data.getCartDto();

        doReturn(List.of(cartItem))
                .when(cartItemRepository).findCartItemsByCartId(anyLong());
        doReturn(List.of(item))
                .when(itemRepository).findAllById(anyList());
        doReturn(List.of(itemDto))
                .when(itemMapper).toListDto(anyList());

        CartDto actualCartDto = itemService.getCartItems();

        assertEquals(expectedCartDto, actualCartDto);
        verify(cartItemRepository, atLeastOnce()).findCartItemsByCartId(anyLong());
        verify(itemRepository, atLeastOnce()).findAllById(anyList());
        verify(itemMapper, atLeastOnce()).toListDto(anyList());
    }

    @Test
    @DisplayName("Позитивный тест - проверяем получение данных товара")
    void positiveTest_shouldGetItem() {
        Item item = Data.getItemPlusCount();
        ItemDto itemDto = Data.getItemDto();

        doReturn(item)
                .when(itemRepository).getReferenceById(anyLong());
        doReturn(itemDto)
                .when(itemMapper).toDto(any(Item.class));

        ItemDto actualItemDto = itemService.getItem(ITEM_ID);

        assertEquals(itemDto, actualItemDto);
        verify(itemRepository, atLeastOnce()).getReferenceById(anyLong());
        verify(itemMapper, atLeastOnce()).toDto(any(Item.class));
    }

    @Test
    @DisplayName("Позитивный тест - проверяем формирование заказа из корзины с товарами")
    void positiveTest_shouldBuyItems() {
        Cart cart = Data.getCart();
        Order order = Data.getOrder();

        doReturn(cart)
                .when(cartRepository).getReferenceById(anyLong());
        doReturn(order)
                .when(orderRepository).save(any(Order.class));
        doReturn(List.of(order))
                .when(orderItemRepository).saveAll(anyList());
        doReturn(cart)
                .when(cartRepository).save(any(Cart.class));
        doNothing().when(cartItemRepository)
                .deleteAllByCartId(anyLong());
        doNothing().when(itemRepository)
                .updateAllCountToZero();

        Long actualOrderId = itemService.buyItems();

        assertEquals(order.getId(), actualOrderId);
        verify(cartRepository, atLeastOnce()).getReferenceById(anyLong());
        verify(orderRepository, atLeastOnce()).save(any(Order.class));
        verify(orderItemRepository, atLeastOnce()).saveAll(anyList());
        verify(cartRepository, atLeastOnce()).save(any(Cart.class));
        verify(cartItemRepository, atLeastOnce()).deleteAllByCartId(anyLong());
        verify(itemRepository, atLeastOnce()).updateAllCountToZero();
    }

    @Test
    @DisplayName("Позитивный тест - проверяем получение списка заказов")
    void positiveTest_shouldGetOrders() {
        Order order = Data.getOrder();
        OrderDto orderDto = Data.getOrderDto();
        Item item = Data.getItemPlusCount();
        ItemDto itemDto = Data.getItemDto();
        OrderItem orderItem = Data.getOrderItem();
        OrderDto expectedOrderDto = Data.getOrderDto();

        doReturn(List.of(order))
                .when(orderRepository).findAll();
        doReturn(List.of(orderDto))
                .when(itemMapper).toListDto(anyList());
        doReturn(List.of(orderItem))
                .when(orderItemRepository).findOrderItemsByOrderId(anyLong());
        doReturn(List.of(item))
                .when(itemRepository).findAllById(anySet());
        doReturn(List.of(itemDto))
                .when(itemMapper).toListDto(anyList());

        List<OrderDto> actualOrdersDto = itemService.getOrders();

        assertEquals(List.of(expectedOrderDto), actualOrdersDto);
        verify(orderRepository, atLeastOnce()).findAll();
        verify(itemMapper, atLeastOnce()).toListDto(anyList());
        verify(orderItemRepository, atLeastOnce()).findOrderItemsByOrderId(anyLong());
        verify(itemRepository, atLeastOnce()).findAllById(anySet());
        verify(itemMapper, atLeastOnce()).toListDto(anyList());
    }

    @Test
    @DisplayName("Позитивный тест - проверяем получение данных заказа")
    void positiveTest_shouldGetOrderItems() {
        Item item = Data.getItemPlusCount();
        ItemDto itemDto = Data.getItemDto();
        OrderItem orderItem = Data.getOrderItem();
        OrderDto expectedOrderDto = Data.getOrderDto();

        doReturn(List.of(orderItem))
                .when(orderItemRepository).findOrderItemsByOrderId(anyLong());
        doReturn(List.of(item))
                .when(itemRepository).findAllById(anySet());
        doReturn(List.of(itemDto))
                .when(itemMapper).toListDto(anyList());

        OrderDto actualOrderDto = itemService.getOrderItems(ORDER_ID);

        assertEquals(expectedOrderDto, actualOrderDto);
        verify(orderItemRepository, atLeastOnce()).findOrderItemsByOrderId(anyLong());
        verify(itemRepository, atLeastOnce()).findAllById(anySet());
        verify(itemMapper, atLeastOnce()).toListDto(anyList());
    }
}