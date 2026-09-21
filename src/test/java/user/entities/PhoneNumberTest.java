package user.entities;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import user.entities.phone_number.PhoneNumber;

public class PhoneNumberTest {
    // Verify that a valid Vietnamese phone number is stored unchanged.
    @Test
    @DisplayName("Create a phone number from valid data")
    void constructPhoneNumber_validData() {
        String value = "0912345678";

        PhoneNumber phoneNumber = new PhoneNumber(value);

        Assertions.assertEquals(value, phoneNumber.phoneNumber());
    }

    // Verify that a null phone number is rejected with the entity's exact message.
    @Test
    @DisplayName("Throw NullPointerException when phone number is null")
    void constructPhoneNumber_nullData() {
        NullPointerException exception = Assertions.assertThrows(
                NullPointerException.class,
                () -> new PhoneNumber(null)
        );

        Assertions.assertEquals("PhoneNumber cannot be null", exception.getMessage());
    }

    // Verify that a phone number outside the accepted Vietnamese format is rejected.
    @Test
    @DisplayName("Throw IllegalArgumentException when phone number is invalid")
    void constructPhoneNumber_invalidData() {
        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new PhoneNumber("1234567899")
        );

        Assertions.assertEquals("Invalid PhoneNumber in VietNam", exception.getMessage());
    }
}
