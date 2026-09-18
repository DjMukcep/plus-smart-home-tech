package ru.yandex.practicum.order.exception;


public class ProductServiceUnavailableException extends RuntimeException {

    public ProductServiceUnavailableException(Long productId, Throwable cause) {
        super("Product service is unavailable for product id=" + productId, cause);
    }
}
