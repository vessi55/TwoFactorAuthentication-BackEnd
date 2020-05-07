package twofactorauth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import twofactorauth.entity.Invitation;

import java.util.Optional;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, String>  {

    Optional<Invitation> findByEmail(String email);
}
