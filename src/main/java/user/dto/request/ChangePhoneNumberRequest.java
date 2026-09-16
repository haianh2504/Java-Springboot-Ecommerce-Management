package user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ChangePhoneNumberRequest {
    @NotBlank(message="PhoneNumber cannot be blank")
    @Pattern(
            regexp = "^(0?)(3[2-9]|5[689]|7[06-9]|8[0-689]|9[0-46-9])[0-9]{7}$",
            message = "Invalid phoneNumber in VietNam"
    )
    private String phoneNumber;
}
