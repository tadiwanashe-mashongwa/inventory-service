package com.example.inventoryservice.repository;

import com.example.inventoryservice.entity.StockLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public  interface StockLevelRepository extends JpaRepository<StockLevel, Long> {
    Optional<StockLevel> findByPartId(String partId);
}