package cart.dto.response;

import cart.entities.Cart;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record CartTotalPriceResponse(
        @Positive
        @NotNull(message = "Cart Id cannot be null")
        Long cartId,

        @PositiveOrZero(message = "Total price cannot be negative")
        @NotNull(message = "Total price cannot be null")
        BigDecimal totalPrice
) {
}
