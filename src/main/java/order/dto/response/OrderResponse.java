package order.dto.response;

import order.entities.Order;
import order.entities.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public record OrderResponse(
        Long orderId,
        Long userId,
        Long cartId,
        OrderStatus orderStatus,
        Instant createdAt,
        BigDecimal subTotal,
        BigDecimal shippingFee,
        BigDecimal discountAmount,
        BigDecimal totalPrice
) {
    public static OrderResponse from(Order order) {
        Objects.requireNonNull(order, "Order cannot be null");
        return new OrderResponse(
                order.getOrderId(),
                order.getUserId(),
                order.getCartId(),
                order.getOrderStatus(),
                order.getCreatedAt(),
                order.getSubTotal(),
                order.getShippingFee(),
                order.getDiscountAmount(),
                order.getTotalPrice()
        );
    }
}
