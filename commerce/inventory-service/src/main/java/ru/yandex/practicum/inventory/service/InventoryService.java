package ru.yandex.practicum.inventory.service;

import ru.yandex.practicum.inventory.dto.ReserveRequest;
import ru.yandex.practicum.inventory.dto.ReserveResponse;
import ru.yandex.practicum.inventory.dto.UpdateInventoryRequest;
import ru.yandex.practicum.inventory.entity.Inventory;

import java.util.List;

public interface InventoryService {

    Inventory addInventoryRecord(ReserveRequest  request);

    Inventory updateInventoryRecord(UpdateInventoryRequest request);

    ReserveResponse reserveInventoryRecord(ReserveRequest request);

    Inventory getInventoryRecordByProductId(Long id);

    List<Inventory> getInventoryRecords();
}
