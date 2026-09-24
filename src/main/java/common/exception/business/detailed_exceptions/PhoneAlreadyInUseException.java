package common.exception.business.detailed_exceptions;

import common.exception.business.BusinessException;

public final class PhoneAlreadyInUseException extends BusinessException {
    public PhoneAlreadyInUseException() {
        super("This phone number has already been used");
    }
    public PhoneAlreadyInUseException(Throwable cause) {
        super("This phone number has already been used", cause);
    }
}
