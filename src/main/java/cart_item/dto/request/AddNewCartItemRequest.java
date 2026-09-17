package cart_item.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AddNewCartItemRequest(
        @NotNull(message = "Product ID cannot be null")
        @Positive(message = "Product ID must be greater than zero")
        Long productId,

        @Positive(message = "Quantity must be greater than zero")
        int quantity
) {}