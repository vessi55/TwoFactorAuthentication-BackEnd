package twofactorauth.dto.invitation;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvitationRequest {

//    @Pattern(regexp = ".+@.+\\.[a-z]+", message = "Invalid email address!")
    private String email;
}
