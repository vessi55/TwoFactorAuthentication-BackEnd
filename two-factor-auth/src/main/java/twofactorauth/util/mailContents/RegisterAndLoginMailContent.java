package twofactorauth.util.mailContents;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegisterAndLoginMailContent {

    private String email;

    private String userName;

    private String verificationCode;
}
