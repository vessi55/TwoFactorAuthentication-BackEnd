package twofactorauth.util;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class MailContent {

    @NonNull
    private String email;

    @NonNull
    private String userName;

    private String verificationCode;
}
