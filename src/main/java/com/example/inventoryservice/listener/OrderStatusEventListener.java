package com.example.inventoryservice.listener;

import com.example.inventoryservice.dto.PaymentEvent;
import com.example.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderStatusEventListener {

    private final InventoryService inventoryService;

    @KafkaListener(topics = "payment-status-changed", groupId = "inventory-service-group")
    public void handlePaymentEvent(PaymentEvent event) {
        log.info("Received payment event for order ID: {} with status: {}", event.orderId(), event.status());
        if ("SUCCESS".equalsIgnoreCase(event.status())) {
            inventoryService.confirmReservation(event.orderId());
        } else if ("FAILED".equalsIgnoreCase(event.status())) {
            inventoryService.cancelReservation(event.orderId());
        }
    }
}
