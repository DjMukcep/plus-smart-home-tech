package ru.yandex.practicum.inventory.exception;

public class CancellationQuantityExceededException extends RuntimeException {

    public CancellationQuantityExceededException(String message) {
        super(message);
    }
}
