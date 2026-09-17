package cart.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateCartRequest(
        @NotNull(message = "User ID cannot be null")
        @Positive(message = "User ID must be greater than zero")
        Long userId
) {}