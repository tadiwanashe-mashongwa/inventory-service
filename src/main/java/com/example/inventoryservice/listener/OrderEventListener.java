package com.example.inventoryservice.listener;

import com.example.inventoryservice.dto.OrderCreatedEvent;
import com.example.inventoryservice.dto.StockReservedEvent;
import com.example.inventoryservice.producer.StockReservationEventProducer;
import com.example.inventoryservice.service.InventoryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final InventoryService inventoryService;
    private final StockReservationEventProducer stockReservationEventProducer;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "order-created", groupId = "inventory-service-group")
    public void handleOrderCreated(String payload) {
        OrderCreatedEvent event;
        try {
            event = objectMapper.readValue(payload, OrderCreatedEvent.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid order created event", e);
        }
        log.info("Received order created event for order ID: {}", event.orderId());
        boolean allReserved = event.items().stream().allMatch(item -> {
            boolean reserved = inventoryService.reserveStock(
                    event.orderId().toString(),
                    item.partId().toString(),
                    item.quantity()
            );
            if (!reserved) {
                log.warn("Failed to reserve stock for order ID: {} and part ID: {} due to insufficient quantity",
                        event.orderId(), item.partId());
            }
            return reserved;
        });
        if (allReserved) stockReservationEventProducer.publish(new StockReservedEvent(event.orderId()));
    }
}
