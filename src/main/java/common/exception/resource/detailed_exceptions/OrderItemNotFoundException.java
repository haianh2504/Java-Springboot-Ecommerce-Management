package common.exception.resource.detailed_exceptions;

import common.exception.resource.ResourceException;

public class OrderItemNotFoundException extends ResourceException {
    // skeleton
    public OrderItemNotFoundException(String message) {
        super(message);
    }
    public OrderItemNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    // detailed implementation
//    ID
    public static OrderItemNotFoundException byId(Long id) {
        return new OrderItemNotFoundException("Order item with id " + id + " not found");
    }
    public OrderItemNotFoundException(Long id, Throwable cause) {
        super("Order item with id " + id + " not found", cause);
    }
//    orderId và productId
    public static OrderItemNotFoundException byOrderIdAndProductId(Long orderId, Long productId)
    {
        return new OrderItemNotFoundException("Order item with orderId " + orderId + " and productId " + productId + " not found");
    }
    public static OrderItemNotFoundException byOrderIdAndProductId(Long orderId, Long productId, Throwable cause)
    {
        return new OrderItemNotFoundException("Order item with orderId " + orderId + " and productId " + productId, cause);
    }
}
