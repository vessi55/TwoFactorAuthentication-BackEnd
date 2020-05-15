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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InvitationService {

    private static final String EMAIL_ALREADY_TAKEN = "Email is already taken by another user!";
    private static final String USER_WITH_ID_NOT_FOUND = "Not Found User With ID : ";
    private static final String NOT_INVITED_USER = "Not Invited User With ID : ";

    private static final int REGISTRATION_CODE_LENGTH = 6;

    private static final String UPPERCASE_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE_LETTERS  = "abcdefghijklmnopqrstuvxyz";
    private static final String DIGITS = "0123456789";

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

    public void delete(Invitation invitation) {
        invitationRepository.delete(invitation);
    }

    public Invitation findInvitationById(String id) {
        Optional<Invitation> invitation = invitationRepository.findById(id);
        return invitation.orElseThrow(() -> new ElementNotFoundException(USER_WITH_ID_NOT_FOUND + id));
    }

    public Invitation findInvitationByEmail(String email) {
        return invitationRepository.findByEmail(email).orElse(null);
    }

    private String generateRegistrationVerificationCode() {

        String alphaNumericString = UPPERCASE_LETTERS + LOWERCASE_LETTERS + DIGITS;

        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < REGISTRATION_CODE_LENGTH; i++) {

            int index = (int)(alphaNumericString.length() * Math.random());
            stringBuilder.append(alphaNumericString.charAt(index));
        }
        return stringBuilder.toString();
    }

    private Invitation saveInvitation(UserInvitationRequest userInvitationRequest) {

        String verificationCode = generateRegistrationVerificationCode();

        Invitation invitation = findInvitationByEmail(userInvitationRequest.getEmail());
        if (invitation != null) {
            throw new ElementAlreadyExistsException(EMAIL_ALREADY_TAKEN);
        }
        invitation = new Invitation(userInvitationRequest.getEmail(), UserRole.USER, UserStatus.INVITED, verificationCode);
        return invitationRepository.save(invitation);
    }

    private String getAdminName() {
        User admin = userService.findUserByRoleAdmin();
        return mailService.getUserName(admin.getUid());
    }

    public void sendInvitationEmail(UserInvitationRequest userInvitationRequest) {

        String adminName = getAdminName();

        Invitation invitation = saveInvitation(userInvitationRequest);

        mailService.sendRegistrationMail(new RegistrationMailContent
                (invitation.getEmail(), adminName, invitation.getVerificationCode()), invitation);
    }

    public void resendInvitationEmail(String invitationId) {

        String adminName = getAdminName();
        String verificationCode = generateRegistrationVerificationCode();

        Invitation invitation = findInvitationById(invitationId);
        if (invitation.getStatus() != UserStatus.INVITED) {
            throw new NotAllowedException(NOT_INVITED_USER + invitationId);
        }

        // reedify 'setUp account' link in email after resend invitation email
        invitation.setCreatedDate(System.currentTimeMillis());
        invitation.setVerificationCode(verificationCode);
        invitationRepository.save(invitation);

        mailService.sendRegistrationMail(new RegistrationMailContent(
                invitation.getEmail(), adminName, verificationCode), invitation);
    }

    public List<UserInvitationResponse> getAllInvitedUsers() {

        List<Invitation> invitations = invitationRepository.findAllByRoleNotOrderByStatusAscEmailAsc(UserRole.ADMIN);

        return invitations.stream()
                .map(invitation -> modelMapper.map(invitation, UserInvitationResponse.class))
                .collect(Collectors.toList());
    }
}
