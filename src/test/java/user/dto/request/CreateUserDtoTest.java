package user.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import user.entities.UserRole;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateUserDtoTest {
    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    void validDtoHasNoConstraintViolations() {
        CreateUserRequest dto = new CreateUserRequest(
                "Nguyen Hai Anh",
                "haianh@example.com",
                "secret",
                "0912345678",
                UserRole.NORMAL_USER
        );

        assertTrue(validator.validate(dto).isEmpty());
    }

    @Test
    void invalidDtoReportsEveryInvalidField() {
        CreateUserRequest dto = new CreateUserRequest(
                " ",
                "invalid-email",
                " ",
                "123",
                null
        );

        Set<String> invalidProperties = validator.validate(dto).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(java.util.stream.Collectors.toSet());

        assertEquals(
                Set.of("username", "email", "password", "phoneNumber", "userRole"),
                invalidProperties
        );
    }
}
