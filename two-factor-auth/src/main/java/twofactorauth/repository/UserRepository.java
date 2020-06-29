package twofactorauth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import twofactorauth.entity.Invitation;
import twofactorauth.entity.User;
import twofactorauth.enums.UserRole;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUidAndIsDeleted(String id, boolean isDeleted);

    Optional<User> findByEmailAndIsDeleted(String email, boolean isDeleted);

    Optional<User> findByPhoneAndIsDeleted(String phone, boolean isDeleted);

    Optional<User> findByRole(UserRole role);

    Optional<User> findByInvitation(Invitation invitation);

}
