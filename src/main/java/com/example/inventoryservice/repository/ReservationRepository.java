package com.example.inventoryservice.repository;

import com.example.inventoryservice.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByOrderId(String orderId);

    boolean existsByOrderIdAndPartId(String orderId, String partId);
}
