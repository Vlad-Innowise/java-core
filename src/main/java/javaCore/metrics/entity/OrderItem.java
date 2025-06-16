package javaCore.metrics.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderItem {

    private String productName;
    private int quantity;
    private double price;
    private Category category;
}
