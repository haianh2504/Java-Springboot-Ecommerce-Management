package common.exception.business.detailed_exceptions;

import common.exception.business.BusinessException;

public final class ProductInactiveException extends BusinessException {
    public ProductInactiveException(Long productId) {
        super("This product with id: " + productId.toString() + " is inactive");
    }
    public ProductInactiveException(Long productId, Throwable cause) {
        super("This product with id: " + productId.toString() + " is inactive", cause);
    }
}
