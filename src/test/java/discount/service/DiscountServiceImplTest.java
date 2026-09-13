package discount.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DiscountServiceImplTest {
    private final DiscountService discountService = new DiscountServiceImpl();

    @ParameterizedTest(name = "subtotal {0} produces discount {1}")
    @MethodSource("discountCases")
    @DisplayName("Discount calculation applies the configured subtotal thresholds")
    void calculateDiscountAmount_validSubtotal_returnsExpectedDiscount(
            BigDecimal subTotal,
            BigDecimal expectedDiscount
    )
    {
        // --GIVEN--
        // Values are supplied by discountCases().

        // --WHEN--
        BigDecimal actualDiscount = discountService.calculateDiscountAmount(subTotal);

        // --THEN--
        assertEquals(0, actualDiscount.compareTo(expectedDiscount));
    }

    static Stream<Arguments> discountCases()
    {
        return Stream.of(
                Arguments.of(new BigDecimal("999999"), BigDecimal.ZERO),
                Arguments.of(new BigDecimal("1000000"), new BigDecimal("80000")),
                Arguments.of(new BigDecimal("5000000"), new BigDecimal("750000"))
        );
    }

    @Test
    @DisplayName("Discount calculation rejects a null subtotal")
    void calculateDiscountAmount_nullSubtotal_throwsNullPointerException()
    {
        // --GIVEN--
        BigDecimal subTotal = null;

        // --WHEN--
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> discountService.calculateDiscountAmount(subTotal)
        );

        // --THEN--
        assertEquals("subTotal must not be null", exception.getMessage());
    }

    @Test
    @DisplayName("Discount calculation rejects a negative subtotal")
    void calculateDiscountAmount_negativeSubtotal_throwsIllegalArgumentException()
    {
        // --GIVEN--
        BigDecimal subTotal = new BigDecimal("-0.01");

        // --WHEN--
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> discountService.calculateDiscountAmount(subTotal)
        );

        // --THEN--
        assertEquals("subTotal must not be negative", exception.getMessage());
    }
}
