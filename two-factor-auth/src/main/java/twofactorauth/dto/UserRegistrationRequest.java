package twofactorauth.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationRequest {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    @Pattern(regexp = ".+@.+\\.[a-z]+", message = "Invalid email address!")
    private String email;

    @NotBlank(message = "Password must not be empty!")
    @Size(min = 6, message = "Password Length must be at least 6 symbols!")
    private String password;

    @NotBlank(message = "Password must not be empty!")
    @Size(min = 6, message = "Password Length must be at least 6 symbols!")
    private String repeatPassword;

    @NotBlank
    @Pattern(regexp = "08[789]\\d{7}", message = "Invalid phone number!")
    private String phone;

    @NotBlank
    @Size(min = 6, max = 6, message = "Verification Code Length must be 6 symbols!")
    private String verificationCode;
}
