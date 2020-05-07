package twofactorauth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import twofactorauth.entity.Invitation;
import twofactorauth.exception.ElementNotFoundException;
import twofactorauth.repository.InvitationRepository;

import java.util.Optional;

@Service
public class InvitationService {

    private static final String USER_NOT_FOUND = "User with this email is not found!";

    private final InvitationRepository invitationRepository;

    @Autowired
    public InvitationService(InvitationRepository invitationRepository) {
        this.invitationRepository = invitationRepository;
    }

    public Invitation saveInvitation(Invitation invitation) {
        return invitationRepository.save(invitation);
    }

    public Invitation getInvitationByEmail(String email) {
        Optional<Invitation> invitedUser = invitationRepository.findByEmail(email);
        return invitedUser.orElseThrow(() -> new ElementNotFoundException(USER_NOT_FOUND));
    }
}
