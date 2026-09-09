package ru.yandex.practicum.order.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.order.dto.CreateOrderRequest;
import ru.yandex.practicum.order.dto.OrderItemRequest;
import ru.yandex.practicum.order.dto.proxy.inventory.ReserveRequest;
import ru.yandex.practicum.order.dto.proxy.product.ProductDto;
import ru.yandex.practicum.order.entity.Order;
import ru.yandex.practicum.order.entity.OrderStatus;
import ru.yandex.practicum.order.exception.InventoryServiceUnavailableException;
import ru.yandex.practicum.order.exception.OrderProcessingException;
import ru.yandex.practicum.order.exception.ProductServiceUnavailableException;
import ru.yandex.practicum.order.feign.InventoryClient;
import ru.yandex.practicum.order.feign.ProductClient;
import ru.yandex.practicum.order.service.fallback.ServiceCallResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

            ProductValidationResult validationResult = validateProducts(items);
            boolean invServiceDegraded = reserveItems(items);
            OrderStatus status = (validationResult.pendingConfirmation() || invServiceDegraded)
                    ? OrderStatus.PENDING_CONFIRMATION
                    : OrderStatus.CONFIRMED;
            CreateOrderRequest orderRequest = getFullOrderRequest(validationResult.items(),request);

            return orderService.saveOrder(orderRequest, status);
        }

        private CreateOrderRequest getFullOrderRequest(List<OrderItemRequest> items,
                                                       CreateOrderRequest orderRequest) {
            return new CreateOrderRequest(orderRequest.customerName(), orderRequest.customerEmail(), items);
        }

        private ProductValidationResult validateProducts(Map<Long, Integer> items) {
            List<OrderItemRequest> itemRequests = new ArrayList<>();
            boolean pendingConfirmation = false;

            for (Map.Entry<Long, Integer> entry : items.entrySet()) {
                Long productId = entry.getKey();
                int quantity = entry.getValue();
                ServiceCallResult<ProductDto> result = getProduct(productId);

                if (result instanceof ServiceCallResult.Degraded<ProductDto>) {
                    pendingConfirmation = true;
                }

                processProductResult(result, itemRequests, productId, quantity);
            }

            return new ProductValidationResult(itemRequests, pendingConfirmation);
        }

        private ServiceCallResult<ProductDto> getProduct(Long productId) {
            try {
                return new ServiceCallResult.Success<>(
                        productClient.getProductById(productId)
                );
            } catch (ProductServiceUnavailableException exception) {
                return new ServiceCallResult.Degraded<>(
                        "Каталог временно недоступен"
                );
            } catch (FeignException exception) {
                if (exception.status() == HttpStatus.NOT_FOUND.value()) {
                    return new ServiceCallResult.Failure<>(
                            "Товар с id=%d не найден".formatted(productId)
                    );
                }

                return new ServiceCallResult.Failure<>(
                        "Не удалось получить данные товара"
                );
            }
        }

        private void processProductResult(
                ServiceCallResult<ProductDto> result,
                List<OrderItemRequest> itemRequests,
                Long productId,
                int quantity) {

            switch (result) {
                case ServiceCallResult.Success<ProductDto> success -> {
                    ProductDto product = success.value();
                    itemRequests.add(processProduct(product, quantity));
                }
                case ServiceCallResult.Degraded<ProductDto> ignored ->
                        itemRequests.add(new OrderItemRequest(
                                productId,
                                "Товар #%d (ожидает проверки)".formatted(productId),
                                quantity,
                                BigDecimal.ZERO));
                case ServiceCallResult.Failure<ProductDto> failure ->
                        throw new OrderProcessingException(failure.message());
            }
        }

        private OrderItemRequest processProduct(ProductDto product, int quantity) {
            if (!product.active()) {
                throw new OrderProcessingException("Товар снят с продажи");
            }

            return getOrderItemRequest(product, quantity);
        }

        private OrderItemRequest getOrderItemRequest(ProductDto productDto, Integer quantity) {
            return new OrderItemRequest(productDto.id(), productDto.name(), quantity, productDto.price());
        }

    private boolean reserveItems(Map<Long, Integer> items) {
        Map<Long, Integer> bookedItems = new HashMap<>();
        boolean pendingConfirmation = false;

        for (Map.Entry<Long, Integer> entry : items.entrySet()) {
            Long productId = entry.getKey();
            int quantity = entry.getValue();

            ServiceCallResult<Void> result = processReserveStock(productId, quantity);

            if (result instanceof ServiceCallResult.Degraded<Void>) {
                pendingConfirmation = true;
            }

            processInventoryResult(result,bookedItems,productId, quantity);
        }
        return pendingConfirmation;
    }

    private void processInventoryResult(
            ServiceCallResult<Void> result, Map<Long, Integer> items, Long productId, int quantity) {
        switch (result) {
            case ServiceCallResult.Success<Void> ignored -> items.put(productId, quantity);
            case ServiceCallResult.Degraded<Void> ignored -> {}
            case ServiceCallResult.Failure<Void> failure -> {
                releaseBookedItems(items);
                throw new OrderProcessingException(failure.message());
            }
        }
    }

    private ServiceCallResult<Void> processReserveStock(Long productId, int quantity) {
        try {
            inventoryClient.reserveStock(new ReserveRequest(productId, quantity));

            return new ServiceCallResult.Success<>(null);

        } catch (InventoryServiceUnavailableException exception) {
            return new ServiceCallResult.Degraded<>(
                    "Склад временно недоступен"
            );
        } catch (FeignException exception) {
            if (exception.status() == HttpStatus.NOT_FOUND.value()) {
                return new ServiceCallResult.Failure<>(
                        "Складская запись для товара id=%d не найдена".formatted(productId)
                );
            }
            if (exception.status() == HttpStatus.CONFLICT.value()) {
                return new ServiceCallResult.Failure<>(
                        "Недостаточно товара id=%d на складе".formatted(productId)
                );
            }

            return new ServiceCallResult.Failure<>("Не удалось зарезервировать товар");
        }
    }

    private void releaseBookedItems(Map<Long, Integer> bookedItems) {
        bookedItems.forEach((productId, quantity) ->
                inventoryClient.releaseStock(new ReserveRequest(productId, quantity)));
    }
}
