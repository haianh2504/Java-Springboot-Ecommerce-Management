package user.entities;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class RawPasswordTest {
    @Test
    @DisplayName("Create a raw password from valid data")
    void constructRawPassword_validData() {
        String value = "StrongPassword1!";

        RawPassword password = new RawPassword(value);

        Assertions.assertEquals(value, password.value());
    }

    @Test
    @DisplayName("Throw NullPointerException when password is null")
    void constructRawPassword_nullData() {
        NullPointerException exception = Assertions.assertThrows(
                NullPointerException.class,
                () -> new RawPassword(null)
        );

        Assertions.assertEquals("Password cannot be null", exception.getMessage());
    }

    @ParameterizedTest(name = "Reject password: {0}")
    @EmptySource
    @ValueSource(strings = {
            "       ",
            "Short1!",
            "lowercase1!",
            "UPPERCASE1!",
            "NoDigits!",
            "NoSpecial1",
            "Has Space1!"
    })
    @DisplayName("Throw IllegalArgumentException when password is not strong")
    void constructRawPassword_invalidData(String value) {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new RawPassword(value)
        );
    }

    @Test
    @DisplayName("Reject a password that exceeds BCrypt's 72-byte UTF-8 limit")
    void constructRawPassword_moreThan72Utf8Bytes_throwsIllegalArgumentException() {
        String value = "Aa1!" + "x".repeat(64) + "ééX";

        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new RawPassword(value)
        );

        Assertions.assertEquals(
                "Password must not exceed 72 bytes when encoded as UTF-8",
                exception.getMessage()
        );
    }
}
