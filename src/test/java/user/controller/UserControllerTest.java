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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
        return new User(
                USER_ID,
                new PasswordHash("$2342haHkacnd"),
                name,
                new PhoneNumber("0912345678"),
                new Email("user@example.com"),
                role,
                UserStatus.ACTIVE,
                Instant.parse("2026-09-10T00:00:00Z")
        );
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
