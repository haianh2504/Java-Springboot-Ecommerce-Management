package order.entities;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.stream.Stream;

public class OrderTest {
    private static final Long ORDER_ID = 1L;
    private static final Long USER_ID = 2L;
    private static final Long CART_ID = 3L;
    private static final Instant CREATED_AT = Instant.parse("2026-01-01T00:00:00Z");
    private static final BigDecimal SUB_TOTAL = new BigDecimal("100.00");
    private static final BigDecimal SHIPPING_FEE = new BigDecimal("10.00");
    private static final BigDecimal DISCOUNT_AMOUNT = new BigDecimal("5.00");
    private static final BigDecimal TOTAL_PRICE = new BigDecimal("105.00");

    @Nested
    @DisplayName("Return an order from SQL")
    class ReturnOrderFromSql {
        // Verify that the SQL-return constructor maps every persisted value correctly.
        @Test
        @DisplayName("Construct an order from valid SQL data")
        void constructOrder_validSqlData() {
            Order order = new Order(
                    ORDER_ID, USER_ID, CART_ID, OrderStatus.SUCCESSFUL, CREATED_AT,
                    SUB_TOTAL, SHIPPING_FEE, DISCOUNT_AMOUNT, TOTAL_PRICE
            );

            Assertions.assertAll(
                    () -> Assertions.assertEquals(ORDER_ID, order.getOrderId()),
                    () -> Assertions.assertEquals(USER_ID, order.getUserId()),
                    () -> Assertions.assertEquals(CART_ID, order.getCartId()),
                    () -> Assertions.assertEquals(OrderStatus.SUCCESSFUL, order.getOrderStatus()),
                    () -> Assertions.assertEquals(CREATED_AT, order.getCreatedAt()),
                    () -> Assertions.assertEquals(SUB_TOTAL, order.getSubTotal()),
                    () -> Assertions.assertEquals(SHIPPING_FEE, order.getShippingFee()),
                    () -> Assertions.assertEquals(DISCOUNT_AMOUNT, order.getDiscountAmount()),
                    () -> Assertions.assertEquals(TOTAL_PRICE, order.getTotalPrice())
            );
        }

        // Verify that optional null monetary values returned by SQL default to zero.
        @Test
        @DisplayName("Default nullable SQL monetary values to zero")
        void constructOrder_nullOptionalMonetaryData_defaultsToZero() {
            Order order = new Order(
                    ORDER_ID, USER_ID, CART_ID, OrderStatus.PENDING_PAYMENT, CREATED_AT,
                    SUB_TOTAL, null, null, TOTAL_PRICE
            );

            Assertions.assertAll(
                    () -> Assertions.assertEquals(BigDecimal.ZERO, order.getShippingFee()),
                    () -> Assertions.assertEquals(BigDecimal.ZERO, order.getDiscountAmount())
            );
        }

        // Verify that each required SQL field rejects null with the expected message.
        @ParameterizedTest(name = "Reject null {0}")
        @MethodSource("requiredNullArguments")
        @DisplayName("Throw NullPointerException when required SQL data is null")
        void constructOrder_nullRequiredData(String fieldName, String expectedMessage,
                                             Long orderId, Long userId, Long cartId,
                                             OrderStatus status, Instant createdAt,
                                             BigDecimal subTotal, BigDecimal totalPrice) {
            NullPointerException exception = Assertions.assertThrows(
                    NullPointerException.class,
                    () -> new Order(orderId, userId, cartId, status, createdAt,
                            subTotal, SHIPPING_FEE, DISCOUNT_AMOUNT, totalPrice)
            );

            Assertions.assertEquals(expectedMessage, exception.getMessage(), fieldName);
        }

        static Stream<Arguments> requiredNullArguments() {
            return Stream.of(
                    Arguments.of("orderId", "orderId cannot be null", null, USER_ID, CART_ID,
                            OrderStatus.SUCCESSFUL, CREATED_AT, SUB_TOTAL, TOTAL_PRICE),
                    Arguments.of("userId", "userId cannot be null", ORDER_ID, null, CART_ID,
                            OrderStatus.SUCCESSFUL, CREATED_AT, SUB_TOTAL, TOTAL_PRICE),
                    Arguments.of("cartId", "cartId cannot be null", ORDER_ID, USER_ID, null,
                            OrderStatus.SUCCESSFUL, CREATED_AT, SUB_TOTAL, TOTAL_PRICE),
                    Arguments.of("orderStatus", "orderStatus cannot be null", ORDER_ID, USER_ID, CART_ID,
                            null, CREATED_AT, SUB_TOTAL, TOTAL_PRICE),
                    Arguments.of("createdAt", "createdAt cannot be null", ORDER_ID, USER_ID, CART_ID,
                            OrderStatus.SUCCESSFUL, null, SUB_TOTAL, TOTAL_PRICE),
                    Arguments.of("subTotal", "subTotal cannot be null", ORDER_ID, USER_ID, CART_ID,
                            OrderStatus.SUCCESSFUL, CREATED_AT, null, TOTAL_PRICE),
                    Arguments.of("totalPrice", "totalPrice cannot be null", ORDER_ID, USER_ID, CART_ID,
                            OrderStatus.SUCCESSFUL, CREATED_AT, SUB_TOTAL, null)
            );
        }
    }

