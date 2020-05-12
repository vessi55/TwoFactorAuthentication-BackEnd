package twofactorauth.util.mailContents;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegistrationMailContent {

    private String email;

    private String adminName;
}
