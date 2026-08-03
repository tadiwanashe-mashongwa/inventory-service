package com.example.inventoryservice.listener;

import com.example.inventoryservice.dto.PaymentEvent;
import com.example.inventoryservice.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderStatusEventListenerTest {

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private OrderStatusEventListener orderStatusEventListener;

    @Test
    void shouldConfirmReservationOnSuccessPayment() {
        PaymentEvent event = new PaymentEvent("ORDER-123", "SUCCESS");

        orderStatusEventListener.handlePaymentEvent(event);

        verify(inventoryService, times(1)).confirmReservation("ORDER-123");
        verify(inventoryService, never()).cancelReservation(anyString());
    }

    @Test
    void shouldCancelReservationOnFailedPayment() {
        PaymentEvent event = new PaymentEvent("ORDER-123", "FAILED");

        orderStatusEventListener.handlePaymentEvent(event);

        verify(inventoryService, times(1)).cancelReservation("ORDER-123");
        verify(inventoryService, never()).confirmReservation(anyString());
    }
}