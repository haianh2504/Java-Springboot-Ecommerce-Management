package user.service;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import user.entities.password_hash.PasswordHash;
import user.entities.RawPassword;

// chưa học spring security
import java.util.Objects;

@Service
public final class BCryptPasswordHasher implements PasswordHasher {
    private static final int LOG_ROUNDS = 10;

    @Override
    public PasswordHash hash(RawPassword rawPassword) {
        Objects.requireNonNull(rawPassword, "Raw password cannot be null");
        String hashedPassword = BCrypt.hashpw(
                rawPassword.value(),
                BCrypt.gensalt(LOG_ROUNDS)
        );
        return new PasswordHash(hashedPassword);
    }
    @Override
    public boolean matches(RawPassword rawPassword, PasswordHash passwordHash) {
        Objects.requireNonNull(rawPassword, "Raw password cannot be null");
        Objects.requireNonNull(passwordHash, "Password hash cannot be null");
        return BCrypt.checkpw(
                rawPassword.value(),
                passwordHash.passwordHash()
        );
    }
}
