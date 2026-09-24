package common.exception.business.detailed_exceptions;

import common.exception.business.BusinessException;

public final class InsufficientStockException extends BusinessException {
    public InsufficientStockException(
            Long productId,
            int requestedQuantity,
            int availableQuantity
    )
    {
        super(String.format(
                "Product [id=%d] does not have enough stock: requested %d, available %d", productId, requestedQuantity, availableQuantity
        ));
    }
    public InsufficientStockException(
            Long productId,
            int requestedQuantity,
            int availableQuantity,
            Throwable cause
    )
    {
     super(String.format(
             "Product [id=%d] does not have enough stock: requested %d, available %d", productId, requestedQuantity, availableQuantity
     ), cause);
    }
}
