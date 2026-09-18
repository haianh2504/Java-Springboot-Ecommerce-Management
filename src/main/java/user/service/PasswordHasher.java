package user.service;

import user.entities.PasswordHash;
import user.entities.RawPassword;

public interface PasswordHasher {
    PasswordHash hash(RawPassword rawPassword);

    boolean matches(
            RawPassword rawPassword,
            PasswordHash passwordHash
    );
}
