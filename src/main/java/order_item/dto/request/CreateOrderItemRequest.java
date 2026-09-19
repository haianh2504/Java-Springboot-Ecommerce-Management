package order_item.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateOrderItemRequest(
        @NotNull(message = "Product ID cannot be null")
        @Positive(message = "Product ID must be greater than zero")
        Long productId,

        @Positive(message = "Quantity must be greater than zero")
        int quantity,

        @NotNull(message = "Unit price cannot be null")
        @Positive(message = "Unit price must be greater than zero")
        BigDecimal unitPrice
) {}
