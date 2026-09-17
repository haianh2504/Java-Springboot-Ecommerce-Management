package cart_item.dto.response;

import cart_item.entities.CartItem;

import java.util.Objects;

public record CartItemResponse(
        Long cartItemId,
        Long cartId,
        Long productId,
        int quantity
) {
    public static CartItemResponse from(CartItem item) {
        Objects.requireNonNull(item,"CartItem cannot be null");
        return new CartItemResponse(
                item.getCartItemId(),
                item.getCartId(),
                item.getProductId(),
                item.getNumber()
        );
    }
}