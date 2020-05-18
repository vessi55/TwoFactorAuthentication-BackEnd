package twofactorauth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import twofactorauth.entity.Invitation;
import twofactorauth.entity.User;
import twofactorauth.enums.UserRole;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    Optional<User> findByRole(UserRole role);

    Optional<User> findByEmailAndPassword(String email, String password);

    Optional<User> findByInvitation(Invitation invitation);
}
