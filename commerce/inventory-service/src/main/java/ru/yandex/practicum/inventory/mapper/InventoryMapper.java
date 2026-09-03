package ru.yandex.practicum.inventory.mapper;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.inventory.dto.InventoryDto;
import ru.yandex.practicum.inventory.dto.ReserveRequest;
import ru.yandex.practicum.inventory.entity.Inventory;

import java.util.List;

@UtilityClass
public class InventoryMapper {

    // только под создание записи!
    public static Inventory toEntity(ReserveRequest request) {
        return Inventory.builder()
                .productId(request.productId())
                .quantity(request.quantity())
                .reservedQuantity(0)
                .build();
    }

    public static InventoryDto toDto(Inventory inventory) {
        return  new InventoryDto(
                inventory.getId(),
                inventory.getProductId(),
                inventory.getQuantity(),
                inventory.getReservedQuantity(),
                inventory.getQuantity() - inventory.getReservedQuantity()
        );
    }

    public static List<InventoryDto> toDto(List<Inventory> inventory) {
        return inventory.stream()
                .map(InventoryMapper::toDto)
                .toList();
    }
}
