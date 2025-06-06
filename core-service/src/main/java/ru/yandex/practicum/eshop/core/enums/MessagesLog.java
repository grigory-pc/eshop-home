package ru.yandex.practicum.eshop.core.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MessagesLog {
  MESSAGE_LOG_DB_GET_REQUEST("Отправляем запрос в БД для получения данных"),
  MESSAGE_LOG_DB_SAVE_REQUEST("Отправляем запрос в БД для сохранения(обновления) данных"),
  MESSAGE_LOG_DB_RESPONSE_ERROR(
      "Возникла непредвиденная ситуация во время получения(отправки) данных из БД"),
  MESSAGE_LOG_ITEMS_SIZE("Получен ответ от БД со списком товаров размером = {}"),
  MESSAGE_LOG_FLUSH_CART("Очистка корзины и количества товаров после размещения заказа"),
  MESSAGE_LOG_FLUSH_CART_SUCCESS("Очистка корзины и количества товаров успешно выполнена");

  private final String message;
}
