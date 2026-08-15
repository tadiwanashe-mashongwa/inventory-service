package com.example.inventoryservice;

import com.example.inventoryservice.dto.OrderCreatedEvent;
import com.example.inventoryservice.dto.OrderItemEvent;
import com.example.inventoryservice.entity.StockLevel;
import com.example.inventoryservice.repository.StockLevelRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.UUID;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer",
                "spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer",
                "spring.kafka.producer.properties.spring.json.add.type.headers=false"
        }
)
@Testcontainers
class InventoryServiceIntegrationTest {

    @LocalServerPort
    private int port;

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15-alpine");

    @Container
    static final KafkaContainer kafka =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.kafka.bootstrap-servers",
                kafka::getBootstrapServers);
    }

    @Autowired
    private StockLevelRepository stockLevelRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void contextLoads() {
        // Verifies Spring Boot starts successfully with PostgreSQL and Kafka.
    }

    @Test
    void shouldApplyInventorySchemaThroughFlyway() {
        Integer appliedMigrations = jdbcTemplate.queryForObject(
                "select count(*) from flyway_schema_history where version = '1' and success = true",
                Integer.class
        );

        assertThat(appliedMigrations).isEqualTo(1);
    }

    @Test
    void shouldExposePublicHealthEndpoint() throws Exception {
        HttpResponse<String> response = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/actuator/health"))
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"status\":\"UP\"");
    }

    @Test
    void shouldExposeOpenApiDocumentation() throws Exception {
        HttpResponse<String> response = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/v3/api-docs"))
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("/api/inventory/stock/{partId}");
    }

    @Test
    void shouldReserveStockWhenOrderServicePublishesAnOrderCreatedEvent() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID partId = UUID.randomUUID();
        stockLevelRepository.save(StockLevel.builder()
                .partId(partId.toString())
                .availableQuantity(10)
                .reservedQuantity(0)
                .build());

        OrderCreatedEvent event = new OrderCreatedEvent(
                orderId,
                UUID.randomUUID(),
                List.of(new OrderItemEvent(partId, 3))
        );
        kafkaTemplate.send("order-created", orderId.toString(), event).get();

        await().untilAsserted(() -> {
            StockLevel stock = stockLevelRepository.findByPartId(partId.toString()).orElseThrow();
            assertThat(stock.getAvailableQuantity()).isEqualTo(7);
            assertThat(stock.getReservedQuantity()).isEqualTo(3);
        });
    }
}
