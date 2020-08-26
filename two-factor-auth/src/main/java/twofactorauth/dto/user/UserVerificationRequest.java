package twofactorauth.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
public class UserVerificationRequest {

    @NotBlank
    private String email;

    @NotBlank(message = "Verification Code Required !")
    private String verificationCode;
}
