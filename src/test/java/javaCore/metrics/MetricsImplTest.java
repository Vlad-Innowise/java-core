package javaCore.metrics;

import javaCore.metrics.entity.Category;
import javaCore.metrics.entity.Customer;
import javaCore.metrics.entity.Order;
import javaCore.metrics.entity.OrderItem;
import javaCore.metrics.entity.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MetricsImplTest {

    private List<Order> orders;
    private Map<String, ItemRecord> items;
    private Metrics metrics = new MetricsImpl();
    private Map<String, Customer> customers;

    @BeforeEach
    void init() {
        customers = new HashMap<>();
        initCustomers();

        items = new HashMap<>();
        initItems();

        orders = new ArrayList<>();

        orders.add(getOrder("0001", getRegDate(360), customers.get("Nick"), List.of(
                createItem("War And Peace", 3),
                createItem("Java Programming", 2)
        ), OrderStatus.DELIVERED));

        orders.add(getOrder("0002", getRegDate(345), customers.get("Nick"), List.of(
                createItem("Ipad", 1)
        ), OrderStatus.CANCELLED));

        orders.add(getOrder("0003", getRegDate(130), customers.get("Anna"), List.of(
                createItem("Jeans", 2),
                createItem("Jacket", 1)
        ), OrderStatus.DELIVERED));

        orders.add(getOrder("0004", getRegDate(120), customers.get("Anna"), List.of(
                createItem("T-Shirt", 3)
        ), OrderStatus.CANCELLED));

        orders.add(getOrder("0005", getRegDate(119), customers.get("Anna"), List.of(
                createItem("T-Shirt", 3)
        ), OrderStatus.DELIVERED));

        orders.add(getOrder("0006", getRegDate(99), customers.get("Anna"), List.of(
                createItem("War And Peace", 2)
        ), OrderStatus.DELIVERED));

        orders.add(getOrder("0007", getRegDate(25), customers.get("Anna"), List.of(
                createItem("Iphone", 1)
        ), OrderStatus.DELIVERED));

        orders.add(getOrder("0008", getRegDate(11), customers.get("Anna"), List.of(
                createItem("Jacket", 1)
        ), OrderStatus.DELIVERED));

        orders.add(getOrder("0009", getRegDate(5), customers.get("Anna"), List.of(
                createItem("Jeans", 1)
        ), OrderStatus.SHIPPED));

        orders.add(getOrder("0010", getRegDate(3), customers.get("Mike"), List.of(
                createItem("Ipad", 1)
        ), OrderStatus.DELIVERED));

        orders.add(getOrder("0011", getRegDate(1), customers.get("Mike"), List.of(
                createItem("War And Peace", 2)
        ), OrderStatus.NEW));

        orders.add(getOrder("0012", getRegDate(1), customers.get("Mike"), List.of(
                createItem("Airpods", 1)
        ), OrderStatus.PROCESSING));

    }

    private void initItems() {
        items.put("War And Peace", new ItemRecord(20.1, "BOOKS"));
        items.put("Java Programming", new ItemRecord(40, "BOOKS"));
        items.put("T-Shirt", new ItemRecord(20.20, "CLOTHING"));
        items.put("Jeans", new ItemRecord(55.55, "CLOTHING"));
        items.put("Jacket", new ItemRecord(90.30, "CLOTHING"));
        items.put("Iphone", new ItemRecord(900.30, "ELECTRONICS"));
        items.put("Ipad", new ItemRecord(1250.50, "ELECTRONICS"));
        items.put("Airpods", new ItemRecord(200.20, "ELECTRONICS"));
    }

    private void initCustomers() {
        customers.put("Nick", getCustomer("1111", "Nick", "nick@gmail.com", getRegDate(365), 34, "Moscow"));
        customers.put("Mike", getCustomer("2222", "Mike", "mike@gmail.com", getRegDate(200), 19, "Minsk"));
        customers.put("Anna", getCustomer("3333", "Anna", "anna@mail.ru", getRegDate(150), 27, "Paris"));
    }

    @Test
    void getUniqueOrderCitiesPositive() {
        List<String> expectedResult = List.of("Moscow", "Minsk", "Paris");

        List<String> actualResult = metrics.getUniqueOrderCities(orders);

        assertEquals(new HashSet<>(expectedResult), new HashSet<>(actualResult));
    }

    @Test
    void getUniqueOrderCitiesEmptyList() {

        List<String> actualResult = metrics.getUniqueOrderCities(Collections.emptyList());

        assertTrue(actualResult.isEmpty());
    }

    @Test
    void totalIncomeForAllCompletedOrdersPositive() {
        /** Included OrderStatus SHIPPED and DELIVERED*/
        BigDecimal expectedResult = Stream.of(
                BigDecimal.valueOf(items.get("War And Peace").price()).multiply(BigDecimal.valueOf(5)),
                BigDecimal.valueOf(items.get("Java Programming").price()).multiply(BigDecimal.valueOf(2)),
                BigDecimal.valueOf(items.get("T-Shirt").price()).multiply(BigDecimal.valueOf(3)),
                BigDecimal.valueOf(items.get("Jeans").price()).multiply(BigDecimal.valueOf(3)),
                BigDecimal.valueOf(items.get("Jacket").price()).multiply(BigDecimal.valueOf(2)),
                BigDecimal.valueOf(items.get("Iphone").price()).multiply(BigDecimal.valueOf(1)),
                BigDecimal.valueOf(items.get("Ipad").price()).multiply(BigDecimal.valueOf(1))
        ).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal actualResult = metrics.totalIncomeForAllCompletedOrders(orders);

        assertEquals(expectedResult, actualResult);
    }

    @Test
    void totalIncomeForAllCompletedOrdersShouldZeroWhenEmptyList() {
        /** Included OrderStatus SHIPPED and DELIVERED*/

        BigDecimal actualResult = metrics.totalIncomeForAllCompletedOrders(Collections.emptyList());

        assertEquals(BigDecimal.ZERO, actualResult);
    }

    @Test
    void totalIncomeForAllCompletedOrdersShouldZeroWhenNoDeliveredOrShipped() {
        /** Included OrderStatus SHIPPED and DELIVERED*/

        orders = new ArrayList<>();

        orders.add(getOrder("0011", getRegDate(1), customers.get("Mike"),
                            List.of(createItem("War And Peace", 2)), OrderStatus.NEW));

        BigDecimal actualResult = metrics.totalIncomeForAllCompletedOrders(orders);

        assertEquals(BigDecimal.ZERO, actualResult);
    }

    @Test
    void totalIncomeForAllCompletedOrdersExcludeOrdersWithNegativeTotal() {
        /** Included OrderStatus SHIPPED and DELIVERED*/

        orders = new ArrayList<>();
        int booksQuantity = 2;

        orders.add(getOrder("0011", getRegDate(1), customers.get("Mike"),
                            List.of(createItem("War And Peace", booksQuantity)), OrderStatus.DELIVERED));

        orders.add(getOrder("0011", getRegDate(1), customers.get("Mike"),
                            List.of(createItem("Ipad", -1)), OrderStatus.DELIVERED));

        BigDecimal expectedResult = BigDecimal.valueOf(items.get("War And Peace").price())
                                              .multiply(BigDecimal.valueOf(booksQuantity));

        BigDecimal actualResult = metrics.totalIncomeForAllCompletedOrders(orders);

        assertEquals(expectedResult, actualResult);
    }

    @Test
    void mostPopularProductBySalesPositive() {
        /** Excluded OrderStatus CANCELLED only*/
        String expectedResult = "War And Peace";

        String actualResult = metrics.mostPopularProductBySales(orders);

        assertEquals(expectedResult, actualResult);
    }

    @Test
    void mostPopularProductBySalesTwoOrMoreExactResults() {
        /** Excluded OrderStatus CANCELLED only*/
        orders = new ArrayList<>();

        int itemsCount = 2;
        orders.add(getOrder("0011", getRegDate(1), customers.get("Mike"),
                            List.of(createItem("War And Peace", itemsCount)), OrderStatus.DELIVERED));

        orders.add(getOrder("0011", getRegDate(1), customers.get("Mike"),
                            List.of(createItem("Java Programming", itemsCount)), OrderStatus.DELIVERED));

        orders.add(getOrder("0007", getRegDate(25), customers.get("Anna"),
                            List.of(createItem("War And Peace", itemsCount)), OrderStatus.DELIVERED));

        orders.add(getOrder("0007", getRegDate(25), customers.get("Anna"),
                            List.of(createItem("Java Programming", itemsCount)), OrderStatus.DELIVERED));
        Set<String> expectedResult = Set.of("War And Peace", "Java Programming");

        String actualResult = metrics.mostPopularProductBySales(orders);

        assertTrue(expectedResult.contains(actualResult));
    }

    @Test
    void mostPopularProductBySalesShouldThrowWhenEmpty() {

        assertThrowsExactly(NoSuchElementException.class,
                            () -> metrics.mostPopularProductBySales(Collections.emptyList()));
    }

    @Test
    void mostPopularProductBySalesShouldThrowWhenCancelledOnly() {

        orders = new ArrayList<>();
        orders.add(getOrder("0004", getRegDate(120), customers.get("Anna"),
                            List.of(createItem("T-Shirt", 3)), OrderStatus.CANCELLED));

        assertThrowsExactly(NoSuchElementException.class,
                            () -> metrics.mostPopularProductBySales(orders));
    }

    @Test
    void avgCheckSumForDeliveredOrdersPositive() {
        /** Included OrderStatus DELIVERED only*/
        BigDecimal total = Stream.of(
                BigDecimal.valueOf(items.get("War And Peace").price()).multiply(BigDecimal.valueOf(5)),
                BigDecimal.valueOf(items.get("Java Programming").price()).multiply(BigDecimal.valueOf(2)),
                BigDecimal.valueOf(items.get("T-Shirt").price()).multiply(BigDecimal.valueOf(3)),
                BigDecimal.valueOf(items.get("Jeans").price()).multiply(BigDecimal.valueOf(2)),//Excluded SHIPPED
                BigDecimal.valueOf(items.get("Jacket").price()).multiply(BigDecimal.valueOf(2)),
                BigDecimal.valueOf(items.get("Iphone").price()).multiply(BigDecimal.valueOf(1)),
                BigDecimal.valueOf(items.get("Ipad").price()).multiply(BigDecimal.valueOf(1))
        ).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal divisor = BigDecimal.valueOf(7);
        BigDecimal expectedResult = total.divide(divisor, 2, RoundingMode.HALF_UP);

        BigDecimal actualResult = metrics.avgCheckSumForDeliveredOrders(orders);

        assertEquals(expectedResult, actualResult);
    }

    @Test
    void avgCheckSumForDeliveredOrdersZeroWhenEmptyCollection() {
        /** Included OrderStatus DELIVERED only*/

        BigDecimal actualResult = metrics.avgCheckSumForDeliveredOrders(Collections.emptyList());

        assertEquals(BigDecimal.ZERO, actualResult);
    }

    @Test
    void avgCheckSumForDeliveredOrdersZeroWhenNoDelivered() {
        /** Included OrderStatus DELIVERED only*/

        orders = new ArrayList<>();

        orders.add(getOrder("0011", getRegDate(1), customers.get("Mike"),
                            List.of(createItem("War And Peace", 2)), OrderStatus.NEW));

        BigDecimal actualResult = metrics.avgCheckSumForDeliveredOrders(orders);

        assertEquals(BigDecimal.ZERO, actualResult);
    }

    @Test
    void avgCheckSumForDeliveredOrdersExcludeOrdersWithNegativeTotal() {
        /** Included OrderStatus DELIVERED only*/

        orders = new ArrayList<>();
        int booksQuantity = 2;
        int iphoneQuantity = 1;

        orders.add(getOrder("0011", getRegDate(1), customers.get("Mike"),
                            List.of(createItem("War And Peace", booksQuantity)), OrderStatus.DELIVERED));

        orders.add(getOrder("0011", getRegDate(1), customers.get("Mike"),
                            List.of(createItem("Ipad", -1)), OrderStatus.DELIVERED));

        orders.add(getOrder("0007", getRegDate(25), customers.get("Anna"),
                            List.of(createItem("Iphone", iphoneQuantity)), OrderStatus.DELIVERED));

        BigDecimal booksTotal = BigDecimal.valueOf(items.get("War And Peace").price())
                                          .multiply(BigDecimal.valueOf(booksQuantity));

        BigDecimal iphoneTotal = BigDecimal.valueOf(items.get("Iphone").price())
                                           .multiply(BigDecimal.valueOf(iphoneQuantity));
        BigDecimal ordersCount = BigDecimal.valueOf(2);
        BigDecimal expectedResult = booksTotal.add(iphoneTotal)
                                              .divide(ordersCount, 2, RoundingMode.HALF_UP);

        BigDecimal actualResult = metrics.avgCheckSumForDeliveredOrders(orders);

        assertEquals(expectedResult, actualResult);
    }

    @Test
    void getAllCustomersWithOrdersAmountGreaterThanPositiveScenario() {
        Set<Customer> expectedResult = Set.of(customers.get("Anna"));

        Set<Customer> actualResult = metrics.getAllCustomersWithOrdersAmountGreaterThan(
                orders, 5);

        assertEquals(expectedResult, actualResult);
    }

    @Test
    void getAllCustomersWithOrdersAmountGreaterThanEmptyOrderList() {

        Set<Customer> actualResult = metrics.getAllCustomersWithOrdersAmountGreaterThan(
                Collections.emptyList(), 5);

        assertTrue(actualResult.isEmpty());
    }

    @Test
    void getAllCustomersWithOrdersAmountGreaterThanShouldThrowWhenOrdersAmountIsNegativeOrZero() {

        int orderAmount = -3;

        assertThrowsExactly(IllegalArgumentException.class, () ->
                metrics.getAllCustomersWithOrdersAmountGreaterThan(orders, orderAmount));
    }

    private Order getOrder(String id, LocalDateTime orderDate, Customer customer, List<OrderItem> items,
                           OrderStatus status) {
        return Order.builder()
                    .orderId(id)
                    .orderDate(orderDate)
                    .customer(customer)
                    .items(items)
                    .status(status)
                    .build();
    }

    private Customer getCustomer(String id, String name, String email, LocalDateTime registeredAt, int age,
                                 String city) {
        return Customer.builder()
                       .customerId(id)
                       .name(name)
                       .email(email)
                       .registeredAt(registeredAt)
                       .age(age)
                       .city(city)
                       .build();
    }

    private OrderItem createItem(String name, int quantity) {
        ItemRecord itemRecord = items.get(name);
        return getOrderItem(name, quantity, itemRecord.price(), itemRecord.category());
    }

    private OrderItem getOrderItem(String productName, int quantity, double price, String category) {
        return OrderItem.builder()
                        .productName(productName)
                        .quantity(quantity)
                        .price(price)
                        .category(Category.valueOf(category))
                        .build();
    }

    private LocalDateTime getRegDate(int days) {
        return LocalDateTime.now().minusDays(days);
    }
}

record ItemRecord(double price, String category) {
}