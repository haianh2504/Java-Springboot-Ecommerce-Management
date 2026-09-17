package product.entities;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class ProductNameTest {
    @Nested
    @DisplayName("Create a product name")
    class CreateProductName {
        // Verify that valid product names are trimmed before being stored.
        @Test
        @DisplayName("Create and trim a valid product name")
        void constructProductName_validData() {
            ProductName productName = new ProductName("  Mechanical Keyboard  ");

            Assertions.assertEquals("Mechanical Keyboard", productName.name());
        }

        // Verify that a null product name is rejected with the entity's exact message.
        @Test
        @DisplayName("Throw NullPointerException when product name is null")
        void constructProductName_nullData() {
            NullPointerException exception = Assertions.assertThrows(
                    NullPointerException.class,
                    () -> new ProductName(null)
            );

            Assertions.assertEquals("ProductName cannot be null", exception.getMessage());
        }

        // Verify that a blank product name is rejected after whitespace is trimmed.
        @Test
        @DisplayName("Throw IllegalArgumentException when product name is blank")
        void constructProductName_blankData() {
            IllegalArgumentException exception = Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> new ProductName("   ")
            );

            Assertions.assertEquals("ProductName cannot be empty", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Replace a product name")
    class ReplaceProductName {
        // A record is immutable, so changing the value creates a new ProductName.
        @Test
        @DisplayName("Create a replacement product name using valid data")
        void replaceName_validData() {
            ProductName originalName = new ProductName("Keyboard");
            ProductName replacementName = new ProductName("Gaming Keyboard");

            Assertions.assertAll(
                    () -> Assertions.assertEquals("Keyboard", originalName.name()),
                    () -> Assertions.assertEquals("Gaming Keyboard", replacementName.name()),
                    () -> Assertions.assertNotSame(originalName, replacementName)
            );
        }

        @Test
        @DisplayName("Throw NullPointerException when replacement name is null")
        void replaceName_nullData() {
            NullPointerException exception = Assertions.assertThrows(
                    NullPointerException.class,
                    () -> new ProductName(null)
            );

            Assertions.assertEquals("ProductName cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Throw IllegalArgumentException when replacement name is blank")
        void replaceName_blankData() {
            IllegalArgumentException exception = Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> new ProductName("   ")
            );

            Assertions.assertEquals("ProductName cannot be empty", exception.getMessage());
        }
    }
}
