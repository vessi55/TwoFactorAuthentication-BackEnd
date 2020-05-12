package twofactorauth.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInvitationRequest {

    @Pattern(regexp = ".+@.+\\.[a-z]+", message = "Invalid email address!")
    private String email;
}
