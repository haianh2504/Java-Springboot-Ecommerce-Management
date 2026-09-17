package order.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateOrderRequest(
        @NotNull(message = "User ID cannot be null")
        @Positive(message = "User ID must be greater than zero")
        Long userId,

        @NotNull(message = "Cart ID cannot be null")
        @Positive(message = "Cart ID must be greater than zero")
        Long cartId
) {}
