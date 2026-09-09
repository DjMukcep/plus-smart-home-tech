package ru.yandex.practicum.order.exception;

public class InventoryServiceUnavailableException extends RuntimeException {

    public InventoryServiceUnavailableException(Long productId, Throwable cause) {
        super("Inventory service is unavailable for product id=" + productId, cause);
    }
}
