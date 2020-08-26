package twofactorauth.dto.invitation;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvitationResponse {

    private String uid;

    private String email;

    private String status;

    private String verificationCode;
}
