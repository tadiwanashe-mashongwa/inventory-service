package com.example.inventoryservice.producer;
import com.example.inventoryservice.dto.StockReservedEvent;
import lombok.RequiredArgsConstructor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
@Component @RequiredArgsConstructor
public class StockReservationEventProducer {
 private final KafkaTemplate<String,String> kafkaTemplate;
 private final ObjectMapper objectMapper;
 public void publish(StockReservedEvent event) { try { kafkaTemplate.send("stock-reserved", event.orderId().toString(), objectMapper.writeValueAsString(event)); } catch (Exception e) { throw new IllegalStateException(e); } }
}
