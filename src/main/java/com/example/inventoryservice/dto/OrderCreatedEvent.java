package com.example.inventoryservice.dto;

import java.util.List;
import java.util.UUID;

public record OrderCreatedEvent(
        UUID orderId,
        UUID customerId,
        List<OrderItemEvent> items
) {}
