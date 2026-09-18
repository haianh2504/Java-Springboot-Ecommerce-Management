package user.entities;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.regex.Pattern;

public final class RawPassword {
    private static final int MAX_BCRYPT_PASSWORD_BYTES = 72;
    private static final Pattern STRONG_PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d\\s])\\S{8,72}$"
    );
    private final String value;

    public RawPassword(String value) {
        this.value = Objects.requireNonNull(value, "Password cannot be null");

        if (!STRONG_PASSWORD_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException(
                    "Password must be 8-72 characters long and contain at least "
                            + "one lowercase letter, one uppercase letter, one digit, "
                            + "one special character, and no whitespace"
            );
        }

        if (value.getBytes(StandardCharsets.UTF_8).length > MAX_BCRYPT_PASSWORD_BYTES) {
            throw new IllegalArgumentException(
                    "Password must not exceed 72 bytes when encoded as UTF-8"
            );
        }
    }
    public String value() {
        return value;
    }
}
