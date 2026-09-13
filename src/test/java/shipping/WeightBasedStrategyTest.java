package shipping;

import cart_item.entities.CartItem;
import checkout.entities.CheckoutItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import product.entities.DigitalProduct;
import product.entities.PhysicalProduct;
import product.entities.Product;
import product.entities.ProductName;
import product.entities.ProductStatus;
import product.entities.ProductType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WeightBasedStrategyTest {
    @Test
    @DisplayName("Shipping fee uses physical product weight and quantity while excluding digital products")
    void calculateShippingFee_mixedProducts_chargesOnlyPhysicalWeight()
    {
        // --GIVEN--
        WeightBasedStrategy strategy = new WeightBasedStrategy(new BigDecimal("20.00"));
        CartItem physicalCartItem = new CartItem(1L, 10L, 101L, 2);
        CartItem digitalCartItem = new CartItem(2L, 10L, 102L, 3);
        Product physicalProduct = new PhysicalProduct(
                101L,
                new ProductName("Keyboard"),
                10,
                new BigDecimal("100.00"),
                ProductStatus.ACTIVE,
                ProductType.PHYSICAL,
                Instant.parse("2026-09-10T00:00:00Z"),
                new BigDecimal("1.50")
        );
        Product digitalProduct = new DigitalProduct(
                102L,
                new ProductName("E-book"),
                10,
                new BigDecimal("50.00"),
                ProductStatus.ACTIVE,
                ProductType.DIGITAL,
                Instant.parse("2026-09-10T00:00:00Z")
        );

        // --WHEN--
        BigDecimal shippingFee = strategy.calculateShippingFee(List.of(
                new CheckoutItem(physicalCartItem, physicalProduct),
                new CheckoutItem(digitalCartItem, digitalProduct)
        ));

        // --THEN--
        assertEquals(0, shippingFee.compareTo(new BigDecimal("60.0000")));
    }
}
