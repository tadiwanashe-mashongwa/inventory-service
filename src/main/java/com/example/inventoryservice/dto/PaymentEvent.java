package com.example.inventoryservice.dto;

public record PaymentEvent(
        String orderId,
        String status
) {}