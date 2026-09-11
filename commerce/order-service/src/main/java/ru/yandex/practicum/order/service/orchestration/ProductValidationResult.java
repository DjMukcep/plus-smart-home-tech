package ru.yandex.practicum.order.service.orchestration;

import ru.yandex.practicum.order.dto.OrderItemRequest;

import java.util.List;

public record ProductValidationResult(List<OrderItemRequest> items, boolean pendingConfirmation) {
}
