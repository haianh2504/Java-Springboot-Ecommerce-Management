package user.entities;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import user.entities.person_name.PersonName;

import java.util.stream.Stream;

public class PersonNameTest {
    // Verify that a valid person name is trimmed and stored correctly.
    @Test
    @DisplayName("Create and trim a valid person name")
    void constructPersonName_validData() {
        PersonName personName = new PersonName("  Nguyễn Văn An  ");

        Assertions.assertEquals("Nguyễn Văn An", personName.name());
    }

    // Verify that a null person name is rejected with the entity's exact message.
    @Test
    @DisplayName("Throw NullPointerException when person name is null")
    void constructPersonName_nullData() {
        NullPointerException exception = Assertions.assertThrows(
                NullPointerException.class,
                () -> new PersonName(null)
        );

        Assertions.assertEquals("Name cannot be null", exception.getMessage());
    }

    // Verify that empty and malformed names report their matching validation message.
    @ParameterizedTest(name = "Reject {0}")
    @MethodSource("invalidPersonNameArguments")
    @DisplayName("Throw IllegalArgumentException when person name is invalid")
    void constructPersonName_invalidData(
            String scenario, String value, String expectedMessage
    ) {
        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new PersonName(value)
        );

        Assertions.assertEquals(expectedMessage, exception.getMessage(), scenario);
    }

    static Stream<Arguments> invalidPersonNameArguments() {
        return Stream.of(
                Arguments.of("empty name", "   ", "Name cannot be empty"),
                Arguments.of("name containing digits", "Nguyen Van An123", "Invalid person name"),
                Arguments.of("name containing symbols", "Nguyen@An", "Invalid person name")
        );
    }
}
