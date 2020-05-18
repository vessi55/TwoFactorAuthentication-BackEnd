package twofactorauth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import twofactorauth.entity.LoginVerification;
import twofactorauth.entity.User;

import java.util.Optional;

@Repository
public interface LoginVerificationRepository extends JpaRepository<LoginVerification, String> {

    Optional<LoginVerification> findByUser(User user);
}
