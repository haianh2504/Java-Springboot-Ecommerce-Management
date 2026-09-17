package order.service;

import exception.resource.detailed_exceptions.OrderNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import order.entities.Order;
import order.entities.OrderStatus;
import order.repository.OrderRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderManagementServiceImplTest {
    private static final Long USER_ID = 10L;
    private static final Long CART_ID = 20L;
    private static final Long ORDER_ID = 30L;
    private static final BigDecimal SUB_TOTAL = new BigDecimal("100.00");
    private static final BigDecimal SHIPPING_FEE = new BigDecimal("10.00");
    private static final BigDecimal DISCOUNT_AMOUNT = new BigDecimal("5.00");
    private static final BigDecimal TOTAL_PRICE = new BigDecimal("105.00");

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderManagementServiceImpl orderManagementServiceImpl;

    private Order createPersistedOrder()
    {
        return new Order(
                ORDER_ID,
                USER_ID,
                CART_ID,
                OrderStatus.PENDING_PAYMENT,
                Instant.parse("2026-09-10T00:00:00Z"),
                SUB_TOTAL,
                SHIPPING_FEE,
                DISCOUNT_AMOUNT,
                TOTAL_PRICE
        );
    }

    @Test
    @DisplayName("Construct the order service with a null repository throws NullPointerException")
    void constructor_nullOrderRepository_throwsNullPointerException()
    {
        // --GIVEN--
        OrderRepository nullOrderRepository = null;

        // --WHEN--
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new OrderManagementServiceImpl(nullOrderRepository)
        );

        // --THEN--
        assertEquals("orderRepository must not be null", exception.getMessage());
    }

    @Test
    @DisplayName("Create an order with valid values, then save and return the persisted order")
    void createOrder_validArguments_savesAndReturnsPersistedOrder()
    {
        // --GIVEN--
        Order savedOrder = createPersistedOrder();
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // --WHEN--
        Order actualOrder = orderManagementServiceImpl.createOrder(
                USER_ID, CART_ID, SUB_TOTAL, SHIPPING_FEE, DISCOUNT_AMOUNT, TOTAL_PRICE
        );

        // --THEN--
        assertAll(
                () -> assertSame(savedOrder, actualOrder),
                () -> assertEquals(ORDER_ID, actualOrder.getOrderId()),
                () -> assertSame(OrderStatus.PENDING_PAYMENT, actualOrder.getOrderStatus())
        );
        verify(orderRepository).save(argThat(order ->
                order.getOrderId() == null
                        && order.getUserId().equals(USER_ID)
                        && order.getCartId().equals(CART_ID)
                        && order.getSubTotal().compareTo(SUB_TOTAL) == 0
                        && order.getShippingFee().compareTo(SHIPPING_FEE) == 0
                        && order.getDiscountAmount().compareTo(DISCOUNT_AMOUNT) == 0
                        && order.getTotalPrice().compareTo(TOTAL_PRICE) == 0
                        && order.getOrderStatus() == OrderStatus.PENDING_PAYMENT
                        && order.getCreatedAt() != null
        ));
    }

    @Test
    @DisplayName("Create an order with zero shipping, discount, and total values successfully")
    void createOrder_zeroAllowedMonetaryValues_savesOrder()
    {
        // --GIVEN--
        BigDecimal zero = BigDecimal.ZERO;
        Order savedOrder = new Order(
                ORDER_ID, USER_ID, CART_ID, OrderStatus.PENDING_PAYMENT,
                Instant.parse("2026-09-10T00:00:00Z"),
                SUB_TOTAL, zero, zero, zero
        );
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // --WHEN--
        Order actualOrder = orderManagementServiceImpl.createOrder(
                USER_ID, CART_ID, SUB_TOTAL, zero, zero, zero
        );

        // --THEN--
        assertSame(savedOrder, actualOrder);
        verify(orderRepository).save(argThat(order ->
                order.getShippingFee().compareTo(BigDecimal.ZERO) == 0
                        && order.getDiscountAmount().compareTo(BigDecimal.ZERO) == 0
                        && order.getTotalPrice().compareTo(BigDecimal.ZERO) == 0
        ));
    }

    @ParameterizedTest(name = "{index}: null {0}")
    @MethodSource("nullCreateOrderLongArguments")
    @DisplayName("Create an order with a null Long identifier throws NullPointerException")
    void createOrder_nullLongArgument_throwsNullPointerException(
            String nullArgument, Long userId, Long cartId, String expectedMessage)
    {
        // --GIVEN--
        // Long arguments are supplied by nullCreateOrderLongArguments().

        // --WHEN--
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> orderManagementServiceImpl.createOrder(
                        userId, cartId, SUB_TOTAL, SHIPPING_FEE, DISCOUNT_AMOUNT, TOTAL_PRICE
                )
        );

        // --THEN--
        assertEquals(expectedMessage, exception.getMessage());
        verifyNoInteractions(orderRepository);
    }

    static Stream<Arguments> nullCreateOrderLongArguments()
    {
        return Stream.of(
                Arguments.of("user ID", null, CART_ID, "userId must not be null"),
                Arguments.of("cart ID", USER_ID, null, "cartId must not be null")
        );
    }

    @ParameterizedTest(name = "{index}: null {0}")
    @MethodSource("nullCreateOrderMonetaryArguments")
    @DisplayName("Create an order with a null monetary argument throws NullPointerException")
    void createOrder_nullMonetaryArgument_throwsNullPointerException(
            String nullArgument,
            BigDecimal subTotal,
            BigDecimal shippingFee,
            BigDecimal discountAmount,
            BigDecimal totalPrice,
            String expectedMessage)
    {
        // --GIVEN--
        // Monetary arguments are supplied by nullCreateOrderMonetaryArguments().

        // --WHEN--
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> orderManagementServiceImpl.createOrder(
                        USER_ID, CART_ID, subTotal, shippingFee, discountAmount, totalPrice
                )
        );

        // --THEN--
        assertEquals(expectedMessage, exception.getMessage());
        verifyNoInteractions(orderRepository);
    }

    static Stream<Arguments> nullCreateOrderMonetaryArguments()
    {
        return Stream.of(
                Arguments.of("subtotal", null, SHIPPING_FEE, DISCOUNT_AMOUNT, TOTAL_PRICE,
                        "subTotal must not be null"),
                Arguments.of("shipping fee", SUB_TOTAL, null, DISCOUNT_AMOUNT, TOTAL_PRICE,
                        "shippingFee must not be null"),
                Arguments.of("discount amount", SUB_TOTAL, SHIPPING_FEE, null, TOTAL_PRICE,
                        "discountAmount must not be null"),
                Arguments.of("total price", SUB_TOTAL, SHIPPING_FEE, DISCOUNT_AMOUNT, null,
                        "totalPrice must not be null")
        );
    }

    @ParameterizedTest(name = "{index}: negative {0}")
    @MethodSource("negativeCreateOrderMonetaryArguments")
    @DisplayName("Create an order with a negative monetary argument throws IllegalArgumentException")
    void createOrder_negativeMonetaryArgument_throwsIllegalArgumentException(
            String invalidArgument,
            BigDecimal subTotal,
            BigDecimal shippingFee,
            BigDecimal discountAmount,
            BigDecimal totalPrice,
            String expectedMessage)
    {
        // --GIVEN--
        // Negative values are supplied by negativeCreateOrderMonetaryArguments().

        // --WHEN--
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderManagementServiceImpl.createOrder(
                        USER_ID, CART_ID, subTotal, shippingFee, discountAmount, totalPrice
                )
        );

        // --THEN--
        assertEquals(expectedMessage, exception.getMessage());
        verifyNoInteractions(orderRepository);
    }

    static Stream<Arguments> negativeCreateOrderMonetaryArguments()
    {
        BigDecimal negative = new BigDecimal("-0.01");
        return Stream.of(
                Arguments.of("subtotal", negative, SHIPPING_FEE, DISCOUNT_AMOUNT, TOTAL_PRICE,
                        "subtotal cannot be negative"),
                Arguments.of("discount amount", SUB_TOTAL, SHIPPING_FEE, negative, TOTAL_PRICE,
                        "discountAmount cannot be negative"),
                Arguments.of("shipping fee", SUB_TOTAL, negative, DISCOUNT_AMOUNT, TOTAL_PRICE,
                        "ShippingFee cannot be negative"),
                Arguments.of("total price", SUB_TOTAL, SHIPPING_FEE, DISCOUNT_AMOUNT, negative,
                        "totalPrice cannot be negative")
        );
    }

    @Test
    @DisplayName("Create an order with a zero subtotal throws IllegalArgumentException")
    void createOrder_zeroSubtotal_throwsIllegalArgumentException()
    {
        // --GIVEN--
        BigDecimal zeroSubtotal = BigDecimal.ZERO;

        // --WHEN--
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderManagementServiceImpl.createOrder(
                        USER_ID, CART_ID, zeroSubtotal,
                        SHIPPING_FEE, DISCOUNT_AMOUNT, TOTAL_PRICE
                )
        );

        // --THEN--
        assertEquals("subTotal must be greater than zero", exception.getMessage());
        verifyNoInteractions(orderRepository);
    }

    @Test
    @DisplayName("Get all orders for a user returns every persisted order")
    void getAllOrdersByUserId_existingOrders_returnsOrders()
    {
        // --GIVEN--
        List<Order> persistedOrders = List.of(
                createPersistedOrder(),
                new Order(
                        31L, USER_ID, 21L, OrderStatus.SUCESSFUL,
                        Instant.parse("2026-09-11T00:00:00Z"),
                        new BigDecimal("50.00"), new BigDecimal("5.00"),
                        BigDecimal.ZERO, new BigDecimal("55.00")
                )
        );
        when(orderRepository.findByUserId(USER_ID)).thenReturn(persistedOrders);

        // --WHEN--
        List<Order> actualOrders = orderManagementServiceImpl.getAllOrdersByUserId(USER_ID);

        // --THEN--
        assertSame(persistedOrders, actualOrders);
        assertEquals(2, actualOrders.size());
        verify(orderRepository).findByUserId(USER_ID);
    }

    @Test
    @DisplayName("Get all orders for a user with no orders returns an empty list")
    void getAllOrdersByUserId_noOrders_returnsEmptyList()
    {
        // --GIVEN--
        List<Order> noOrders = List.of();
        when(orderRepository.findByUserId(USER_ID)).thenReturn(noOrders);

        // --WHEN--
        List<Order> actualOrders = orderManagementServiceImpl.getAllOrdersByUserId(USER_ID);

        // --THEN--
        assertSame(noOrders, actualOrders);
        assertTrue(actualOrders.isEmpty());
        verify(orderRepository).findByUserId(USER_ID);
    }

    @ParameterizedTest
    @NullSource
    @DisplayName("Get all orders with a null user ID throws NullPointerException")
    void getAllOrdersByUserId_nullUserId_throwsNullPointerException(Long userId)
    {
        // --GIVEN--
        // The null Long argument is supplied by @NullSource.

        // --WHEN--
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> orderManagementServiceImpl.getAllOrdersByUserId(userId)
        );

        // --THEN--
        assertEquals("userId must not be null", exception.getMessage());
        verifyNoInteractions(orderRepository);
    }

    @Test
    @DisplayName("Delete an existing order successfully")
    void deleteOrderById_existingOrder_deletesSuccessfully()
    {
        // --GIVEN--
        Order persistedOrder = createPersistedOrder();
        when(orderRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(persistedOrder));

        // --WHEN--
        orderManagementServiceImpl.deleteOrderById(ORDER_ID);

        // --THEN--
        verify(orderRepository).findByOrderId(ORDER_ID);
        verify(orderRepository).deleteByOrderId(ORDER_ID);
    }

    @Test
    @DisplayName("Delete an unknown order throws OrderNotFoundException")
    void deleteOrderById_unknownOrder_throwsOrderNotFoundException()
    {
        // --GIVEN--
        when(orderRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.empty());

        // --WHEN--
        OrderNotFoundException exception = assertThrows(
                OrderNotFoundException.class,
                () -> orderManagementServiceImpl.deleteOrderById(ORDER_ID)
        );

        // --THEN--
        assertEquals("Order with id 30 not found", exception.getMessage());
        verify(orderRepository).findByOrderId(ORDER_ID);
        verify(orderRepository, never()).deleteByOrderId(anyLong());
    }

    @ParameterizedTest
    @NullSource
    @DisplayName("Delete an order with a null order ID throws NullPointerException")
    void deleteOrderById_nullOrderId_throwsNullPointerException(Long orderId)
    {
        // --GIVEN--
        // The null Long argument is supplied by @NullSource.

        // --WHEN--
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> orderManagementServiceImpl.deleteOrderById(orderId)
        );

        // --THEN--
        assertEquals("orderId must not be null", exception.getMessage());
        verifyNoInteractions(orderRepository);
    }

    @Test
    @DisplayName("Get an existing order by ID returns the persisted order")
    void getOrderById_existingOrder_returnsPersistedOrder()
    {
        // --GIVEN--
        Order persistedOrder = createPersistedOrder();
        when(orderRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(persistedOrder));

        // --WHEN--
        Order actualOrder = orderManagementServiceImpl.getOrderById(ORDER_ID);

        // --THEN--
        assertSame(persistedOrder, actualOrder);
        verify(orderRepository).findByOrderId(ORDER_ID);
    }

    @Test
    @DisplayName("Get an unknown order by ID throws OrderNotFoundException")
    void getOrderById_unknownOrder_throwsOrderNotFoundException()
    {
        // --GIVEN--
        when(orderRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.empty());

        // --WHEN--
        OrderNotFoundException exception = assertThrows(
                OrderNotFoundException.class,
                () -> orderManagementServiceImpl.getOrderById(ORDER_ID)
        );

        // --THEN--
        assertEquals("Order with id 30 not found", exception.getMessage());
        verify(orderRepository).findByOrderId(ORDER_ID);
    }

    @ParameterizedTest
    @NullSource
    @DisplayName("Get an order with a null order ID throws NullPointerException")
    void getOrderById_nullOrderId_throwsNullPointerException(Long orderId)
    {
        // --GIVEN--
        // The null Long argument is supplied by @NullSource.

        // --WHEN--
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> orderManagementServiceImpl.getOrderById(orderId)
        );

        // --THEN--
        assertEquals("orderId must not be null", exception.getMessage());
        verifyNoInteractions(orderRepository);
    }
}
