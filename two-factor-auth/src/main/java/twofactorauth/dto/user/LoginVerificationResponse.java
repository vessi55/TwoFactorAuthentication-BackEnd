package twofactorauth.dto.user;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginVerificationResponse {

    private String userUid;

    private String verificationCode;
}
