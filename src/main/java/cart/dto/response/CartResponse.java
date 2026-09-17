package cart.dto.response;

import cart.entities.Cart;
import cart.entities.CartStatus;

import java.time.Instant;
import java.util.Objects;

public record CartResponse(
        Long cartId,
        Long userId,
        Instant createdAt,
        CartStatus status
) {
    public static CartResponse from(Cart cart) {
        Objects.requireNonNull(cart, "Cart cannot be null");
        return new CartResponse(
                cart.getCartId(),
                cart.getUserId(),
                cart.getCreatedAt(),
                cart.getCartStatus()
        );
    }
}
