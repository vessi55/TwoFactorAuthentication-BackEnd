package twofactorauth.dto.user;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {

    private String id;

    @NotBlank(message = "Password must not be empty!")
    @Size(min = 6, message = "Password Length must be at least 6 symbols!")
    private String password;

    @NotBlank(message = "Password must not be empty!")
    @Size(min = 6, message = "Password Length must be at least 6 symbols!")
    private String repeatPassword;
}