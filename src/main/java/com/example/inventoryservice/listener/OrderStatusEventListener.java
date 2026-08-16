package com.example.inventoryservice.listener;

import com.example.inventoryservice.dto.PaymentEvent;
import com.example.inventoryservice.service.InventoryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderStatusEventListener {

    private final InventoryService inventoryService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "payment-status-changed", groupId = "inventory-service-group")
    public void handlePaymentEvent(String payload) {
        PaymentEvent event;
        try {
            event = objectMapper.readValue(payload, PaymentEvent.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid payment status event", e);
        }
        log.info("Received payment event for order ID: {} with status: {}", event.orderId(), event.status());
        if ("SUCCESS".equalsIgnoreCase(event.status())) {
            inventoryService.confirmReservation(event.orderId());
        } else if ("FAILED".equalsIgnoreCase(event.status())) {
            inventoryService.cancelReservation(event.orderId());
        }
    }
}
