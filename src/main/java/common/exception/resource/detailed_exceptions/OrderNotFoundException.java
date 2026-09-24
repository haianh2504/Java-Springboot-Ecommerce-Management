package common.exception.resource.detailed_exceptions;

import common.exception.resource.ResourceException;

public class OrderNotFoundException extends ResourceException {
    public OrderNotFoundException(Long id) {
        super("Order with id " + id + " not found");
    }
    public OrderNotFoundException(Long id, Throwable cause) {
        super("Order with id " + id + " not found", cause);
    }
}
