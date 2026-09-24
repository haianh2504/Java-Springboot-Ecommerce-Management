package common.exception.resource.detailed_exceptions;

import common.exception.resource.ResourceException;

public class CartNotFoundException extends ResourceException {
    public CartNotFoundException(Long id) {
        super("Cart with id " + id + " not found");
    }
    public CartNotFoundException(Long id, Throwable cause) {
        super("Cart with id " + id + " not found", cause);
    }
}
