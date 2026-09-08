package ru.yandex.practicum.order.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.order.dto.CreateOrderRequest;
import ru.yandex.practicum.order.dto.OrderItemRequest;
import ru.yandex.practicum.order.dto.proxy.inventory.ReserveRequest;
import ru.yandex.practicum.order.dto.proxy.product.ProductDto;
import ru.yandex.practicum.order.entity.Order;
import ru.yandex.practicum.order.exception.OrderProcessingException;
import ru.yandex.practicum.order.feign.InventoryClient;
import ru.yandex.practicum.order.feign.ProductClient;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DefaultOrderOrchestrationService implements OrderOrchestrationService {

    private final OrderService orderService;
    private final ProductClient productClient;
    private final InventoryClient inventoryClient;

    @Override
    public Order createOrder(CreateOrderRequest request) {
        Map<Long, Integer> items = request.items().stream()
                .collect(Collectors.groupingBy(OrderItemRequest::productId,
                        Collectors.summingInt(OrderItemRequest::quantity)));

        validateProducts(items);
        reserveItems(items);

        return orderService.saveOrder(request);
    }

    private void validateProducts(Map<Long, Integer> items) {
        items.forEach((productId, quantity) -> {
            try {
                ProductDto product = productClient.getProductById(productId);

                if (!product.active()) {
                    throw new OrderProcessingException("Товар снят с продажи");
                }
            } catch (FeignException e) {
                throw mapProductException(e, productId);
            }
        });
    }

    private void reserveItems(Map<Long, Integer> items) {
        Map<Long, Integer> bookedItems = new HashMap<>();
        items.forEach((productId, quantity) -> {
            try {
                inventoryClient.reserveStock(new ReserveRequest(productId, quantity));
                bookedItems.put(productId, quantity);
            } catch (FeignException e) {
                releaseBookedItems(bookedItems);
                throw mapInventoryException(e, productId);
            }
        });
    }

    private void releaseBookedItems(Map<Long, Integer> bookedItems) {
        bookedItems.forEach((productId, quantity) ->
                inventoryClient.releaseStock(new ReserveRequest(productId, quantity)));
    }

    private OrderProcessingException mapProductException(FeignException exception, Long productId) {
        if (exception.status() == 404) {
            return new OrderProcessingException(
                    "Товар с id=%d не найден".formatted(productId)
            );
        }

        return new OrderProcessingException(
                "Не удалось получить данные товара"
        );
    }

    private OrderProcessingException mapInventoryException(FeignException exception, Long productId) {
        if (exception.status() == 404) {
            return new OrderProcessingException(
                    "Складская запись для товара id=%d не найдена".formatted(productId)
            );
        }

        if (exception.status() == 409) {
            return new OrderProcessingException(
                    "Недостаточно товара id=%d на складе".formatted(productId)
            );
        }

        return new OrderProcessingException(
                "Не удалось зарезервировать товар"
        );
    }
}
