package com.example.inventoryservice.controller;

import com.example.inventoryservice.entity.StockLevel;
import com.example.inventoryservice.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InventoryController.class)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryService inventoryService;

    @Test
    void shouldAddStockSuccessfully() throws Exception {
        StockLevel stock = StockLevel.builder().id(1L).partId("PART-1").availableQuantity(10).reservedQuantity(0).version(0L).build();
        when(inventoryService.addStock("PART-1", 10)).thenReturn(stock);

        mockMvc.perform(post("/api/inventory/stock")
                        .param("partId", "PART-1")
                        .param("quantity", "10")
                        .with(jwt().authorities(AuthorityUtils.createAuthorityList("ROLE_ADMIN"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.partId").value("PART-1"))
                .andExpect(jsonPath("$.availableQuantity").value(10));

        verify(inventoryService, times(1)).addStock("PART-1", 10);
    }

    @Test
    void shouldGetAvailableStockSuccessfully() throws Exception {
        when(inventoryService.getAvailableStock("PART-1")).thenReturn(25);

        mockMvc.perform(get("/api/inventory/stock/PART-1")
                        .with(jwt().authorities(AuthorityUtils.createAuthorityList("ROLE_CUSTOMER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(25));

        verify(inventoryService, times(1)).getAvailableStock("PART-1");
    }

    @Test
    void shouldDeductStockForBreakageSuccessfully() throws Exception {
        doNothing().when(inventoryService).deductStockForBreakage("PART-1", 5);

        mockMvc.perform(post("/api/inventory/breakage")
                        .param("partId", "PART-1")
                        .param("quantity", "5")
                        .with(jwt().authorities(AuthorityUtils.createAuthorityList("ROLE_ADMIN"))))
                .andExpect(status().isNoContent());

        verify(inventoryService, times(1)).deductStockForBreakage("PART-1", 5);
    }
}