    @Nested
    @DisplayName("Create a new order")
    class CreateNewOrder {
        // Verify valid input creates a pending order with generated creation time and no ID.
        @Test
        @DisplayName("Construct a new order from valid data")
        void constructOrder_validData() {
            Instant beforeCreation = Instant.now();
            Order order = new Order(
                    USER_ID, CART_ID, SUB_TOTAL, SHIPPING_FEE, DISCOUNT_AMOUNT, TOTAL_PRICE
            );
            Instant afterCreation = Instant.now();

            Assertions.assertAll(
                    () -> Assertions.assertNull(order.getOrderId()),
                    () -> Assertions.assertEquals(USER_ID, order.getUserId()),
                    () -> Assertions.assertEquals(CART_ID, order.getCartId()),
                    () -> Assertions.assertEquals(OrderStatus.PENDING_PAYMENT, order.getOrderStatus()),
                    () -> Assertions.assertFalse(order.getCreatedAt().isBefore(beforeCreation)),
                    () -> Assertions.assertFalse(order.getCreatedAt().isAfter(afterCreation)),
                    () -> Assertions.assertEquals(SUB_TOTAL, order.getSubTotal()),
                    () -> Assertions.assertEquals(SHIPPING_FEE, order.getShippingFee()),
                    () -> Assertions.assertEquals(DISCOUNT_AMOUNT, order.getDiscountAmount()),
                    () -> Assertions.assertEquals(TOTAL_PRICE, order.getTotalPrice())
            );
        }

        // Verify that optional null monetary values for a new order default to zero.
        @Test
        @DisplayName("Default nullable monetary values to zero")
        void constructOrder_nullOptionalMonetaryData_defaultsToZero() {
            Order order = new Order(USER_ID, CART_ID, SUB_TOTAL, null, null, TOTAL_PRICE);

            Assertions.assertAll(
                    () -> Assertions.assertEquals(BigDecimal.ZERO, order.getShippingFee()),
                    () -> Assertions.assertEquals(BigDecimal.ZERO, order.getDiscountAmount())
            );
        }

        // Verify that each required new-order field rejects null with the expected message.
        @ParameterizedTest(name = "Reject null {0}")
        @MethodSource("requiredNullArguments")
        @DisplayName("Throw NullPointerException when required data is null")
        void constructOrder_nullRequiredData(String fieldName, String expectedMessage,
                                             Long userId, Long cartId,
                                             BigDecimal subTotal, BigDecimal totalPrice) {
            NullPointerException exception = Assertions.assertThrows(
                    NullPointerException.class,
                    () -> new Order(userId, cartId, subTotal,
                            SHIPPING_FEE, DISCOUNT_AMOUNT, totalPrice)
            );

            Assertions.assertEquals(expectedMessage, exception.getMessage(), fieldName);
        }

        static Stream<Arguments> requiredNullArguments() {
            return Stream.of(
                    Arguments.of("userId", "userId cannot be null", null, CART_ID, SUB_TOTAL, TOTAL_PRICE),
                    Arguments.of("cartId", "cartId cannot be null", USER_ID, null, SUB_TOTAL, TOTAL_PRICE),
                    Arguments.of("subTotal", "subTotal cannot be null", USER_ID, CART_ID, null, TOTAL_PRICE),
                    Arguments.of("totalPrice", "totalPrice cannot be null", USER_ID, CART_ID, SUB_TOTAL, null)
            );
        }

        // Verify that zero or negative monetary inputs violate the constructor rules.
        @ParameterizedTest(name = "Reject invalid {0}")
        @MethodSource("illegalMonetaryArguments")
        @DisplayName("Throw IllegalArgumentException for illegal monetary data")
        void constructOrder_illegalMonetaryData(String fieldName, String expectedMessage,
                                                BigDecimal subTotal, BigDecimal shippingFee,
                                                BigDecimal discountAmount) {
            IllegalArgumentException exception = Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> new Order(USER_ID, CART_ID, subTotal,
                            shippingFee, discountAmount, TOTAL_PRICE)
            );

            Assertions.assertEquals(expectedMessage, exception.getMessage(), fieldName);
        }

        static Stream<Arguments> illegalMonetaryArguments() {
            return Stream.of(
                    Arguments.of("zero subTotal", "subTotal must be greater than zero",
                            BigDecimal.ZERO, SHIPPING_FEE, DISCOUNT_AMOUNT),
                    Arguments.of("negative subTotal", "subTotal must be greater than zero",
                            new BigDecimal("-0.01"), SHIPPING_FEE, DISCOUNT_AMOUNT),
                    Arguments.of("negative shippingFee", "shippingFee must not be negative",
                            SUB_TOTAL, new BigDecimal("-0.01"), DISCOUNT_AMOUNT),
                    Arguments.of("negative discountAmount", "discountAmount must not be negative",
                            SUB_TOTAL, SHIPPING_FEE, new BigDecimal("-0.01"))
            );
        }
    }

}
