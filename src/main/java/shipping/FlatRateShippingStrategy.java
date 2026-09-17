package shipping;

import checkout.entities.CheckoutItem;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public final class FlatRateShippingStrategy implements ShippingStrategy {
    private final BigDecimal flatFee;

    public FlatRateShippingStrategy(BigDecimal flatFee) {
        this.flatFee = Objects.requireNonNull(flatFee, "flatFee must not be null");
        if (flatFee.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("flatFee must not be negative");
        }
    }

    @Override
    public BigDecimal calculateShippingFee(List<CheckoutItem> checkoutItems) {
        Objects.requireNonNull(checkoutItems, "checkoutItems must not be null");
        if (checkoutItems.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return flatFee;
    }
}
