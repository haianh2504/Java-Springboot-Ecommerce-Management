package common.exception.business.detailed_exceptions;

import common.exception.business.BusinessException;

public final class CartAlreadyCheckedOutException extends BusinessException {
    public CartAlreadyCheckedOutException() {
        super("This cart has already been checked out");
    }
    public CartAlreadyCheckedOutException(Throwable cause) {
        super("This cart has already been checked out", cause);
    }
}
