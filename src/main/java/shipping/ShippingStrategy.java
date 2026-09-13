package shipping;

import checkout.entities.CheckoutItem;

import java.math.BigDecimal;
import java.util.List;

// Applying Design patten -> "Strategy Pattern"
public interface ShippingStrategy {
    BigDecimal calculateShippingFee(List<CheckoutItem> checkoutItems);
}
