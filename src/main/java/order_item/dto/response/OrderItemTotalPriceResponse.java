package order_item.dto.response;

import java.math.BigDecimal;

public record OrderItemTotalPriceResponse(
        Long orderId,
        Long productId,
        BigDecimal totalPrice
) {}
