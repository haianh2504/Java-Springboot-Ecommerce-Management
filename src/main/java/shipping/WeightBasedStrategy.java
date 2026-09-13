package shipping;

import checkout.entities.CheckoutItem;
import product.entities.PhysicalProduct;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class WeightBasedStrategy implements ShippingStrategy {
    private final BigDecimal pricePerWeightUnit;
//    constructor
    public WeightBasedStrategy(BigDecimal pricePerWeightUnit) {
        this.pricePerWeightUnit = Objects.requireNonNull(pricePerWeightUnit, "pricePerWeightUnit must not be null");
        if(pricePerWeightUnit.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price per unit must be greater than zero");
        }
    }
    //    calculate Fee
    @Override
    public BigDecimal calculateShippingFee(List<CheckoutItem> checkoutItems) {
        Objects.requireNonNull(checkoutItems, "checkoutItems must not be null");
        BigDecimal totalWeight = BigDecimal.ZERO;
        for (CheckoutItem checkoutItem : checkoutItems) {
            if (checkoutItem.product() instanceof PhysicalProduct physicalProduct) {
                BigDecimal itemWeight = physicalProduct.getWeight()
                        .multiply(BigDecimal.valueOf(checkoutItem.cartItem().getNumber()));
                totalWeight = totalWeight.add(itemWeight);
            }
        }
        return pricePerWeightUnit.multiply(totalWeight);
    }
}
