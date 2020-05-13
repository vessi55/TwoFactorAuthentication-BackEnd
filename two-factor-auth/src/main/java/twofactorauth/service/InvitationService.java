package twofactorauth.service;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import twofactorauth.dto.UserInvitationRequest;
import twofactorauth.dto.UserInvitationResponse;
import twofactorauth.entity.Invitation;
import twofactorauth.entity.User;
import twofactorauth.enums.UserRole;
import twofactorauth.enums.UserStatus;
import twofactorauth.exception.ElementAlreadyExistsException;
import twofactorauth.exception.ElementNotFoundException;
import twofactorauth.exception.NotAllowedException;
import twofactorauth.repository.InvitationRepository;
import twofactorauth.util.mailContents.RegistrationMailContent;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InvitationService {

    private static final String EMAIL_ALREADY_TAKEN = "Email is already taken by another user!";
    private static final String USER_WITH_ID_NOT_FOUND = "Not Found User With ID : ";
    private static final String NOT_INVITED_USER = "Not Invited User With ID : ";

    private static final String SEND_MAIL_SUCCESS = "Email is sent successfully!";

    @Autowired
    private InvitationRepository invitationRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private MailService mailService;

    @Autowired
    private ModelMapper modelMapper;

    public void save(Invitation invitation) {
        invitationRepository.save(invitation);
    }

    public Invitation findInvitationById(String id) {
        Optional<Invitation> invitation = invitationRepository.findById(id);
        return invitation.orElseThrow(() -> new ElementNotFoundException(USER_WITH_ID_NOT_FOUND + id));
    }

    public Invitation findInvitationByEmail(String email) {
        return invitationRepository.findByEmail(email).orElse(null);
    }

    private Invitation saveInvitation(UserInvitationRequest userInvitationRequest) {

        Invitation invitation = findInvitationByEmail(userInvitationRequest.getEmail());
        if (invitation != null) {
            throw new ElementAlreadyExistsException(EMAIL_ALREADY_TAKEN);
        }
        invitation = new Invitation(userInvitationRequest.getEmail(), UserRole.USER, UserStatus.INVITED);
        return invitationRepository.save(invitation);
    }

    private String getAdminName() {
        User admin = userService.findUserByRoleAdmin();
        return mailService.getUserName(admin.getUid());
    }

    @Transactional
    public String sendInvitationEmail(UserInvitationRequest userInvitationRequest) {

        String adminName = getAdminName();

        Invitation invitation = saveInvitation(userInvitationRequest);

        mailService.sendRegistrationMail(new RegistrationMailContent(invitation.getEmail(), adminName));
        return SEND_MAIL_SUCCESS;
    }

    public String resendInvitationEmail(String invitationId) {

        String adminName = getAdminName();

        Invitation invitation = findInvitationById(invitationId);
        if (invitation.getStatus() != UserStatus.INVITED) {
            throw new NotAllowedException(NOT_INVITED_USER + invitationId);
        }

        // reedify 'setUp account' link in email after resend invitation email
        invitation.setCreatedDate(System.currentTimeMillis());
        invitationRepository.save(invitation);

        mailService.sendRegistrationMail(new RegistrationMailContent(invitation.getEmail(), adminName));
        return SEND_MAIL_SUCCESS;
    }

    public List<UserInvitationResponse> getAllInvitedUsers() {

        List<Invitation> invitations = invitationRepository.findAllByRoleNotOrderByStatusAscEmailAsc(UserRole.ADMIN);

        return invitations.stream()
                .map(invitation -> modelMapper.map(invitation, UserInvitationResponse.class))
                .collect(Collectors.toList());
    }
}
