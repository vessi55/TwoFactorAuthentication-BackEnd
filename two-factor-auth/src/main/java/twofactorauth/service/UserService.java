package twofactorauth.service;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import twofactorauth.dto.UserInvitationRequest;
import twofactorauth.dto.UserRegistrationRequest;
import twofactorauth.dto.UserRegistrationResponse;
import twofactorauth.entity.Invitation;
import twofactorauth.entity.User;
import twofactorauth.enums.UserRole;
import twofactorauth.enums.UserStatus;
import twofactorauth.exception.ElementAlreadyExistsException;
import twofactorauth.exception.ElementNotFoundException;
import twofactorauth.exception.PasswordsDoNotMatchException;
import twofactorauth.repository.UserRepository;
import twofactorauth.util.mailContents.RegistrationMailContent;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
public class UserService {

    private static final String EMAIL_ALREADY_TAKEN = "Email is already taken by another user!";
    private static final String PHONE_NUMBER_ALREADY_TAKEN = "Phone Number is already taken by another user!";
    private static final String NOT_MATCHING_PASSWORDS = "Passwords do not match!";
    private static final String USER_NOT_FOUND = "User Is Not Found!";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InvitationService invitationService;

    @Autowired
    private MailService mailService;

    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    public UserRegistrationResponse saveUser(UserRegistrationRequest userRegistrationRequest) {

        checkIfEmailAlreadyTaken(userRegistrationRequest.getEmail());
        checkIfPhoneAlreadyTaken(userRegistrationRequest.getPhone());
        checkIfMatchingPasswords(userRegistrationRequest.getPassword(), userRegistrationRequest.getRepeatPassword());

        User user;
        Invitation invitation = invitationService.getInvitationByEmail(userRegistrationRequest.getEmail());
        if (invitation == null) {
            throw new ElementNotFoundException(USER_NOT_FOUND);
        }

        invitation.setStatus(UserStatus.REGISTERED);
        invitationService.saveInvitation(invitation);
        user = saveUserInformation(userRegistrationRequest, invitation);

        return modelMapper.map(user, UserRegistrationResponse.class);
    }

    private void checkIfEmailAlreadyTaken(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ElementAlreadyExistsException(EMAIL_ALREADY_TAKEN);
        }
    }

    private void checkIfPhoneAlreadyTaken(String phoneNumber) {
        if (userRepository.findByPhone(phoneNumber).isPresent()) {
            throw new ElementAlreadyExistsException(PHONE_NUMBER_ALREADY_TAKEN);
        }
    }

    private void checkIfMatchingPasswords(String password, String repeatPassword) {
        if (!password.equals(repeatPassword)) {
            throw new PasswordsDoNotMatchException(NOT_MATCHING_PASSWORDS);
        }
    }

    private User saveUserInformation(UserRegistrationRequest userRegistrationRequest, Invitation invitation) {

        User user = User.builder()
                .firstName(userRegistrationRequest.getFirstName())
                .lastName(userRegistrationRequest.getLastName())
                .email(userRegistrationRequest.getEmail())
                .phone(userRegistrationRequest.getPhone())
                .password(userRegistrationRequest.getPassword())
                .invitation(invitation).build();

        return userRepository.save(user);
    }

    public User findUserByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        return user.orElseThrow(() -> new ElementNotFoundException(USER_NOT_FOUND));
    }

    public User findUserById(String id) {
        Optional<User> user = userRepository.findById(id);
        return user.orElseThrow(() -> new ElementNotFoundException(USER_NOT_FOUND));
    }

    public User findUserByRoleAdmin() {
        Optional<User> user = userRepository.findByRole(UserRole.ADMIN);
        return user.orElseThrow(() -> new ElementNotFoundException(USER_NOT_FOUND));
    }

    private Invitation saveInvitation(UserInvitationRequest userInvitationRequest) {

        Invitation invitation = invitationService.getInvitationByEmail(userInvitationRequest.getEmail());
        if (invitation != null) {
            throw new ElementAlreadyExistsException(EMAIL_ALREADY_TAKEN);
        }
        invitation = new Invitation(userInvitationRequest.getEmail(), UserRole.USER, UserStatus.INVITED);
        return invitationService.saveInvitation(invitation);
    }

    @Transactional
    public String sendInvitation(UserInvitationRequest userInvitationRequest) {
        User admin = findUserByRoleAdmin();
        String adminName = mailService.getUserName(admin.getEmail());

        Invitation invitation = saveInvitation(userInvitationRequest);

        mailService.sendRegistrationMail(invitation.getEmail(), new RegistrationMailContent(invitation.getEmail(), adminName));
        return "Registration Email Is Sent Successfully!";
    }
}
