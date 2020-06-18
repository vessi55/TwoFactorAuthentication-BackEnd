package twofactorauth.dto.user;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import twofactorauth.enums.UserRole;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private String token;

    private String uid;

    private String email;

    private String firstName;

    private String lastName;

    private UserRole role;

    private String phone;
}
