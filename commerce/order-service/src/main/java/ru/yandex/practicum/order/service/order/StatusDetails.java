package ru.yandex.practicum.order.service.order;

import ru.yandex.practicum.order.entity.OrderStatus;

public record StatusDetails(OrderStatus status, String message) {
}
