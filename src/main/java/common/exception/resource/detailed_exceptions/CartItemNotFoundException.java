package common.exception.resource.detailed_exceptions;

import common.exception.resource.ResourceException;

public class CartItemNotFoundException extends ResourceException {
    // cart item id
    public CartItemNotFoundException(Long id) {
        super("cart item with id " + id + " not found");
    }
    public CartItemNotFoundException(Long id, Throwable cause) {
        super("cart item with id " + id + " not found", cause);
    }
    // cart id & productId
    public CartItemNotFoundException(Long cartId, Long productId)
    {
        super(String.format("Cart item with cart [id=%d] and product [id=%d] not found", cartId, productId));
    }
    public CartItemNotFoundException(Long cartId, Long productId,Throwable cause)
    {
        super(String.format("Cart item with cart [id=%d] and product [id=%d] not found", cartId, productId), cause);
    }

}
