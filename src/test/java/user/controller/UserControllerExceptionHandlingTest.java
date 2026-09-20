package user.controller;

import exception.business.detailed_exceptions.EmailAlreadyInUseException;
import exception.global_exception_handler.GlobalExceptionHandler;
import exception.resource.detailed_exceptions.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import user.service.UserManagementService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class UserControllerExceptionHandlingTest {
    private MockMvc mockMvc; // người giả lập gửi HTTP Request tới controller
    private UserManagementService userManagementService;

    @BeforeEach
    void setUp() {
        userManagementService = mock(UserManagementService.class);
        UserController userController = new UserController(userManagementService);

        // set up
        mockMvc = standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getUserById_whenUserDoesNotExist_returnsErrorResponse() throws Exception {
        when(userManagementService.findUserById(999L))
                .thenThrow(new UserNotFoundException(999L));

        mockMvc.perform(get("/api/v1/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.httpStatusCode").value(404))
                .andExpect(jsonPath("$.errorMessage").value("User with id 999 not found"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createUser_whenEmailAlreadyExists_returnsErrorResponse() throws Exception {
        doThrow(new EmailAlreadyInUseException())
                .when(userManagementService)
                .createUser(any(), any(), any(), any(), any());

        String requestBody = """
                {
                  "username": "Nguyen Hai Anh",
                  "email": "haianh@example.com",
                  "password": "StrongPassword1!",
                  "phoneNumber": "0912345678",
                  "userRole": "NORMAL_USER"
                }
                """;

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON) // xác định input đầu vào là JSON
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)) // kiểm tra định dạng có phải JSON không
                // kiểm tra sâu nhờ JSON  để bóc tách kiểu cấu trúc dữ liệu JSON có khớp
                .andExpect(jsonPath("$.httpStatusCode").value(409))
                .andExpect(jsonPath("$.errorMessage").value("This email has already been used"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
