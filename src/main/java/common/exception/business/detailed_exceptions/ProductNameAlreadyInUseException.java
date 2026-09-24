package common.exception.business.detailed_exceptions;

import common.exception.business.BusinessException;

public final class ProductNameAlreadyInUseException extends BusinessException {
    public ProductNameAlreadyInUseException() {
        super("This product name has already been used");
    }
    public ProductNameAlreadyInUseException(Throwable cause) {
        super("This product name has already been used", cause);
    }
}
