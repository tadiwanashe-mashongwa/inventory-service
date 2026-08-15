package com.example.inventoryservice.config;

import com.example.inventoryservice.controller.InventoryController;
import com.example.inventoryservice.entity.StockLevel;
import com.example.inventoryservice.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InventoryController.class)
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryService inventoryService;

    @Test
    void shouldRejectUnauthenticatedStockLookup() throws Exception {
        mockMvc.perform(get("/api/inventory/stock/PART-1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowCustomerToReadStock() throws Exception {
        when(inventoryService.getAvailableStock("PART-1")).thenReturn(12);

        mockMvc.perform(get("/api/inventory/stock/PART-1")
                        .with(jwt().authorities(AuthorityUtils.createAuthorityList("ROLE_CUSTOMER"))))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectCustomerStockAdjustment() throws Exception {
        mockMvc.perform(post("/api/inventory/stock")
                        .param("partId", "PART-1")
                        .param("quantity", "10")
                        .with(jwt().authorities(AuthorityUtils.createAuthorityList("ROLE_CUSTOMER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowAdminToAddStock() throws Exception {
        when(inventoryService.addStock("PART-1", 10)).thenReturn(StockLevel.builder()
                .partId("PART-1")
                .availableQuantity(10)
                .reservedQuantity(0)
                .build());

        mockMvc.perform(post("/api/inventory/stock")
                        .param("partId", "PART-1")
                        .param("quantity", "10")
                        .with(jwt().authorities(AuthorityUtils.createAuthorityList("ROLE_ADMIN"))))
                .andExpect(status().isCreated());
    }
}
