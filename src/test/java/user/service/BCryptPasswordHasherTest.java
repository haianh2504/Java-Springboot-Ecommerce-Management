package user.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import user.entities.PasswordHash;
import user.entities.RawPassword;

class BCryptPasswordHasherTest {
    private final PasswordHasher passwordHasher = new BCryptPasswordHasher();

    @Test
    @DisplayName("Hash a raw password and verify it against the resulting BCrypt hash")
    void hashAndMatch_validPassword_success() {
        RawPassword rawPassword = new RawPassword("StrongPassword1!");

        PasswordHash passwordHash = passwordHasher.hash(rawPassword);

        Assertions.assertNotEquals(rawPassword.value(), passwordHash.passwordHash());
        Assertions.assertTrue(passwordHasher.matches(rawPassword, passwordHash));
    }

    @Test
    @DisplayName("Generate different salted hashes for the same raw password")
    void hash_samePassword_generatesDifferentHashes() {
        RawPassword rawPassword = new RawPassword("StrongPassword1!");

        PasswordHash firstHash = passwordHasher.hash(rawPassword);
        PasswordHash secondHash = passwordHasher.hash(rawPassword);

        Assertions.assertNotEquals(firstHash, secondHash);
        Assertions.assertTrue(passwordHasher.matches(rawPassword, firstHash));
        Assertions.assertTrue(passwordHasher.matches(rawPassword, secondHash));
    }

    @Test
    @DisplayName("Reject a different raw password")
    void matches_differentPassword_returnsFalse() {
        PasswordHash passwordHash = passwordHasher.hash(
                new RawPassword("StrongPassword1!")
        );

        boolean matches = passwordHasher.matches(
                new RawPassword("DifferentPassword2@"),
                passwordHash
        );

        Assertions.assertFalse(matches);
    }
}
