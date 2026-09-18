package ru.yandex.practicum.order.dto.proxy.inventory;

public record ReserveResponse(boolean success, Integer availableQuantity, String message) {
}
