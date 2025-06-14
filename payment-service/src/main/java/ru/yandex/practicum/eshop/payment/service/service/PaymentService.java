package ru.yandex.practicum.eshop.payment.service.service;

import reactor.core.publisher.Mono;

/**
 * Сервис для работы с товарами.
 */
public interface PaymentService {
  /**
   * Формирование заказа для товаров в корзине.
   *
   * @return id заказа.
   */
  Mono<Long> buyItems();
}