package common.exception.business.detailed_exceptions;

import common.exception.business.BusinessException;

public final class CartOwnershipMismatchException extends BusinessException {
    public CartOwnershipMismatchException(Long userId, Long cartId){
        super(String.format(
           "This cart [id=%d] does not belong to the user [id=%d]", cartId, userId
        ));
    }
    public CartOwnershipMismatchException(Long userId, Long cartId, Throwable cause) {
        super(String.format(
                "This cart [id=%d] does not belong to the user [id=%d]", cartId, userId
        ),cause);
    }
}
