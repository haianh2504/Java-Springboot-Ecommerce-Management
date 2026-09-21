package user.service;
import exception.business.detailed_exceptions.EmailAlreadyInUseException;
import exception.business.detailed_exceptions.PhoneAlreadyInUseException;
import exception.business.detailed_exceptions.AccountBannedException;
import exception.business.detailed_exceptions.UserNotAuthorizedException;
import exception.business.detailed_exceptions.UserAlreadyActive;
import exception.resource.detailed_exceptions.UserNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import user.entities.*;
import user.entities.email.Email;
import user.entities.password_hash.PasswordHash;
import user.entities.person_name.PersonName;
import user.entities.phone_number.PhoneNumber;
import user.repository.UserRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserManagementServiceImplTest {
    // giả lập repository và service thuộc tính
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordHasher passwordHasher;
    // Ịnject mocks ở trên <- tự động inject mocks vào nhờ Extension
    @InjectMocks
    private UserManageServiceImpl userManagementServiceImpl;

    // Persisted User
    private User createPersistedActiveUser()
    {
        return new User(
                1L,
                new PasswordHash("$2342haHkacnd"),
                new PersonName("Phan Hai Anh"),
                new PhoneNumber("0912345678"),
                new Email("haianh2504077@gmail.com"),
                UserRole.NORMAL_USER,
                UserStatus.ACTIVE,
                Instant.parse("2026-09-10T00:00:00Z")
        );
    }
    private User createPersistedPendingUser()
    {
        return new User(
                2L,
                new PasswordHash("$2342haHkacnd"),
                new PersonName("Phan Hai Anh"),
                null,
                new Email("haianh2504077@gmail.com"),
                UserRole.NORMAL_USER,
                UserStatus.PENDING,
                Instant.parse("2026-09-10T00:00:00Z")
        );
    }
    private User createPersistedAdminUser()
    {
        return new User(
                10L,
                new PasswordHash("$2342haHkacnd"),
                new PersonName("Admin User"),
                new PhoneNumber("0987654321"),
                new Email("admin@gmail.com"),
                UserRole.ADMIN,
                UserStatus.ACTIVE,
                Instant.parse("2026-09-10T00:00:00Z")
        );
    }
    // create new user with valid RawPassword, PersonName, PhoneNumber, Email and UserRole -> ACTIVE
    @Test
    @DisplayName("create new user with valid information then user is saved and returned ACTIVE user")
    void createUser_validInformation_savedActiveUser()
    {
        // -- GIVEN --
        // set up
        RawPassword rawPassword = new RawPassword("StrongPassword1!");
        PasswordHash passwordHash = new PasswordHash("$2342haHkacnd");
        PersonName personName = new PersonName("Phan Hai Anh");
        Email email = new Email("haianh2504077@gmail.com");
        PhoneNumber phoneNumber = new PhoneNumber("0912345678");
        UserRole role = UserRole.NORMAL_USER;
        User savedUser = createPersistedActiveUser();
        // stubbing 1: email chưa tồn tại trong DB -> Optional.empty(
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordHasher.hash(rawPassword)).thenReturn(passwordHash);
        // stubbing 2: khi save user thì sẽ return new User có id
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // -- WHEN --
        User actualUser = userManagementServiceImpl.createUser(
                rawPassword,
                personName,
                phoneNumber,
                email,
                role
        );

        // -- THEN --
        assertAll(
                () -> assertNotNull(actualUser),
                () -> assertSame(savedUser, actualUser),
                () -> assertEquals(1L, actualUser.getId()),
                () -> assertEquals(passwordHash, actualUser.getPasswordHash()),
                () -> assertEquals(personName, actualUser.getName()),
                () -> assertEquals(phoneNumber, actualUser.getPhoneNumber()),
                () -> assertEquals(email, actualUser.getEmail()),
                () -> assertSame(role, actualUser.getRole()),
                () -> assertSame(UserStatus.ACTIVE, actualUser.getStatus())
        );
        // verify: findByEmail() chi chay 1 lan
        verify(userRepository, times(1)).findByEmail(email);
        verify(passwordHasher, times(1)).hash(rawPassword);
        // verify: save() chi chay 1 lan
        verify(userRepository, times(1)).save(argThat(createdUser ->
                createdUser.getId() == null
                        && createdUser.getPasswordHash().equals(passwordHash)
                        && createdUser.getName().equals(personName)
                        && createdUser.getPhoneNumber().equals(phoneNumber)
                        && createdUser.getEmail().equals(email)
                        && createdUser.getRole() == role
                        && createdUser.getStatus() == UserStatus.ACTIVE
        ));
    }

    // create user with valid information exception null Phone number -> PENDING
    @Test
    @DisplayName("create user with null phone number, save and return PENDING user")
    void createUser_nullPhoneNumber_pendingStatus()
    {
        // -- GIVEN --
        RawPassword rawPassword = new RawPassword("StrongPassword1!");
        PasswordHash passwordHash = new PasswordHash("$2342haHkacnd");
        PersonName personName = new PersonName("Phan Hai Anh");
        Email email = new Email("haianh2504077@gmail.com");
        UserRole role = UserRole.NORMAL_USER;

        User savedUser = createPersistedPendingUser();

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordHasher.hash(rawPassword)).thenReturn(passwordHash);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // -- WHEN --
        User actualUser = userManagementServiceImpl.createUser(
                rawPassword,
                personName,
                null,
                email,
                role
        );

        // -- THEN --
        verify(userRepository, times(1)).findByEmail(email);
        verify(passwordHasher, times(1)).hash(rawPassword);
        verify(userRepository, times(1)).save(argThat(createdUser ->
                createdUser.getId() == null
                        && createdUser.getPasswordHash().equals(passwordHash)
                        && createdUser.getName().equals(personName)
                        && createdUser.getPhoneNumber() == null
                        && createdUser.getEmail().equals(email)
                        && createdUser.getRole() == role
                        && createdUser.getStatus() == UserStatus.PENDING
        ));
        assertAll(
                () -> assertNotNull(actualUser),
                () -> assertSame(savedUser, actualUser),
                () -> assertEquals(2L, actualUser.getId()),
                () -> assertEquals(passwordHash, actualUser.getPasswordHash()),
                () -> assertEquals(personName, actualUser.getName()),
                () -> assertNull(actualUser.getPhoneNumber()),
                () -> assertEquals(email, actualUser.getEmail()),
                () -> assertSame(role, actualUser.getRole()),
                () -> assertSame(UserStatus.PENDING, actualUser.getStatus())
        );

    }
    // create user with email already in use
    @Test
    @DisplayName("create user with email already in use throw exception")
    void createUser_emailAlreadyInUse_throwException()
    {
        // -- GIVEN --
        RawPassword rawPassword = new RawPassword("StrongPassword1!");
        PersonName personName = new PersonName("Phan Hai Anh");
        Email email = new Email("haianh2504077@gmail.com"); // already in use
        PhoneNumber phoneNumber = new PhoneNumber("0912345678");
        UserRole role = UserRole.NORMAL_USER;

        User existingUser = createPersistedActiveUser();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

        // -- WHEN --
        EmailAlreadyInUseException exception = assertThrows(
                EmailAlreadyInUseException.class,
                () -> userManagementServiceImpl.createUser(
                        rawPassword,
                        personName,
                        phoneNumber,
                        email,
                        role
                )
        );
        // -- THEN --
        assertEquals("This email has already been used", exception.getMessage());
        verify(userRepository, times(1)).findByEmail(email);
        verifyNoInteractions(passwordHasher);
        verify(userRepository, never()).save(any(User.class));
    }

    // create user with one null required argument
    @ParameterizedTest(name = "{index}: null {0}")
    @MethodSource("nullRequiredArguments")
    @DisplayName("Create user with a null required argument throws NullPointerException")
    void createUser_nullRequiredArguments_throwNullPointerException(
            String nullArgument,
            RawPassword rawPassword,
            PersonName personName,
            PhoneNumber phoneNumber,
            Email email,
            UserRole role,
            String expectedMessage
    )
    {
        // -- GIVEN --
        // Arguments are supplied by nullRequiredArguments().

        // -- WHEN --
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> userManagementServiceImpl.createUser(
                        rawPassword,
                        personName,
                        phoneNumber,
                        email,
                        role
                )
        );

        // -- THEN --
        assertEquals(expectedMessage, exception.getMessage());
        verifyNoInteractions(userRepository);
    }

    static Stream<Arguments> nullRequiredArguments()
    {
        RawPassword rawPassword = new RawPassword("StrongPassword1!");
        PersonName personName = new PersonName("Phan Hai Anh");
        PhoneNumber phoneNumber = new PhoneNumber("0912345678");
        Email email = new Email("haianh2504077@gmail.com");
        UserRole role = UserRole.NORMAL_USER;

        return Stream.of(
                Arguments.of("raw password", null, personName, phoneNumber, email, role,
                        "Raw password cannot be null"),
                Arguments.of("name", rawPassword, null, phoneNumber, email, role,
                        "Name cannot be null"),
                Arguments.of("email", rawPassword, personName, phoneNumber, null, role,
                        "Email cannot be null"),
                Arguments.of("role", rawPassword, personName, phoneNumber, email, null,
                        "User role cannot be null")
        );
    }

    // find user by valid ID and return persisted User
    @Test
    @DisplayName("Find user with valid ID provided return persisted user object")
    void findUserBy_IdProvided_returnPersistedUserObject()
    {
        // -- GIVEN --
        User persistedUser = createPersistedActiveUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(persistedUser));

        // -- WHEN --
        User actualUser = userManagementServiceImpl.findUserById(1L);

        // -- THEN --
        assertSame(persistedUser, actualUser);
        verify(userRepository, times(1)).findById(1L);
    }

    // find user by null ID and throw null exception
    @Test
    @DisplayName("Find user with null ID provided throw null exception")
    void findUserBy_IdNullProvided_throwNullPointerException()
    {
        // -- GIVEN --
        Long userId = null;

        // -- WHEN --
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> userManagementServiceImpl.findUserById(userId)
        );

        // -- THEN --
        assertEquals("UserId cannot be null", exception.getMessage());
        verifyNoInteractions(userRepository);
    }

    // find user by valid ID but throw UserNotFoundException
    @Test
    @DisplayName("Find user with valid ID provided but throw UserNotFoundException")
    void findUserBy_IdNotFoundProvided_throwUserNotFoundException()
    {
        // -- GIVEN --
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // -- WHEN --
        UserNotFoundException userNotFoundException = assertThrows(
                UserNotFoundException.class,
                ()-> userManagementServiceImpl.findUserById(userId)
        );

        // -- THEN --
        assertEquals(String.format("User with id %d not found", userId), userNotFoundException.getMessage());
        verify(userRepository, times(1)).findById(userId);
    }

    // Find user by valid email and return persisted user object
    @Test
    @DisplayName("Find user with valid email provided and return persisted user")
    void findUserby_EmailProvided_returnPersistedUserObject()
    {
        // -- GIVEN --
        User persistedUser = createPersistedActiveUser();
        when(userRepository.findByEmail(persistedUser.getEmail())).thenReturn(Optional.of(persistedUser));

        // -- WHEN --
        User actualUser = userManagementServiceImpl.findUserByEmail(persistedUser.getEmail());

        // -- THEN --
        assertSame(persistedUser, actualUser);
        verify(userRepository, times(1)).findByEmail(persistedUser.getEmail());
    }

    // Find user by null email and throw null pointer exception
    @Test
    @DisplayName("Find user with null email provided throw null pointer exception")
    void findUserBy_nullEmailProvided_throwNullPointerException()
    {
        // -- GIVEN --
        Email email = null;

        // -- WHEN --
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> userManagementServiceImpl.findUserByEmail(email)
        );

        // -- THEN --
        assertEquals("User email cannot be null", exception.getMessage());
        verifyNoInteractions(userRepository);
    }

    // Find user by valid email but throw UserNotFoundException
    @Test
    @DisplayName("Find user with valid email provided but throw UserNotFoundException")
    void findUserBy_emailNotFoundProvided_throwUserNotFoundException()
    {
        // -- GIVEN --
        Email validEmail = new Email("haianh2504077@gmail.com");
        when(userRepository.findByEmail(validEmail)).thenReturn(Optional.empty());

        // -- WHEN --
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userManagementServiceImpl.findUserByEmail(validEmail)
        );

        // -- THEN --
        assertEquals(String.format("User with email %s not found", validEmail), exception.getMessage());
        verify(userRepository, times(1)).findByEmail(validEmail);
    }

    // activate user with valid arguments and found -> activate
    @Test
    @DisplayName("Activate persisted user with valid arguments provided successfully")
    void activatePersistedUser_validArguments_successfully()
    {
        // -- GIVEN --
        User persistedUser = createPersistedPendingUser();
        PhoneNumber validPhoneNumber = new PhoneNumber("0912345678");
        when(userRepository.findById(persistedUser.getId())).thenReturn(Optional.of(persistedUser));
        when(userRepository.update(persistedUser)).thenReturn(persistedUser);

        // -- WHEN --
        User updatedUser = userManagementServiceImpl.activateUser(persistedUser.getId(), validPhoneNumber);

        // -- THEN --
        assertAll(
                () -> assertSame(persistedUser, updatedUser),
                () -> assertSame(UserStatus.ACTIVE, updatedUser.getStatus()),
                () -> assertEquals(validPhoneNumber, updatedUser.getPhoneNumber())
        );
        verify(userRepository, times(1)).findById(persistedUser.getId());
        verify(userRepository, times(1)).update(persistedUser);
    }

    // activate user with valid arguments but not found, throw UserNotFoundException
    @Test
    @DisplayName("Activate not found user with valid arguments provided and throw UserNotFoundException")
    void  activateUserBy_IdNotFound_throwUserNotFoundException()
    {
        // -- GIVEN --
        Long id = 5L;
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        // -- WHEN --
        UserNotFoundException userNotFoundException = assertThrows(
                UserNotFoundException.class,
                () -> userManagementServiceImpl.activateUser(id, new PhoneNumber("0912345678"))
        );

        // -- THEN --
        assertEquals(String.format("User with id %d not found", id), userNotFoundException.getMessage());
        verify(userRepository, times(1)).findById(id);
        verify(userRepository, never()).update(any(User.class));
    }

    // activate user with null arguments and throw NullPointerException
    @ParameterizedTest(name = "{index}: null {0}")
    @MethodSource("nullArgumentsProvided")
    @DisplayName("Activate user with null arguments provided and throw NullPointerException")
    void activateUserBy_nullArguments_throwNullPointerException(
            String nullArgument, Long userId, PhoneNumber phoneNumber, String exceptionMessage)
    {
        // -- GIVEN --
        // Arguments are supplied by nullArgumentsProvided().

        // -- WHEN --
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                ()-> userManagementServiceImpl.activateUser(userId, phoneNumber)
        );

        // -- THEN --
        assertEquals(exceptionMessage, exception.getMessage());
        verifyNoInteractions(userRepository);
    }
    private static Stream<Arguments> nullArgumentsProvided()
    {
        return Stream.of(
                Arguments.of("user ID", null, new PhoneNumber("0912345678"), "UserId cannot be null"),
                Arguments.of("phone number", 5L, null, "Phone number cannot be null")
        );
    }

    // activate an already active user
    @Test
    @DisplayName("Activate an already active user throws UserAlreadyActive")
    void activateUser_alreadyActive_throwsUserAlreadyActive()
    {
        // -- GIVEN --
        User activeUser = createPersistedActiveUser();
        PhoneNumber newPhoneNumber = new PhoneNumber("0987654321");
        when(userRepository.findById(activeUser.getId())).thenReturn(Optional.of(activeUser));

        // -- WHEN --
        UserAlreadyActive exception = assertThrows(
                UserAlreadyActive.class,
                () -> userManagementServiceImpl.activateUser(activeUser.getId(), newPhoneNumber)
        );

        // -- THEN --
        assertEquals("User [Phan Hai Anh] is already active", exception.getMessage());
        assertEquals(new PhoneNumber("0912345678"), activeUser.getPhoneNumber());
        verify(userRepository, times(1)).findById(activeUser.getId());
        verify(userRepository, never()).update(any(User.class));
    }

    // change persisted user's name with valid value
    @Test
    @DisplayName("Change persisted user's name with a valid value successfully")
    void changeUserName_validValue_returnsUpdatedUser()
    {
        // -- GIVEN --
        User persistedUser = createPersistedActiveUser();
        PersonName newName = new PersonName("Nguyen Van An");
        when(userRepository.findById(persistedUser.getId())).thenReturn(Optional.of(persistedUser));
        when(userRepository.update(persistedUser)).thenReturn(persistedUser);

        // -- WHEN --
        User updatedUser = userManagementServiceImpl.changeUserName(persistedUser.getId(), newName);

        // -- THEN --
        assertAll(
                () -> assertSame(persistedUser, updatedUser),
                () -> assertEquals(newName, updatedUser.getName())
        );
        verify(userRepository, times(1)).findById(persistedUser.getId());
        verify(userRepository, times(1)).update(persistedUser);
    }

    // change persisted user's name with invalid value
    @ParameterizedTest(name = "{index}: invalid name = {0}")
    @MethodSource("invalidPersonNames")
    @DisplayName("Reject invalid values for a user's name")
    void changeUserName_invalidValue_throwsIllegalArgumentException(String invalidName, String expectedMessage)
    {
        // -- GIVEN --
        User persistedUser = createPersistedActiveUser();

        // -- WHEN --
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userManagementServiceImpl.changeUserName(
                        persistedUser.getId(),
                        new PersonName(invalidName)
                )
        );

        // -- THEN --
        assertEquals(expectedMessage, exception.getMessage());
        verifyNoInteractions(userRepository);
    }

    private static Stream<Arguments> invalidPersonNames()
    {
        return Stream.of(
                Arguments.of("   ", "Name cannot be empty"),
                Arguments.of("Phan123", "Invalid person name")
        );
    }

    // change name but userId not found
    @Test
    @DisplayName("Change name for a user ID that is not found throws UserNotFoundException")
    void changeUserName_userIdNotFound_throwsUserNotFoundException()
    {
        // -- GIVEN --
        Long userId = 5L;
        PersonName newName = new PersonName("Nguyen Van An");
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // -- WHEN --
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userManagementServiceImpl.changeUserName(userId, newName)
        );

        // -- THEN --
        assertEquals(String.format("User with id %d not found", userId), exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).update(any(User.class));
    }

    // change name with null arguments -> throw null pointer exception
    @ParameterizedTest(name = "{index}: null {0}")
    @MethodSource("nullChangeNameArguments")
    @DisplayName("Change user's name with a null required argument throws NullPointerException")
    void changeUserName_nullArgument_throwsNullPointerException(
            String nullArgument, Long userId, PersonName newName, String expectedMessage)
    {
        // -- GIVEN --
        // Arguments are supplied by nullChangeNameArguments().

        // -- WHEN --
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> userManagementServiceImpl.changeUserName(userId, newName)
        );

        // -- THEN --
        assertEquals(expectedMessage, exception.getMessage());
        verifyNoInteractions(userRepository);
    }

    private static Stream<Arguments> nullChangeNameArguments()
    {
        return Stream.of(
                Arguments.of("user ID", null, new PersonName("Nguyen Van An"), "UserId cannot be null"),
                Arguments.of("new name", 1L, null, "New username cannot be null")
        );
    }

    // change persisted user's phone number with valid value
    @Test
    @DisplayName("Change persisted user's phone number with a valid value successfully")
    void changePhoneNumber_validValue_returnsUpdatedUser()
    {
        // -- GIVEN --
        User persistedUser = createPersistedActiveUser();
        PhoneNumber newPhoneNumber = new PhoneNumber("0987654321");
        when(userRepository.findById(persistedUser.getId())).thenReturn(Optional.of(persistedUser));
        when(userRepository.findByPhoneNumber(newPhoneNumber)).thenReturn(Optional.empty());
        when(userRepository.update(persistedUser)).thenReturn(persistedUser);

        // -- WHEN --
        User updatedUser = userManagementServiceImpl.changePhoneNumber(
                persistedUser.getId(), newPhoneNumber
        );

        // -- THEN --
        assertAll(
                () -> assertSame(persistedUser, updatedUser),
                () -> assertEquals(newPhoneNumber, updatedUser.getPhoneNumber())
        );
        verify(userRepository, times(1)).findById(persistedUser.getId());
        verify(userRepository, times(1)).findByPhoneNumber(newPhoneNumber);
        verify(userRepository, times(1)).update(persistedUser);
    }

    // change persisted user's phone number with invalid value
    @Test
    @DisplayName("Change user's phone number to an already used value throws PhoneAlreadyInUseException")
    void changePhoneNumber_alreadyUsedValue_throwsPhoneAlreadyInUseException()
    {
        // -- GIVEN --
        User persistedUser = createPersistedActiveUser();
        PhoneNumber usedPhoneNumber = new PhoneNumber("0987654321");
        User userWithUsedPhoneNumber = new User(
                3L,
                persistedUser.getPasswordHash(),
                new PersonName("Nguyen Van An"),
                usedPhoneNumber,
                new Email("nguyenvanan@gmail.com"),
                UserRole.NORMAL_USER,
                UserStatus.ACTIVE,
                persistedUser.getTimeCreated()
        );
        when(userRepository.findById(persistedUser.getId())).thenReturn(Optional.of(persistedUser));
        when(userRepository.findByPhoneNumber(usedPhoneNumber))
                .thenReturn(Optional.of(userWithUsedPhoneNumber));

        // -- WHEN --
        PhoneAlreadyInUseException exception = assertThrows(
                PhoneAlreadyInUseException.class,
                () -> userManagementServiceImpl.changePhoneNumber(
                        persistedUser.getId(), usedPhoneNumber
                )
        );

        // -- THEN --
        assertEquals("This phone number has already been used", exception.getMessage());
        assertNotEquals(usedPhoneNumber, persistedUser.getPhoneNumber());
        verify(userRepository, times(1)).findById(persistedUser.getId());
        verify(userRepository, times(1)).findByPhoneNumber(usedPhoneNumber);
        verify(userRepository, never()).update(any(User.class));
    }

    // change phone number but userId not found
    @Test
    @DisplayName("Change phone number for a user ID that is not found throws UserNotFoundException")
    void changePhoneNumber_userIdNotFound_throwsUserNotFoundException()
    {
        // -- GIVEN --
        Long userId = 5L;
        PhoneNumber newPhoneNumber = new PhoneNumber("0987654321");
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // -- WHEN --
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userManagementServiceImpl.changePhoneNumber(userId, newPhoneNumber)
        );

        // -- THEN --
        assertEquals(String.format("User with id %d not found", userId), exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).findByPhoneNumber(any(PhoneNumber.class));
        verify(userRepository, never()).update(any(User.class));
    }

    // change user's phone number with null arguments required -> throw null pointer exception
    @ParameterizedTest(name = "{index}: null {0}")
    @MethodSource("nullChangePhoneNumberArguments")
    @DisplayName("Change user's phone number with a null required argument throws NullPointerException")
    void changePhoneNumber_nullArgument_throwsNullPointerException(
            String nullArgument, Long userId, PhoneNumber phoneNumber, String expectedMessage)
    {
        // -- GIVEN --
        // Arguments are supplied by nullChangePhoneNumberArguments().

        // -- WHEN --
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> userManagementServiceImpl.changePhoneNumber(userId, phoneNumber)
        );

        // -- THEN --
        assertEquals(expectedMessage, exception.getMessage());
        verifyNoInteractions(userRepository);
    }
    private static Stream<Arguments> nullChangePhoneNumberArguments()
    {
        return Stream.of(
                Arguments.of("user ID", null, new PhoneNumber("0987654321"), "UserId cannot be null"),
                Arguments.of("new phone number", 1L, null, "Phone number cannot be null")
        );
    }

    // change persisted user's email with a valid value
    @Test
    @DisplayName("Change persisted user's email with a valid value successfully")
    void changeEmail_validValue_returnsUpdatedUser()
    {
        // -- GIVEN --
        User persistedUser = createPersistedActiveUser();
        Email newEmail = new Email("newemail@gmail.com");
        when(userRepository.findById(persistedUser.getId())).thenReturn(Optional.of(persistedUser));
        when(userRepository.findByEmail(newEmail)).thenReturn(Optional.empty());
        when(userRepository.update(persistedUser)).thenReturn(persistedUser);

        // -- WHEN --
        User updatedUser = userManagementServiceImpl.changeEmail(persistedUser.getId(), newEmail);

        // -- THEN --
        assertAll(
                () -> assertSame(persistedUser, updatedUser),
                () -> assertEquals(newEmail, updatedUser.getEmail())
        );
        verify(userRepository, times(1)).findById(persistedUser.getId());
        verify(userRepository, times(1)).findByEmail(newEmail);
        verify(userRepository, times(1)).update(persistedUser);
    }

    // change persisted user's email to an already used value
    @Test
    @DisplayName("Change user's email to an already used value throws EmailAlreadyInUseException")
    void changeEmail_alreadyUsedValue_throwsEmailAlreadyInUseException()
    {
        // -- GIVEN --
        User persistedUser = createPersistedActiveUser();
        Email originalEmail = persistedUser.getEmail();
        Email usedEmail = new Email("usedemail@gmail.com");
        User userWithUsedEmail = new User(
                3L,
                persistedUser.getPasswordHash(),
                new PersonName("Nguyen Van An"),
                new PhoneNumber("0987654321"),
                usedEmail,
                UserRole.NORMAL_USER,
                UserStatus.ACTIVE,
                persistedUser.getTimeCreated()
        );
        when(userRepository.findById(persistedUser.getId())).thenReturn(Optional.of(persistedUser));
        when(userRepository.findByEmail(usedEmail)).thenReturn(Optional.of(userWithUsedEmail));

        // -- WHEN --
        EmailAlreadyInUseException exception = assertThrows(
                EmailAlreadyInUseException.class,
                () -> userManagementServiceImpl.changeEmail(persistedUser.getId(), usedEmail)
        );

        // -- THEN --
        assertEquals("This email has already been used", exception.getMessage());
        assertEquals(originalEmail, persistedUser.getEmail());
        verify(userRepository, times(1)).findById(persistedUser.getId());
        verify(userRepository, times(1)).findByEmail(usedEmail);
        verify(userRepository, never()).update(any(User.class));
    }

    // change email but user ID is not found
    @Test
    @DisplayName("Change email for a user ID that is not found throws UserNotFoundException")
    void changeEmail_userIdNotFound_throwsUserNotFoundException()
    {
        // -- GIVEN --
        Long userId = 5L;
        Email newEmail = new Email("newemail@gmail.com");
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // -- WHEN --
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userManagementServiceImpl.changeEmail(userId, newEmail)
        );

        // -- THEN --
        assertEquals(String.format("User with id %d not found", userId), exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).findByEmail(any(Email.class));
        verify(userRepository, never()).update(any(User.class));
    }

    // change email with a null required argument
    @ParameterizedTest(name = "{index}: null {0}")
    @MethodSource("nullChangeEmailArguments")
    @DisplayName("Change user's email with a null required argument throws NullPointerException")
    void changeEmail_nullArgument_throwsNullPointerException(
            String nullArgument, Long userId, Email email, String expectedMessage)
    {
        // -- GIVEN --
        // Arguments are supplied by nullChangeEmailArguments().

        // -- WHEN --
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> userManagementServiceImpl.changeEmail(userId, email)
        );

        // -- THEN --
        assertEquals(expectedMessage, exception.getMessage());
        verifyNoInteractions(userRepository);
    }

    private static Stream<Arguments> nullChangeEmailArguments()
    {
        return Stream.of(
                Arguments.of("user ID", null, new Email("newemail@gmail.com"), "UserId cannot be null"),
                Arguments.of("new email", 1L, null, "Email cannot be null")
        );
    }

    // Admin promotes target normal user to ADMIN role with valid arguments -> happy path
    @Test
    @DisplayName("Admin promotes a normal user to ADMIN successfully")
    void promoteToAdmin_validArguments_returnsPromotedUser()
    {
        // -- GIVEN --
        User admin = createPersistedAdminUser();
        User targetUser = createPersistedActiveUser();
        when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(userRepository.findById(targetUser.getId())).thenReturn(Optional.of(targetUser));
        when(userRepository.update(targetUser)).thenReturn(targetUser);

        // -- WHEN --
        User promotedUser = userManagementServiceImpl.promoteToAdmin(
                admin.getId(), targetUser.getId()
        );

        // -- THEN --
        assertAll(
                () -> assertSame(targetUser, promotedUser),
                () -> assertSame(UserRole.ADMIN, promotedUser.getRole())
        );
        verify(userRepository, times(1)).findById(admin.getId());
        verify(userRepository, times(1)).findById(targetUser.getId());
        verify(userRepository, times(1)).update(targetUser);
    }

    // Admin's role is not ADMIN => throw UserNotAuthorizedException
    @Test
    @DisplayName("Normal user cannot promote another user to ADMIN")
    void promoteToAdmin_requesterIsNotAdmin_throwsUserNotAuthorizedException()
    {
        // -- GIVEN --
        User nonAdmin = createPersistedActiveUser();
        Long targetUserId = 2L;
        when(userRepository.findById(nonAdmin.getId())).thenReturn(Optional.of(nonAdmin));

        // -- WHEN --
        UserNotAuthorizedException exception = assertThrows(
                UserNotAuthorizedException.class,
                () -> userManagementServiceImpl.promoteToAdmin(nonAdmin.getId(), targetUserId)
        );

        // -- THEN --
        assertEquals("User is not authorized to perform this operation", exception.getMessage());
        verify(userRepository, times(1)).findById(nonAdmin.getId());
        verify(userRepository, never()).findById(targetUserId);
        verify(userRepository, never()).update(any(User.class));
    }

    // AdminId not found -> throw UserNotFoundException ( Authorize )
    @Test
    @DisplayName("Promotion with an unknown admin ID throws UserNotFoundException")
    void promoteToAdmin_adminIdNotFound_throwsUserNotFoundException()
    {
        // -- GIVEN --
        Long adminId = 10L;
        Long targetUserId = 1L;
        when(userRepository.findById(adminId)).thenReturn(Optional.empty());

        // -- WHEN --
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userManagementServiceImpl.promoteToAdmin(adminId, targetUserId)
        );

        // -- THEN --
        assertEquals(String.format("User with id %d not found", adminId), exception.getMessage());
        verify(userRepository, times(1)).findById(adminId);
        verify(userRepository, never()).findById(targetUserId);
        verify(userRepository, never()).update(any(User.class));
    }

    // User Id not found -> throw UserNotFoundException ( Authorize )
    @Test
    @DisplayName("Promotion with an unknown target user ID throws UserNotFoundException")
    void promoteToAdmin_targetUserIdNotFound_throwsUserNotFoundException()
    {
        // -- GIVEN --
        User admin = createPersistedAdminUser();
        Long targetUserId = 5L;
        when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.empty());

        // -- WHEN --
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userManagementServiceImpl.promoteToAdmin(admin.getId(), targetUserId)
        );

        // -- THEN --
        assertEquals(String.format("User with id %d not found", targetUserId), exception.getMessage());
        verify(userRepository, times(1)).findById(admin.getId());
        verify(userRepository, times(1)).findById(targetUserId);
        verify(userRepository, never()).update(any(User.class));
    }

    // Admin promotes a banned user -> throw AccountBannedException
    @Test
    @DisplayName("Admin cannot promote a banned user")
    void promoteToAdmin_targetUserIsBanned_throwsAccountBannedException()
    {
        // -- GIVEN --
        User admin = createPersistedAdminUser();
        User bannedUser = createPersistedActiveUser();
        bannedUser.banned();
        when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(userRepository.findById(bannedUser.getId())).thenReturn(Optional.of(bannedUser));

        // -- WHEN --
        AccountBannedException exception = assertThrows(
                AccountBannedException.class,
                () -> userManagementServiceImpl.promoteToAdmin(admin.getId(), bannedUser.getId())
        );

        // -- THEN --
        assertEquals("This account has already been banned.", exception.getMessage());
        assertSame(UserRole.NORMAL_USER, bannedUser.getRole());
        verify(userRepository, times(1)).findById(admin.getId());
        verify(userRepository, times(1)).findById(bannedUser.getId());
        verify(userRepository, never()).update(any(User.class));
    }

    // Promotion gone wrong because null arguments required
    @ParameterizedTest(name = "{index}: null {0}")
    @MethodSource("nullPromotionArguments")
    @DisplayName("Promotion with a null required argument throws NullPointerException")
    void promoteToAdmin_nullArgument_throwsNullPointerException(
            String nullArgument, Long adminId, Long targetUserId, String expectedMessage)
    {
        // -- GIVEN --
        // Arguments are supplied by nullPromotionArguments().

        // -- WHEN --
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> userManagementServiceImpl.promoteToAdmin(adminId, targetUserId)
        );

        // -- THEN --
        assertEquals(expectedMessage, exception.getMessage());
        verifyNoInteractions(userRepository);
    }

    private static Stream<Arguments> nullPromotionArguments()
    {
        return Stream.of(
                Arguments.of("admin ID", null, 1L, "Admin Id cannot be null"),
                Arguments.of("target user ID", 10L, null, "UserId cannot be null")
        );
    }


}
