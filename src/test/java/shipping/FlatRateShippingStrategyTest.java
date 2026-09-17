package shipping;

import cart_item.entities.CartItem;
import checkout.entities.CheckoutItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import product.entities.Product;
import product.entities.ProductName;
import product.entities.ProductStatus;
import shipping.FlatRateShippingStrategy;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FlatRateShippingStrategyTest {
    @Test
    @DisplayName("Charge one flat fee for a non-empty checkout")
    void calculateShippingFee_nonEmptyCheckout_returnsFlatFee() {
        FlatRateShippingStrategy strategy = new FlatRateShippingStrategy(new BigDecimal("10.00"));
        Product product = new Product(
                new ProductName("Keyboard"), 10, new BigDecimal("100.00"), ProductStatus.ACTIVE
        );
        CheckoutItem item = new CheckoutItem(new CartItem(1L, 1L, 2), product);

        BigDecimal fee = strategy.calculateShippingFee(List.of(item));

        assertEquals(0, new BigDecimal("10.00").compareTo(fee));
    }

    @Test
    @DisplayName("Do not charge shipping for an empty checkout")
    void calculateShippingFee_emptyCheckout_returnsZero() {
        FlatRateShippingStrategy strategy = new FlatRateShippingStrategy(new BigDecimal("10.00"));

        assertEquals(BigDecimal.ZERO, strategy.calculateShippingFee(List.of()));
    }

    @Test
    @DisplayName("Reject an invalid flat fee")
    void constructor_invalidFee_throwsException() {
        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> new FlatRateShippingStrategy(null)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new FlatRateShippingStrategy(new BigDecimal("-0.01")))
        );
    }
}
