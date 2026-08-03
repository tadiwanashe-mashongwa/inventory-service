package com.example.inventoryservice.controller;

import com.example.inventoryservice.entity.StockLevel;
import com.example.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/stock")
    public ResponseEntity<StockLevel> addStock(@RequestParam String partId, @RequestParam int quantity) {
        StockLevel updatedStock = inventoryService.addStock(partId, quantity);
        return ResponseEntity.status(HttpStatus.CREATED).body(updatedStock);
    }

    @GetMapping("/stock/{partId}")
    public ResponseEntity<Integer> getAvailableStock(@PathVariable String partId) {
        int availableStock = inventoryService.getAvailableStock(partId);
        return ResponseEntity.ok(availableStock);
    }

    @PostMapping("/breakage")
    public ResponseEntity<Void> deductStockForBreakage(@RequestParam String partId, @RequestParam int quantity) {
        inventoryService.deductStockForBreakage(partId, quantity);
        return ResponseEntity.noContent().build();
    }
}