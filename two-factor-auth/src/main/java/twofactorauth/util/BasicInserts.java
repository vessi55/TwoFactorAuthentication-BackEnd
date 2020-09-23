package twofactorauth.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import twofactorauth.entity.Invitation;
import twofactorauth.entity.User;
import twofactorauth.enums.UserGender;
import twofactorauth.enums.UserRole;
import twofactorauth.enums.UserStatus;
import twofactorauth.repository.UserRepository;

import javax.annotation.PostConstruct;

@Component
public class BasicInserts {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {

        Invitation invitation = new Invitation("admin@abv.bg",
                UserRole.ADMIN, UserStatus.REGISTERED, "ADMIN1");

        User admin = new User("Весела", "Коцева", UserRole.ADMIN,
                UserGender.FEMALE, "twofactorauthProject@outlook.com",
                passwordEncoder.encode("admin"), "+359888102030", invitation);

        if (!userRepository.findByEmailAndIsDeleted(admin.getEmail(), false).isPresent()) {
            userRepository.save(admin);
        }
    }
}