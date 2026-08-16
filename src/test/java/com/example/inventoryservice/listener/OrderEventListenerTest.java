package com.example.inventoryservice.listener;

import com.example.inventoryservice.service.InventoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderStatusEventListenerTest {

    @Mock
    private InventoryService inventoryService;

    @Test
    void shouldConfirmReservationFromPaymentStatusJsonWithMetadata() {
        OrderStatusEventListener orderStatusEventListener = new OrderStatusEventListener(inventoryService, new ObjectMapper());

        orderStatusEventListener.handlePaymentEvent("""
                {"paymentId":"payment-123","orderId":"ORDER-123","status":"SUCCESS"}
                """);

        verify(inventoryService, times(1)).confirmReservation("ORDER-123");
        verify(inventoryService, never()).cancelReservation(anyString());
    }

    @Test
    void shouldCancelReservationFromFailedPaymentStatusJsonWithMetadata() {
        OrderStatusEventListener orderStatusEventListener = new OrderStatusEventListener(inventoryService, new ObjectMapper());

        orderStatusEventListener.handlePaymentEvent("""
                {"paymentId":"payment-123","orderId":"ORDER-123","status":"FAILED"}
                """);

        verify(inventoryService, times(1)).cancelReservation("ORDER-123");
        verify(inventoryService, never()).confirmReservation(anyString());
    }
}
