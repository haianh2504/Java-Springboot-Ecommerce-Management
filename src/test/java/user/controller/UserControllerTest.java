package user.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import user.entities.*;
import user.service.UserManagementService;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {
    private static final Long USER_ID = 10L;
    private static final Long ADMIN_ID = 1L;

    @Mock
    private UserManagementService userManagementService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(new UserController(userManagementService)).build();
    }

    private User persistedUser(PersonName name, UserRole role) {
        return persistedUser(
                name,
                new PhoneNumber("0912345678"),
                new Email("user@example.com"),
                role,
                UserStatus.ACTIVE
        );
    }

    private User persistedUser(
            PersonName name,
            PhoneNumber phoneNumber,
            Email email,
            UserRole role,
            UserStatus status
    ) {
        return new User(
                USER_ID,
                new PasswordHash("$2342haHkacnd"),
                name,
                phoneNumber,
                email,
                role,
                status,
                Instant.parse("2026-09-10T00:00:00Z")
        );
    }

    @Test
    void createUser_validRequest_returnsCreatedUser() throws Exception {
        RawPassword password = new RawPassword("StrongPassword1!");
        PersonName name = new PersonName("Normal User");
        PhoneNumber phone = new PhoneNumber("0912345678");
        Email email = new Email("user@example.com");
        when(userManagementService.createUser(
                argThat(value -> value.value().equals(password.value())),
                eq(name), eq(phone), eq(email), eq(UserRole.NORMAL_USER)
        )).thenReturn(persistedUser(name, UserRole.NORMAL_USER));

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username":"Normal User",
                                  "email":"user@example.com",
                                  "password":"StrongPassword1!",
                                  "phoneNumber":"0912345678",
                                  "userRole":"NORMAL_USER"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.role").value("NORMAL_USER"));

        verify(userManagementService).createUser(
                argThat(value -> value.value().equals(password.value())),
                eq(name), eq(phone), eq(email), eq(UserRole.NORMAL_USER)
        );
    }

    @Test
    void getUserByEmail_existingUser_returnsUser() throws Exception {
        Email email = new Email("user@example.com");
        when(userManagementService.findUserByEmail(email))
                .thenReturn(persistedUser(new PersonName("Normal User"), UserRole.NORMAL_USER));

        mockMvc.perform(get("/api/v1/users").param("email", "user@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.email").value("user@example.com"));

        verify(userManagementService).findUserByEmail(email);
    }

    @Test
    void getUserById_existingUser_returnsUser() throws Exception {
        when(userManagementService.findUserById(USER_ID))
                .thenReturn(persistedUser(new PersonName("Normal User"), UserRole.NORMAL_USER));

        mockMvc.perform(get("/api/v1/users/{id}", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Normal User"));

        verify(userManagementService).findUserById(USER_ID);
    }

    @Test
    void activateUser_validRequest_returnsActiveUser() throws Exception {
        PhoneNumber phone = new PhoneNumber("0912345678");
        User activeUser = persistedUser(new PersonName("Normal User"), UserRole.NORMAL_USER);
        when(userManagementService.activateUser(USER_ID, phone)).thenReturn(activeUser);

        mockMvc.perform(patch("/api/v1/users/{id}/activation", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phoneNumber\":\"0912345678\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.phoneNumber").value("0912345678"));

        verify(userManagementService).activateUser(USER_ID, phone);
    }

    @Test
    void changePhoneNumber_validRequest_returnsUpdatedUser() throws Exception {
        PhoneNumber newPhone = new PhoneNumber("0987654321");
        User updated = persistedUser(
                new PersonName("Normal User"), newPhone, new Email("user@example.com"),
                UserRole.NORMAL_USER, UserStatus.ACTIVE
        );
        when(userManagementService.changePhoneNumber(USER_ID, newPhone)).thenReturn(updated);

        mockMvc.perform(patch("/api/v1/users/{id}/phone-number", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phoneNumber\":\"0987654321\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phoneNumber").value("0987654321"));

        verify(userManagementService).changePhoneNumber(USER_ID, newPhone);
    }

    @Test
    void changeEmail_validRequest_returnsUpdatedUser() throws Exception {
        Email newEmail = new Email("updated@example.com");
        User updated = persistedUser(
                new PersonName("Normal User"), new PhoneNumber("0912345678"), newEmail,
                UserRole.NORMAL_USER, UserStatus.ACTIVE
        );
        when(userManagementService.changeEmail(USER_ID, newEmail)).thenReturn(updated);

        mockMvc.perform(patch("/api/v1/users/{id}/email", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newEmail\":\"updated@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("updated@example.com"));

        verify(userManagementService).changeEmail(USER_ID, newEmail);
    }

    @Test
    void changeUserName_validRequest_returnsUpdatedUser() throws Exception {
        PersonName newName = new PersonName("Updated User");
        when(userManagementService.changeUserName(USER_ID, newName))
                .thenReturn(persistedUser(newName, UserRole.NORMAL_USER));

        mockMvc.perform(patch("/api/v1/users/{id}/name", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"newUserName": "Updated User"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Updated User"));

        verify(userManagementService).changeUserName(USER_ID, newName);
    }

    @Test
    void promoteToAdmin_authorizedRequest_returnsPromotedUser() throws Exception {
        User promotedUser = persistedUser(new PersonName("Normal User"), UserRole.ADMIN);
        when(userManagementService.promoteToAdmin(ADMIN_ID, USER_ID))
                .thenReturn(promotedUser);

        mockMvc.perform(patch("/api/v1/users/{id}/role/admin", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"adminId": 1}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.role").value("ADMIN"));

        verify(userManagementService).promoteToAdmin(ADMIN_ID, USER_ID);
    }
}
