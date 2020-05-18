package twofactorauth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import twofactorauth.entity.Invitation;
import twofactorauth.enums.UserRole;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, String>  {

    Optional<Invitation> findById(String id);

    Optional<Invitation> findByEmailAndIsDeleted(String email, boolean isDeleted);

    List<Invitation> findAllByRoleNotAndIsDeletedOrderByStatusAscEmailAsc(UserRole role, boolean isDeleted);
}
