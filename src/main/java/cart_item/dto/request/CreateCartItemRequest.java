package cart_item.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateCartItemRequest(
        @Positive @NotNull Long productId,
        @Positive int quantity
) {
}
