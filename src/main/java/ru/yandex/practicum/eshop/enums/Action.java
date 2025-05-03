package ru.yandex.practicum.eshop.enums;

import ch.qos.logback.core.joran.spi.ActionException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum для действий с товарами в корзине.
 */
@Getter
@RequiredArgsConstructor public enum Action {
  PLUS,
  MINUS,
  DELETE;

  public static Action getValueOf(String action) throws ActionException {
      for (Action type : Action.values()) {
        if (type.name().equalsIgnoreCase(action)) {
          return type;
        }
      } throw new ActionException("Некорректный тип действия с товаром");
  }
}