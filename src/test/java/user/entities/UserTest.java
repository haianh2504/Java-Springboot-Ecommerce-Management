package user.entities;

import common.exception.business.detailed_exceptions.AccountBannedException;
import common.exception.business.detailed_exceptions.UserAlreadyActive;
import common.exception.business.detailed_exceptions.UserAlreadyBanned;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import user.entities.email.Email;
import user.entities.password_hash.PasswordHash;
import user.entities.person_name.PersonName;
import user.entities.phone_number.PhoneNumber;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private static final PasswordHash PASSWORD_HASH = new PasswordHash("$2342haHkacnd");
    private static final PersonName NAME = new PersonName("Phan Hai Anh");
    private static final Email EMAIL = new Email("haianh2504077@gmail.com");
    private static final PhoneNumber PHONE_NUMBER = new PhoneNumber("0912345678");
    private static final UserRole ROLE = UserRole.NORMAL_USER;

    private User createUser(PhoneNumber phoneNumber) {
        return User.builder()
                .passwordHash(PASSWORD_HASH)
                .name(NAME)
                .phoneNumber(phoneNumber)
                .email(EMAIL)
                .userRole(ROLE)
                .build();
    }

    private User createActiveUser() {
        return createUser(PHONE_NUMBER);
    }

    private User createPendingUser() {
        return createUser(null);
    }

    @Test
    @DisplayName("Should create an active user when a valid phone number is provided")
    void createUser_validPhoneNumber_createsActiveUser() {
        Instant beforeCreation = Instant.now();

        User user = createActiveUser();

        assertAll(
                () -> assertNull(user.getId()),
                () -> assertEquals(PASSWORD_HASH, user.getPasswordHash()),
                () -> assertEquals(NAME, user.getName()),
                () -> assertEquals(PHONE_NUMBER, user.getPhoneNumber()),
                () -> assertEquals(EMAIL, user.getEmail()),
                () -> assertSame(ROLE, user.getRole()),
                () -> assertSame(UserStatus.ACTIVE, user.getStatus()),
                () -> assertNotNull(user.getTimeCreated()),
                () -> assertFalse(user.getTimeCreated().isBefore(beforeCreation))
        );
    }

    @Test
    @DisplayName("Should create a pending user when no phone number is provided")
    void createUser_withoutPhoneNumber_createsPendingUser() {
        User user = createPendingUser();

        assertAll(
                () -> assertNull(user.getId()),
                () -> assertEquals(PASSWORD_HASH, user.getPasswordHash()),
                () -> assertEquals(NAME, user.getName()),
                () -> assertNull(user.getPhoneNumber()),
                () -> assertEquals(EMAIL, user.getEmail()),
                () -> assertSame(ROLE, user.getRole()),
                () -> assertSame(UserStatus.PENDING, user.getStatus()),
                () -> assertNotNull(user.getTimeCreated())
        );
    }

    @Test
    @DisplayName("Should reject a user when the password hash is missing")
    void createUser_missingPasswordHash_throwsNullPointerException() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> User.builder()
                        .passwordHash(null)
                        .name(NAME)
                        .email(EMAIL)
                        .userRole(ROLE)
                        .build()
        );

        assertEquals("User passwordHash cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should reject a user when the name is missing")
    void createUser_missingName_throwsNullPointerException() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> User.builder()
                        .passwordHash(PASSWORD_HASH)
                        .name(null)
                        .email(EMAIL)
                        .userRole(ROLE)
                        .build()
        );

        assertEquals("User name cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should reject a user when the email is missing")
    void createUser_missingEmail_throwsNullPointerException() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> User.builder()
                        .passwordHash(PASSWORD_HASH)
                        .name(NAME)
                        .email(null)
                        .userRole(ROLE)
                        .build()
        );

        assertEquals("User email cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should reject a user when the role is missing")
    void createUser_missingRole_throwsNullPointerException() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> User.builder()
                        .passwordHash(PASSWORD_HASH)
                        .name(NAME)
                        .email(EMAIL)
                        .userRole(null)
                        .build()
        );

        assertEquals("User role cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should activate a pending user with a valid phone number")
    void activate_pendingUser_setsPhoneNumberAndActiveStatus() {
        User user = createPendingUser();

        user.activate(PHONE_NUMBER);

        assertAll(
                () -> assertEquals(PHONE_NUMBER, user.getPhoneNumber()),
                () -> assertSame(UserStatus.ACTIVE, user.getStatus())
        );
    }

    @Test
    @DisplayName("Should reject activation when the user is already active")
    void activate_activeUser_throwsUserAlreadyActive() {
        User user = createActiveUser();

        UserAlreadyActive exception = assertThrows(
                UserAlreadyActive.class,
                () -> user.activate(new PhoneNumber("0987654321"))
        );

        assertAll(
                () -> assertEquals("User [Phan Hai Anh] is already active", exception.getMessage()),
                () -> assertEquals(PHONE_NUMBER, user.getPhoneNumber()),
                () -> assertSame(UserStatus.ACTIVE, user.getStatus())
        );
    }

    @Test
    @DisplayName("Should reject activation when the phone number is null")
    void activate_pendingUserWithNullPhone_throwsNullPointerException() {
        User user = createPendingUser();

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> user.activate(null)
        );

        assertAll(
                () -> assertEquals("PhoneNumber cannot be null", exception.getMessage()),
                () -> assertNull(user.getPhoneNumber()),
                () -> assertSame(UserStatus.PENDING, user.getStatus())
        );
    }

    @Test
    @DisplayName("Should ban a user")
    void banned_activeUser_setsBannedStatus() {
        User user = createActiveUser();

        user.banned();

        assertSame(UserStatus.BANNED, user.getStatus());
    }

    @Test
    @DisplayName("Should reject banning an already banned user")
    void banned_alreadyBannedUser_throwsUserAlreadyBanned() {
        User user = createActiveUser();
        user.banned();

        UserAlreadyBanned exception = assertThrows(UserAlreadyBanned.class, user::banned);

        assertAll(
                () -> assertEquals(
                        "Cannot perform action on user [Phan Hai Anh]: account is already banned",
                        exception.getMessage()
                ),
                () -> assertSame(UserStatus.BANNED, user.getStatus())
        );
    }

    @Test
    @DisplayName("Should reject authorization for a banned user")
    void authorize_bannedUser_throwsAccountBannedException() {
        User user = createActiveUser();
        user.banned();

        AccountBannedException exception = assertThrows(AccountBannedException.class, user::authorize);

        assertAll(
                () -> assertEquals("This account has already been banned.", exception.getMessage()),
                () -> assertSame(UserRole.NORMAL_USER, user.getRole()),
                () -> assertSame(UserStatus.BANNED, user.getStatus())
        );
    }

    @Test
    @DisplayName("Should authorize a non-banned user")
    void authorize_nonBannedUser_changesRoleToAdmin() {
        User activeUser = createActiveUser();
        User pendingUser = createPendingUser();

        activeUser.authorize();
        pendingUser.authorize();

        assertAll(
                () -> assertSame(UserRole.ADMIN, activeUser.getRole()),
                () -> assertSame(UserRole.ADMIN, pendingUser.getRole())
        );
    }

    @Test
    @DisplayName("Should change the user's name")
    void changeName_validName_updatesName() {
        User user = createActiveUser();
        PersonName newName = new PersonName("Phan Hai Dang");

        user.changeName(newName);

        assertEquals(newName, user.getName());
    }

    @Test
    @DisplayName("Should reject a null name")
    void changeName_null_throwsNullPointerException() {
        User user = createActiveUser();

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> user.changeName(null)
        );

        assertAll(
                () -> assertEquals("New username cannot be null", exception.getMessage()),
                () -> assertEquals(NAME, user.getName())
        );
    }

    @Test
    @DisplayName("Should change the user's email")
    void changeEmail_validEmail_updatesEmail() {
        User user = createActiveUser();
        Email newEmail = new Email("new.address@gmail.com");

        user.changeEmail(newEmail);

        assertEquals(newEmail, user.getEmail());
    }

    @Test
    @DisplayName("Should reject a null email")
    void changeEmail_null_throwsNullPointerException() {
        User user = createActiveUser();

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> user.changeEmail(null)
        );

        assertAll(
                () -> assertEquals("New email cannot be null", exception.getMessage()),
                () -> assertEquals(EMAIL, user.getEmail())
        );
    }

    @Test
    @DisplayName("Should change the user's phone number")
    void changePhoneNumber_differentValidNumber_updatesPhoneNumber() {
        User user = createActiveUser();
        PhoneNumber newPhoneNumber = new PhoneNumber("0987654321");

        user.changePhoneNumber(newPhoneNumber);

        assertAll(
                () -> assertEquals(newPhoneNumber, user.getPhoneNumber()),
                () -> assertNotEquals(PHONE_NUMBER, user.getPhoneNumber()),
                () -> assertSame(UserStatus.ACTIVE, user.getStatus())
        );
    }

    @Test
    @DisplayName("Should reject a null phone number")
    void changePhoneNumber_null_throwsNullPointerException() {
        User user = createActiveUser();

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> user.changePhoneNumber(null)
        );

        assertAll(
                () -> assertEquals("New phoneNumber cannot be null", exception.getMessage()),
                () -> assertEquals(PHONE_NUMBER, user.getPhoneNumber())
        );
    }
}
