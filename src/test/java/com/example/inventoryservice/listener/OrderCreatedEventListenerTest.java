package com.example.inventoryservice.listener;
import com.example.inventoryservice.dto.*;
import com.example.inventoryservice.producer.StockReservationEventProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.inventoryservice.service.InventoryService;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.mockito.Mockito.*;
class OrderCreatedEventListenerTest {
 @Test void publishesStockReservedWhenAllItemsReserve() throws Exception {
  InventoryService inventory=mock(InventoryService.class); StockReservationEventProducer producer=mock(StockReservationEventProducer.class);
  UUID orderId=UUID.randomUUID(); OrderItemEvent item=new OrderItemEvent(UUID.randomUUID(),2);
  when(inventory.reserveStock(orderId.toString(),item.partId().toString(),2)).thenReturn(true);
  OrderCreatedEvent event = new OrderCreatedEvent(orderId,UUID.randomUUID(),List.of(item));
  new OrderEventListener(inventory,producer,new ObjectMapper()).handleOrderCreated(new ObjectMapper().writeValueAsString(event));
  verify(producer).publish(new StockReservedEvent(orderId));
 }
 @Test void doesNotPublishWhenAnyItemCannotReserve() throws Exception {
  InventoryService inventory=mock(InventoryService.class); StockReservationEventProducer producer=mock(StockReservationEventProducer.class);
  UUID orderId=UUID.randomUUID(); OrderItemEvent item=new OrderItemEvent(UUID.randomUUID(),2);
  when(inventory.reserveStock(orderId.toString(),item.partId().toString(),2)).thenReturn(false);
  OrderCreatedEvent event = new OrderCreatedEvent(orderId,UUID.randomUUID(),List.of(item));
  new OrderEventListener(inventory,producer,new ObjectMapper()).handleOrderCreated(new ObjectMapper().writeValueAsString(event));
  verifyNoInteractions(producer);
 }
}
