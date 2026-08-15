package com.example.inventoryservice.dto;

import java.util.UUID;

public record OrderItemEvent(
        UUID partId,
        Integer quantity
) {}
