package com.example.inventoryservice.listener;

import com.example.inventoryservice.dto.OrderCreatedEvent;
import com.example.inventoryservice.dto.OrderItemEvent;
import com.example.inventoryservice.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class OrderCreatedEventListenerTest {

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private OrderEventListener orderEventListener;

    @Test
    void shouldReserveEachItemFromAnOrderCreatedEvent() {
        UUID orderId = UUID.randomUUID();
        UUID firstPartId = UUID.randomUUID();
        UUID secondPartId = UUID.randomUUID();
        OrderCreatedEvent event = new OrderCreatedEvent(
                orderId,
                UUID.randomUUID(),
                List.of(
                        new OrderItemEvent(firstPartId, 2),
                        new OrderItemEvent(secondPartId, 1)
                )
        );

        orderEventListener.handleOrderCreated(event);

        verify(inventoryService).reserveStock(orderId.toString(), firstPartId.toString(), 2);
        verify(inventoryService).reserveStock(orderId.toString(), secondPartId.toString(), 1);
    }

    @Test
    void shouldNotReserveStockWhenAnOrderCreatedEventHasNoItems() {
        OrderCreatedEvent event = new OrderCreatedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                List.of()
        );

        orderEventListener.handleOrderCreated(event);

        verifyNoInteractions(inventoryService);
    }
}
