package com.example.inventoryservice.service;

import com.example.inventoryservice.entity.Reservation;
import com.example.inventoryservice.entity.StockLevel;
import com.example.inventoryservice.repository.ReservationRepository;
import com.example.inventoryservice.repository.StockLevelRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private StockLevelRepository stockLevelRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void shouldReserveStockSuccessfully() {
        StockLevel stock = StockLevel.builder()
                .id(1L)
                .partId("PART-123")
                .availableQuantity(10)
                .reservedQuantity(0)
                .version(0L)
                .build();

        when(stockLevelRepository.findByPartId("PART-123")).thenReturn(Optional.of(stock));
        when(stockLevelRepository.save(any(StockLevel.class))).thenReturn(stock);

        boolean result = inventoryService.reserveStock("ORDER-999", "PART-123", 2);

        assertTrue(result);
        assertEquals(8, stock.getAvailableQuantity());
        assertEquals(2, stock.getReservedQuantity());
        verify(reservationRepository, times(1)).save(any());
    }

    @Test
    void shouldNotReserveStockTwiceForTheSameOrderItem() {
        StockLevel stock = StockLevel.builder()
                .id(1L)
                .partId("PART-123")
                .availableQuantity(8)
                .reservedQuantity(2)
                .version(0L)
                .build();

        when(reservationRepository.existsByOrderIdAndPartId("ORDER-999", "PART-123"))
                .thenReturn(true);

        boolean result = inventoryService.reserveStock("ORDER-999", "PART-123", 2);

        assertTrue(result);
        assertEquals(8, stock.getAvailableQuantity());
        assertEquals(2, stock.getReservedQuantity());
        verifyNoInteractions(stockLevelRepository);
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void shouldFailWhenStockIsInsufficient() {
        StockLevel stock = StockLevel.builder()
                .id(1L)
                .partId("PART-123")
                .availableQuantity(1)
                .reservedQuantity(0)
                .version(0L)
                .build();

        when(stockLevelRepository.findByPartId("PART-123")).thenReturn(Optional.of(stock));

        boolean result = inventoryService.reserveStock("ORDER-999", "PART-123", 5);

        assertFalse(result);
        assertEquals(1, stock.getAvailableQuantity());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void shouldAddStockSuccessfully() {
        when(stockLevelRepository.findByPartId("PART-123")).thenReturn(Optional.empty());
        when(stockLevelRepository.save(any(StockLevel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StockLevel stock = inventoryService.addStock("PART-123", 50);

        assertNotNull(stock);
        assertEquals(50, stock.getAvailableQuantity());
        assertEquals(0, stock.getReservedQuantity());
    }

    @Test
    void shouldDeductStockForBreakageSuccessfully() {
        StockLevel stock = StockLevel.builder()
                .id(1L)
                .partId("PART-123")
                .availableQuantity(20)
                .reservedQuantity(0)
                .version(0L)
                .build();

        when(stockLevelRepository.findByPartId("PART-123")).thenReturn(Optional.of(stock));

        inventoryService.deductStockForBreakage("PART-123", 5);

        assertEquals(15, stock.getAvailableQuantity());
        verify(stockLevelRepository, times(1)).save(stock);
    }

    @Test
    void shouldConfirmReservationSuccessfully() {
        Reservation reservation = Reservation.builder()
                .id(1L)
                .orderId("ORDER-999")
                .partId("PART-123")
                .quantity(3)
                .status(Reservation.ReservationStatus.PENDING)
                .build();

        StockLevel stock = StockLevel.builder()
                .id(1L)
                .partId("PART-123")
                .availableQuantity(10)
                .reservedQuantity(3)
                .version(0L)
                .build();

        when(reservationRepository.findByOrderId("ORDER-999")).thenReturn(List.of(reservation));
        when(stockLevelRepository.findByPartId("PART-123")).thenReturn(Optional.of(stock));

        inventoryService.confirmReservation("ORDER-999");

        assertEquals(Reservation.ReservationStatus.CONFIRMED, reservation.getStatus());
        assertEquals(0, stock.getReservedQuantity());
        verify(reservationRepository, times(1)).save(reservation);
        verify(stockLevelRepository, times(1)).save(stock);
    }

    @Test
    void shouldCancelReservationSuccessfully() {
        Reservation reservation = Reservation.builder()
                .id(1L)
                .orderId("ORDER-999")
                .partId("PART-123")
                .quantity(3)
                .status(Reservation.ReservationStatus.PENDING)
                .build();

        StockLevel stock = StockLevel.builder()
                .id(1L)
                .partId("PART-123")
                .availableQuantity(7)
                .reservedQuantity(3)
                .version(0L)
                .build();

        when(reservationRepository.findByOrderId("ORDER-999")).thenReturn(List.of(reservation));
        when(stockLevelRepository.findByPartId("PART-123")).thenReturn(Optional.of(stock));

        inventoryService.cancelReservation("ORDER-999");

        assertEquals(Reservation.ReservationStatus.CANCELLED, reservation.getStatus());
        assertEquals(10, stock.getAvailableQuantity());
        assertEquals(0, stock.getReservedQuantity());
        verify(reservationRepository, times(1)).save(reservation);
        verify(stockLevelRepository, times(1)).save(stock);
    }
}
