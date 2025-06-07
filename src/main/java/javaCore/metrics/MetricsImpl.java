package javaCore.metrics;


import javaCore.metrics.entity.Customer;
import javaCore.metrics.entity.Order;
import javaCore.metrics.entity.OrderItem;
import javaCore.metrics.entity.OrderStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MetricsImpl implements Metrics {

    @Override
    public List<String> getUniqueOrderCities(List<Order> orders) {
        return orders.stream()
                     .map(o -> o.getCustomer().getCity())
                     .distinct()
                     .toList();
    }

    @Override
    public BigDecimal totalIncomeForAllCompletedOrders(List<Order> orders) {
        Set<OrderStatus> statuses = EnumSet.of(OrderStatus.SHIPPED, OrderStatus.DELIVERED);

        Stream<Order> orderStream = orders.stream()
                                          .filter(o -> statuses.contains(o.getStatus()));

        return sumAllOrdersWithPositiveTotalOnly(orderStream);
    }

    private BigDecimal sumAllOrdersWithPositiveTotalOnly(Stream<Order> orders) {
        return orders.map(this::calculateTotalByOrder)
                     .filter(totalByOrder -> totalByOrder.compareTo(BigDecimal.ZERO) > 0)
                     .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateTotalByOrder(Order order) {
        return order.getItems()
                    .stream()
                    .map(this::itemTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal itemTotal(OrderItem item) {
        return BigDecimal.valueOf(item.getPrice()).multiply(
                BigDecimal.valueOf(item.getQuantity())
        );
    }

    @Override
    public String mostPopularProductBySales(List<Order> orders) {
        Map<String, Integer> productSales =
                orders.stream()
                      .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                      .flatMap(o -> o.getItems().stream())
                      .collect(Collectors.toMap(OrderItem::getProductName,
                                                OrderItem::getQuantity,
                                                Integer::sum,
                                                HashMap::new));
        return productSales.entrySet()
                           .stream()
                           .max(Map.Entry.comparingByValue())
                           .map(Map.Entry::getKey)
                           .orElseThrow(() -> new NoSuchElementException("No sold products found: " + orders));
    }

    @Override
    public BigDecimal avgCheckSumForDeliveredOrders(List<Order> orders) {
        Predicate<Order> deliveredOnly = o -> o.getStatus() == OrderStatus.DELIVERED;

        long deliveredOrdersCount =
                orders.stream()
                      .filter(deliveredOnly)
                      .map(this::calculateTotalByOrder)
                      .filter(totalByOrder -> totalByOrder.compareTo(BigDecimal.ZERO) > 0)
                      .count();

        Stream<Order> orderStream = orders.stream()
                                          .filter(deliveredOnly);
        BigDecimal totalDeliveredAmount = sumAllOrdersWithPositiveTotalOnly(orderStream);

        return deliveredOrdersCount != 0
                ? totalDeliveredAmount.divide(BigDecimal.valueOf(deliveredOrdersCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
    }

    @Override
    public Set<Customer> getAllCustomersWithOrdersAmountGreaterThan(List<Order> orders, int amount) {

        if (amount <= 0) {
            throw new IllegalArgumentException("Orders amounts can't be negative or zero: " + amount);
        }

        Map<String, Customer> customers = new HashMap<>();
        Map<String, Long> ordersCountByCustomer =
                orders.stream()
                      .map(o -> {
                          customers.putIfAbsent(o.getCustomer().getCustomerId(),
                                                o.getCustomer());
                          return o;
                      }).collect(Collectors.groupingBy(
                              o -> o.getCustomer().getCustomerId(),
                              Collectors.counting())
                      );
        return ordersCountByCustomer.entrySet().stream()
                                    .filter(es -> es.getValue() > amount)
                                    .map(es -> customers.get(es.getKey()))
                                    .collect(Collectors.toSet());
    }

}
