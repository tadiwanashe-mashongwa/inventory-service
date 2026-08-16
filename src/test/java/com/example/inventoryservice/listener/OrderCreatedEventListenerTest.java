package com.example.inventoryservice.listener;
import com.example.inventoryservice.dto.*;
import com.example.inventoryservice.producer.StockReservationEventProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.inventoryservice.service.InventoryService;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.mockito.Mockito.*;
class OrderCreatedEventListenerTest {
 @Test void publishesStockReservedWhenAllItemsReserve() {
  InventoryService inventory=mock(InventoryService.class); StockReservationEventProducer producer=mock(StockReservationEventProducer.class);
  UUID orderId=UUID.randomUUID(); OrderItemEvent item=new OrderItemEvent(UUID.randomUUID(),2);
  when(inventory.reserveStock(orderId.toString(),item.partId().toString(),2)).thenReturn(true);
  new OrderEventListener(inventory,producer).handleOrderCreated(new OrderCreatedEvent(orderId,UUID.randomUUID(),List.of(item)));
  verify(producer).publish(new StockReservedEvent(orderId));
 }
 @Test void doesNotPublishWhenAnyItemCannotReserve() {
  InventoryService inventory=mock(InventoryService.class); StockReservationEventProducer producer=mock(StockReservationEventProducer.class);
  UUID orderId=UUID.randomUUID(); OrderItemEvent item=new OrderItemEvent(UUID.randomUUID(),2);
  when(inventory.reserveStock(orderId.toString(),item.partId().toString(),2)).thenReturn(false);
  new OrderEventListener(inventory,producer).handleOrderCreated(new OrderCreatedEvent(orderId,UUID.randomUUID(),List.of(item)));
  verifyNoInteractions(producer);
 }
}
