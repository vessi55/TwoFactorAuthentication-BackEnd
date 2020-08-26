package twofactorauth.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import twofactorauth.dto.invitation.InvitationRequest;
import twofactorauth.dto.invitation.InvitationResponse;
import twofactorauth.dto.EmailLinkValidResponse;
import twofactorauth.dto.user.UserRegistrationRequest;
import twofactorauth.entity.Invitation;
import twofactorauth.entity.User;
import twofactorauth.enums.UserRole;
import twofactorauth.enums.UserStatus;
import twofactorauth.exception.ElementAlreadyExistsException;
import twofactorauth.exception.ElementNotFoundException;
import twofactorauth.exception.NotAllowedException;
import twofactorauth.repository.InvitationRepository;
import twofactorauth.util.MailContent;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class InvitationService {

    private static final String EMAIL_ALREADY_TAKEN = "Email is already taken by another user !";
    private static final String USER_WITH_NOT_FOUND = "User does not exist !";
    private static final String USER_ALREADY_EXISTS = "User already exists !";
    private static final String INVITATION_DELETED = "Already Deleted Invitation With Email : ";
    private static final String NOT_DELETED_INVITATION = "Not Deleted Invitation !";
    private static final String SUCCESSFULLY_DELETED_INVITATION = "Invitation is Successfully Deleted ! ";
    private static final String INVITATION_EMAIL_SUCCESS = "Invitation Email is Sent Successfully ! ";

    private static final int REGISTRATION_VERIFICATION_CODE_LENGTH = 6;

    private static final Long REGISTER_EMAIL_EXPIRATION_TIME = TimeUnit.HOURS.toMillis(24);

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
        return invitation.orElseThrow(() -> new ElementNotFoundException(USER_WITH_NOT_FOUND));
    }

    public Invitation findInvitationByEmail(String email) {
        return invitationRepository.findByEmailAndIsDeleted(email, false).orElse(null);
    }

    public Invitation getInvitedUser(UserRegistrationRequest userRegistrationRequest) {
        Invitation invitation = findInvitationByEmail(userRegistrationRequest.getEmail());
        if (invitation == null) {
            throw new ElementNotFoundException(USER_WITH_NOT_FOUND);
        }
        return invitation;
    }

    private String getAdminName() {
        User admin = userService.findUserByRoleAdmin();
        return admin.getFirstName() + " " + admin.getLastName();
    }

    private String generateRegistrationVerificationCode() {
        return RandomStringUtils.randomAlphanumeric(REGISTRATION_VERIFICATION_CODE_LENGTH);
    }

    private Invitation saveInvitation(InvitationRequest invitationRequest) {

        String verificationCode = generateRegistrationVerificationCode();

        Invitation invitation = findInvitationByEmail(invitationRequest.getEmail());
        if (invitation != null) {
            throw new ElementAlreadyExistsException(EMAIL_ALREADY_TAKEN);
        }
        invitation = new Invitation(invitationRequest.getEmail(), UserRole.USER, UserStatus.INVITED, verificationCode);
        return invitationRepository.save(invitation);
    }

    public String sendInvitationEmail(InvitationRequest invitationRequest) {

        String adminName = getAdminName();

        Invitation invitation = saveInvitation(invitationRequest);

        mailService.sendRegistrationMail(new MailContent
                (invitation.getEmail(), adminName, invitation.getVerificationCode()), invitation);

        return INVITATION_EMAIL_SUCCESS;
    }

    public String resendInvitationEmail(String invitationId) {

        String adminName = getAdminName();
        String verificationCode = generateRegistrationVerificationCode();

        Invitation invitation = findInvitationById(invitationId);
        if (invitation.getStatus() != UserStatus.INVITED) {
            throw new NotAllowedException(USER_ALREADY_EXISTS);
        }
        if(invitation.isDeleted()) {
            throw new ElementNotFoundException(INVITATION_DELETED + invitation.getEmail());
        }
        // reedify 'setUp account' link in email after resend invitation email
        invitation.setCreatedDate(System.currentTimeMillis());
        invitation.setVerificationCode(verificationCode);
        invitationRepository.save(invitation);

        mailService.sendRegistrationMail(new MailContent(
                invitation.getEmail(), adminName, verificationCode), invitation);

        return INVITATION_EMAIL_SUCCESS;
    }

    public String sendWelcomeBackEmail(User user) {
        String name = user.getFirstName() + " " + user.getLastName();

        mailService.sendWelcomeBackMail(new MailContent(user.getEmail(), name));

        return INVITATION_EMAIL_SUCCESS;
    }

    public EmailLinkValidResponse checkIfRegisterLinkIsValid(String invitationId) {

        Invitation invitation = findInvitationById(invitationId);

        long currentDate = System.currentTimeMillis();
        long expirationDate = invitation.getCreatedDate() + REGISTER_EMAIL_EXPIRATION_TIME;
        boolean isUrlExpired = currentDate >= expirationDate;

        return new EmailLinkValidResponse(invitation.getEmail(), isUrlExpired);
    }

    public String deleteInvitationById(String invitationId) {

        Invitation invitation = findInvitationById(invitationId);
        if (invitation.isDeleted()) {
            throw new NotAllowedException(INVITATION_DELETED + invitation.getEmail());
        }
        invitation.setDeleted(true);

        if(invitation.getStatus() == UserStatus.REGISTERED) {
            User user = userService.findUserByInvitation(invitation);
            if(user != null) {
                user.setDeleted(true);
                userService.save(user);
            } else {
                throw new ElementNotFoundException(USER_WITH_NOT_FOUND);
            }
        } else {
            // invalidate 'setUp account' link in email after deleted invitation
            invitation.setCreatedDate(System.currentTimeMillis() - REGISTER_EMAIL_EXPIRATION_TIME);
            invitationRepository.save(invitation);
        }

        return SUCCESSFULLY_DELETED_INVITATION + invitationId;
    }

    @Transactional
    public String recoverInvitationById(String invitationId) {

        Invitation invitation = findInvitationById(invitationId);

        if (!invitation.isDeleted()) {
            throw new NotAllowedException(NOT_DELETED_INVITATION);
        }
        invitation.setDeleted(false);

        if(invitation.getStatus() == UserStatus.REGISTERED) {
            User user = userService.findUserByInvitation(invitation);
            if (!user.isDeleted()) {
                throw new NotAllowedException(NOT_DELETED_INVITATION);
            }
            user.setDeleted(false);
            userService.save(user);
            return sendWelcomeBackEmail(user);
        }

        return resendInvitationEmail(invitationId);
    }

    public List<InvitationResponse> getAllInvitedUsers() {

        List<Invitation> invitations = invitationRepository.findAllByRoleNotAndIsDeletedOrderByStatusAscEmailAsc(UserRole.ADMIN, false);

        return getInvitationResponses(invitations);
    }

    public List<InvitationResponse> getUsersArchive() {

        List<Invitation> invitations = invitationRepository.findAllByIsDeletedOrderByEmailAsc(true);

        return getInvitationResponses(invitations);
    }

    private List<InvitationResponse> getInvitationResponses(List<Invitation> invitations) {
        return invitations.stream()
                .map(invitation -> modelMapper.map(invitation, InvitationResponse.class))
                .collect(Collectors.toList());
    }
}
