package javaCore.metrics;

import javaCore.metrics.entity.Customer;
import javaCore.metrics.entity.Order;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public interface Metrics {

    List<String> getUniqueOrderCities(List<Order> orders);

    BigDecimal totalIncomeForAllCompletedOrders(List<Order> orders);

    String mostPopularProductBySales(List<Order> orders);

    BigDecimal avgCheckSumForDeliveredOrders(List<Order> orders);

    Set<Customer> getAllCustomersWithOrdersAmountGreaterThan(List<Order> orders, int amount);

}
