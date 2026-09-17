package product.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {
    private static final ProductName NAME = new ProductName("Mechanical Keyboard");
    private static final BigDecimal PRICE = new BigDecimal("89.99");

    @Test
    @DisplayName("Create a new product with valid values")
    void createProduct_validValues_initializesProduct() {
        Product product = new Product(NAME, 10, PRICE, ProductStatus.INACTIVE);

        assertAll(
                () -> assertNull(product.getId()),
                () -> assertEquals(NAME, product.getName()),
                () -> assertEquals(10, product.getQuantity()),
                () -> assertEquals(PRICE, product.getBasePrice()),
                () -> assertEquals(ProductStatus.INACTIVE, product.getStatus()),
                () -> assertNotNull(product.getCreatedAt())
        );
    }

    @Test
    @DisplayName("Hydrate a persisted product with valid database values")
    void hydrateProduct_validValues_initializesProduct() {
        Instant createdAt = Instant.parse("2026-09-10T00:00:00Z");

        Product product = new Product(
                1L, NAME, 10, PRICE, ProductStatus.ACTIVE, createdAt
        );

        assertAll(
                () -> assertEquals(1L, product.getId()),
                () -> assertEquals(NAME, product.getName()),
                () -> assertEquals(10, product.getQuantity()),
                () -> assertEquals(PRICE, product.getBasePrice()),
                () -> assertEquals(ProductStatus.ACTIVE, product.getStatus()),
                () -> assertEquals(createdAt, product.getCreatedAt())
        );
    }

    @Test
    @DisplayName("Reject invalid product values")
    void createProduct_invalidValues_throwsException() {
        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> new Product(null, 1, PRICE, ProductStatus.ACTIVE)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new Product(NAME, -1, PRICE, ProductStatus.ACTIVE)),
                () -> assertThrows(NullPointerException.class,
                        () -> new Product(NAME, 1, null, ProductStatus.ACTIVE)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new Product(NAME, 1, BigDecimal.ZERO, ProductStatus.ACTIVE)),
                () -> assertThrows(NullPointerException.class,
                        () -> new Product(NAME, 1, PRICE, null))
        );
    }

    @Test
    @DisplayName("Change mutable product fields")
    void changeProduct_validValues_updatesState() {
        Product product = new Product(NAME, 10, PRICE, ProductStatus.INACTIVE);
        ProductName newName = new ProductName("Wireless Keyboard");

        product.changeProductName(newName);
        product.setStockQuantity(15);
        product.setBasePrice(new BigDecimal("99.99"));
        product.activate();

        assertAll(
                () -> assertEquals(newName, product.getName()),
                () -> assertEquals(15, product.getQuantity()),
                () -> assertEquals(new BigDecimal("99.99"), product.getBasePrice()),
                () -> assertEquals(ProductStatus.ACTIVE, product.getStatus())
        );
    }
}
