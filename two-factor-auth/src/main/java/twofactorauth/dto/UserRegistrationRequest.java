package twofactorauth.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
    @Size(min = 6, message = "Length must be at least 6 symbols!")
    private String password;

    @NotBlank(message = "Password must not be empty!")
    @Size(min = 6, message = "Length must be at least 6 symbols!")
    private String repeatPassword;

    @NotNull
    @Pattern(regexp = "(\\+)?(359|0)8[789]\\d{1}\\d{3}\\d{3}", message = "Invalid phone number!")
    private String phone;
}
