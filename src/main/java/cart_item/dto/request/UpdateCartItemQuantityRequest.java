package cart_item.dto.request;

import jakarta.validation.constraints.Positive;

public record UpdateCartItemQuantityRequest(
        @Positive(message = "Quantity must be greater than zero")
        int quantity
) {}