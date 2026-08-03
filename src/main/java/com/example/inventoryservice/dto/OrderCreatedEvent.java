package com.example.inventoryservice.dto;

public record OrderCreatedEvent(
        String orderId,
        String partId,
        Integer quantity
) {}