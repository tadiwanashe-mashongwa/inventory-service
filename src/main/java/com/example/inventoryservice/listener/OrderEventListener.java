package com.example.inventoryservice.listener;

import com.example.inventoryservice.dto.OrderCreatedEvent;
import com.example.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final InventoryService inventoryService;

    @KafkaListener(topics = "order-created-topic", groupId = "inventory-service-group")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Received order created event for order ID: {} and part ID: {}", event.orderId(), event.partId());
        boolean reserved = inventoryService.reserveStock(event.orderId(), event.partId(), event.quantity());
        if (!reserved) {
            log.warn("Failed to reserve stock for order ID: {} due to insufficient quantity", event.orderId());
        }
    }
}