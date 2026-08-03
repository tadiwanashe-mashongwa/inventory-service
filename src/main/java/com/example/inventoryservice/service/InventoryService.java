package com.example.inventoryservice.service;

import com.example.inventoryservice.entity.Reservation;
import com.example.inventoryservice.entity.StockLevel;
import com.example.inventoryservice.repository.ReservationRepository;
import com.example.inventoryservice.repository.StockLevelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final StockLevelRepository stockLevelRepository;
    private final ReservationRepository reservationRepository;

    @Transactional
    public StockLevel addStock(String partId, int initialQuantity) {
        StockLevel stock = stockLevelRepository.findByPartId(partId)
                .orElse(StockLevel.builder()
                        .partId(partId)
                        .availableQuantity(0)
                        .reservedQuantity(0)
                        .version(0L)
                        .build());

        stock.setAvailableQuantity(stock.getAvailableQuantity() + initialQuantity);
        return stockLevelRepository.save(stock);
    }

    public int getAvailableStock(String partId) {
        return stockLevelRepository.findByPartId(partId)
                .map(StockLevel::getAvailableQuantity)
                .orElse(0);
    }

    @Transactional
    public boolean reserveStock(String orderId, String partId, int quantity) {
        StockLevel stock = stockLevelRepository.findByPartId(partId)
                .orElseThrow(() -> new IllegalArgumentException("Stock not found for part: " + partId));

        if (stock.getAvailableQuantity() < quantity) {
            return false;
        }

        stock.setAvailableQuantity(stock.getAvailableQuantity() - quantity);
        stock.setReservedQuantity(stock.getReservedQuantity() + quantity);
        stockLevelRepository.save(stock);

        Reservation reservation = Reservation.builder()
                .orderId(orderId)
                .partId(partId)
                .quantity(quantity)
                .status(Reservation.ReservationStatus.PENDING)
                .build();

        reservationRepository.save(reservation);

        return true;
    }

    @Transactional
    public void deductStockForBreakage(String partId, int quantity) {
        StockLevel stock = stockLevelRepository.findByPartId(partId)
                .orElseThrow(() -> new IllegalArgumentException("Stock not found for part: " + partId));

        if (stock.getAvailableQuantity() < quantity) {
            throw new IllegalArgumentException("Insufficient available stock to record breakage");
        }

        stock.setAvailableQuantity(stock.getAvailableQuantity() - quantity);
        stockLevelRepository.save(stock);
    }

    @Transactional
    public void confirmReservation(String orderId) {
        List<Reservation> reservations = reservationRepository.findByOrderId(orderId);
        for (Reservation reservation : reservations) {
            if (reservation.getStatus() == Reservation.ReservationStatus.PENDING) {
                reservation.setStatus(Reservation.ReservationStatus.CONFIRMED);
                reservationRepository.save(reservation);

                StockLevel stock = stockLevelRepository.findByPartId(reservation.getPartId())
                        .orElseThrow(() -> new IllegalArgumentException("Stock not found for part: " + reservation.getPartId()));

                stock.setReservedQuantity(stock.getReservedQuantity() - reservation.getQuantity());
                stockLevelRepository.save(stock);
            }
        }
    }

    @Transactional
    public void cancelReservation(String orderId) {
        List<Reservation> reservations = reservationRepository.findByOrderId(orderId);
        for (Reservation reservation : reservations) {
            if (reservation.getStatus() == Reservation.ReservationStatus.PENDING) {
                reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
                reservationRepository.save(reservation);

                StockLevel stock = stockLevelRepository.findByPartId(reservation.getPartId())
                        .orElseThrow(() -> new IllegalArgumentException("Stock not found for part: " + reservation.getPartId()));

                stock.setAvailableQuantity(stock.getAvailableQuantity() + reservation.getQuantity());
                stock.setReservedQuantity(stock.getReservedQuantity() - reservation.getQuantity());
                stockLevelRepository.save(stock);
            }
        }
    }
}