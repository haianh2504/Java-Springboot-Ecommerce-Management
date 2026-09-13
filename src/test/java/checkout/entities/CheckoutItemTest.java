package checkout.entities;

import cart_item.entities.CartItem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import product.entities.DigitalProduct;
import product.entities.Product;
import product.entities.ProductName;
import product.entities.ProductStatus;
import product.entities.ProductType;

import java.math.BigDecimal;
import java.util.stream.Stream;

public class CheckoutItemTest {
    private static final CartItem CART_ITEM = new CartItem(1L, 2L, 1);
    private static final Product PRODUCT = new DigitalProduct(
            new ProductName("Java E-book"),
            10,
            new BigDecimal("25.00"),
            ProductStatus.ACTIVE,
            ProductType.DIGITAL
    );

    // Verify that each required checkout item component rejects null with the exact message.
    @ParameterizedTest(name = "Reject null {0}")
    @MethodSource("requiredNullArguments")
    @DisplayName("Throw NullPointerException when required data is null")
    void constructCheckoutItem_nullRequiredData(
            String fieldName, String expectedMessage, CartItem cartItem, Product product
    ) {
        NullPointerException exception = Assertions.assertThrows(
                NullPointerException.class,
                () -> new CheckoutItem(cartItem, product)
        );

        Assertions.assertEquals(expectedMessage, exception.getMessage(), fieldName);
    }

    static Stream<Arguments> requiredNullArguments() {
        return Stream.of(
                Arguments.of("cartItem", "cartItem cannot be null", null, PRODUCT),
                Arguments.of("product", "product cannot be null", CART_ITEM, null)
        );
    }

    @Test
    @DisplayName("Checkout item exposes the product price and calculates its line total")
    void pricing_validComponents_usesProductBasePrice()
    {
        // --GIVEN--
        CheckoutItem checkoutItem = new CheckoutItem(CART_ITEM, PRODUCT);

        // --WHEN--
        BigDecimal unitPrice = checkoutItem.unitPrice();
        BigDecimal lineTotal = checkoutItem.lineTotal();

        // --THEN--
        Assertions.assertAll(
                () -> Assertions.assertEquals(0, unitPrice.compareTo(new BigDecimal("25.00"))),
                () -> Assertions.assertEquals(0, lineTotal.compareTo(new BigDecimal("25.00")))
        );
    }
}
