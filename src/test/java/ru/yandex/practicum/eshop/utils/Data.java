package ru.yandex.practicum.eshop.utils;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.eshop.dto.ItemDto;
import ru.yandex.practicum.eshop.entity.*;

@UtilityClass
public class Data {
    private static final Long ITEM_ID = 1L;
    private static final Long CART_ID = 1L;
    private static final Long ORDER_ID = 1L;
    private static final String ITEM_SHORTS_IMG_PATH = "/images/shorts.jpg";

    public Item getItemPlusCount() {
        return Item.builder()
                .id(ITEM_ID)
                .title("Шорты")
                .imgPath(ITEM_SHORTS_IMG_PATH)
                .description("Летние шорты карго")
                .price(1399.99)
                .count(1)
                .build();
    }

    public ItemDto getItemDto() {
        return ItemDto.builder()
                .id(ITEM_ID)
                .title("Шорты")
                .imgPath(ITEM_SHORTS_IMG_PATH)
                .description("Летние шорты карго")
                .price(1399.99)
                .count(1)
                .build();
    }

    public Order getOrder() {
        Item item = getItemPlusCount();

        return Order.builder()
                .id(ORDER_ID)
                .totalSum(item.getPrice())
                .build();
    }

    public Cart getCart() {
        Item item = getItemPlusCount();

        return Cart.builder()
                .id(CART_ID)
                .total(item.getPrice())
                .build();
    }

    public CartItem getCartItem() {
        return CartItem.builder()
                .cartId(CART_ID)
                .itemId(ITEM_ID)
                .count(1)
                .build();

    }

    public OrderItem getOrderItem() {
        return OrderItem.builder()
                .orderId(ORDER_ID)
                .itemId(ITEM_ID)
                .count(1)
                .build();
    }
}