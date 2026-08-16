package com.example.inventoryservice.producer;
import com.example.inventoryservice.dto.StockReservedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
@Component @RequiredArgsConstructor
public class StockReservationEventProducer {
 private final KafkaTemplate<String,Object> kafkaTemplate;
 public void publish(StockReservedEvent event) { kafkaTemplate.send("stock-reserved", event.orderId().toString(), event); }
}
