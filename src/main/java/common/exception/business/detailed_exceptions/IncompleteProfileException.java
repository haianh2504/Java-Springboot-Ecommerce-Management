package common.exception.business.detailed_exceptions;

import common.exception.business.BusinessException;

public final class IncompleteProfileException extends BusinessException {
    public IncompleteProfileException(){
        super("User profile is incomplete. Required verification and profile details must be completed before this action");
    }
    public IncompleteProfileException(Throwable cause){
        super("User profile is incomplete. Required verification and profile details must be completed before this action", cause);
    }
}
