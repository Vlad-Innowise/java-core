package javaCore.metrics.entity;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class Order {

    private String orderId;
    private LocalDateTime orderDate;
    private Customer customer;
    private List<OrderItem> items;
    private OrderStatus status;
}
