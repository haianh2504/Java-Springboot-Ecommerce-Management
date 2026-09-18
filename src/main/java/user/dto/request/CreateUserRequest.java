package user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import user.entities.UserRole;

@Getter
@Setter
@NoArgsConstructor // automatically create a default constructor
@AllArgsConstructor // automatically create a full arguments constructor
public class CreateUserRequest {
    @NotBlank(message = "User name cannot be blank")
    @Pattern(
            regexp = "^[\\p{L}]+(?:[ '\\-][\\p{L}]+)*$",
            message = "Invalid user name"
    )
    private String username;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d\\s])\\S{8,72}$",
            message = "Password must be 8-72 characters long and contain at least one lowercase "
                    + "letter, one uppercase letter, one digit, one special character, and no whitespace"
    )
    private String password;

    @NotBlank(message = "Phone number cannot be blank")
    @Pattern(
            regexp = "^(0?)(3[2-9]|5[689]|7[06-9]|8[0-689]|9[0-46-9])[0-9]{7}$",
            message = "Invalid phone number in Vietnam"
    )
    private String phoneNumber;

    @NotNull(message = "User role cannot be null")
    private UserRole userRole;
}
