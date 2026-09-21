package user.entities;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import user.entities.email.Email;

import java.util.stream.Stream;

public class EmailTest {
    // Verify that a valid email address is stored unchanged.
    @Test
    @DisplayName("Create an email from valid data")
    void constructEmail_validData() {
        String value = "haianh@example.com";

        Email email = new Email(value);

        Assertions.assertEquals(value, email.email());
    }

    // Verify that a null email address is rejected with the entity's exact message.
    @Test
    @DisplayName("Throw NullPointerException when email is null")
    void constructEmail_nullData() {
        NullPointerException exception = Assertions.assertThrows(
                NullPointerException.class,
                () -> new Email(null)
        );

        Assertions.assertEquals("Email cannot be null", exception.getMessage());
    }

    // Verify that blank and malformed email addresses report their matching validation message.
    @ParameterizedTest(name = "Reject {0}")
    @MethodSource("invalidEmailArguments")
    @DisplayName("Throw IllegalArgumentException when email is invalid")
    void constructEmail_invalidData(String scenario, String value, String expectedMessage) {
        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new Email(value)
        );

        Assertions.assertEquals(expectedMessage, exception.getMessage(), scenario);
    }

    static Stream<Arguments> invalidEmailArguments() {
        return Stream.of(
                Arguments.of("blank email", "   ", "Email cannot be empty"),
                Arguments.of("malformed email", "haianh.example.com", "Invalid Email")
        );
    }
}
