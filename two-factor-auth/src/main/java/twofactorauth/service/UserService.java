package twofactorauth.service;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import twofactorauth.dto.EmailLinkValidResponse;
import twofactorauth.dto.user.*;
import twofactorauth.dto.user.password.ResetPasswordRequest;
import twofactorauth.entity.Invitation;
import twofactorauth.entity.LoginVerification;
import twofactorauth.entity.User;
import twofactorauth.enums.UserRole;
import twofactorauth.enums.UserStatus;
import twofactorauth.exception.*;
import twofactorauth.repository.UserRepository;
import twofactorauth.util.MailContent;

import javax.transaction.Transactional;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {

    private static final String EMAIL_ALREADY_TAKEN = "Email is already taken by another user!";
    private static final String PHONE_NUMBER_ALREADY_TAKEN = "Phone Number is already taken by another user!";
    private static final String NOT_MATCHING_PASSWORDS = "Passwords do not match!";
    private static final String WRONG_CREDENTIALS = "Wrong credentials";
    private static final String INVALID_VERIFICATION_CODE = "Invalid Verification Code!";

    private static final String USER_WITH_ID_NOT_FOUND = "Not Found User With ID : ";
    private static final String USER_WITH_EMAIL_NOT_FOUND = "Not Found User With Email : ";
    private static final String ADMIN_NOT_FOUND = "Not Found User With Role ADMIN";
    private static final String SUCCESSFULLY_CHANGED_PASSWORD = "Successfully Changed Password!";

    private static final Long REGISTER_EMAIL_EXPIRATION_TIME = TimeUnit.HOURS.toMillis(24);
    private static final Long RESET_PASSWORD_EXPIRATION_TIME = TimeUnit.HOURS.toMillis(2);

    public static final int LOW_VALUE_VERIFICATION_CODE = 100;
    public static final int HIGH_VALUE_VERIFICATION_CODE = 999;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InvitationService invitationService;

    @Autowired
    private MailService mailService;

    @Autowired
    private LoginVerificationService loginVerificationService;

    @Autowired
    private ModelMapper modelMapper;

    public void save(User user) {
        userRepository.save(user);
    }

    @Transactional
    public UserResponse registerUser(UserRegistrationRequest userRegistrationRequest) {

        checkIfEmailAlreadyTaken(userRegistrationRequest.getEmail());
        checkIfPhoneAlreadyTaken(userRegistrationRequest.getPhone());
        checkIfMatchingPasswords(userRegistrationRequest.getPassword(), userRegistrationRequest.getRepeatPassword());

        Invitation invitation = invitationService.getInvitedUser(userRegistrationRequest);

        checkIfVerificationCodeIsValid(userRegistrationRequest, invitation);

        // invalidate 'setUp account' link in email after successful registration
        invitation.setCreatedDate(System.currentTimeMillis() - REGISTER_EMAIL_EXPIRATION_TIME);
        invitation.setStatus(UserStatus.REGISTERED);
        invitationService.save(invitation);

        User user = saveUserInformation(userRegistrationRequest, invitation);

        return modelMapper.map(user, UserResponse.class);
    }

    private void checkIfEmailAlreadyTaken(String email) {
        if (userRepository.findByEmailAndIsDeleted(email, false).isPresent()) {
            throw new ElementAlreadyExistsException(EMAIL_ALREADY_TAKEN);
        }
    }

    private void checkIfPhoneAlreadyTaken(String phoneNumber) {
        if (userRepository.findByPhoneAndIsDeleted(phoneNumber, false).isPresent()) {
            throw new ElementAlreadyExistsException(PHONE_NUMBER_ALREADY_TAKEN);
        }
    }

    private void checkIfMatchingPasswords(String password, String repeatPassword) {
        if (!password.equals(repeatPassword)) {
            throw new PasswordsDoNotMatchException(NOT_MATCHING_PASSWORDS);
        }
    }

    private void checkIfVerificationCodeIsValid(UserRegistrationRequest userRegistrationRequest, Invitation invitation) {
        if(!userRegistrationRequest.getVerificationCode().equals(invitation.getVerificationCode())) {
            throw new InvalidVerificationCodeException(INVALID_VERIFICATION_CODE);
        }
    }

    private User saveUserInformation(UserRegistrationRequest userRegistrationRequest, Invitation invitation) {

        User user = User.builder()
                .firstName(userRegistrationRequest.getFirstName())
                .lastName(userRegistrationRequest.getLastName())
                .email(userRegistrationRequest.getEmail())
                .phone(userRegistrationRequest.getPhone())
                .password(userRegistrationRequest.getPassword())
                .role(UserRole.USER)
                .invitation(invitation).build();

        return userRepository.save(user);
    }

    public User findUserById(String id) {
        Optional<User> user = userRepository.findByUidAndIsDeleted(id, false);
        return user.orElseThrow(() -> new ElementNotFoundException(USER_WITH_ID_NOT_FOUND + id));
    }

    public User findUserByEmail(String email) {
        Optional<User> user = userRepository.findByEmailAndIsDeleted(email, false);
        return user.orElseThrow(() -> new ElementNotFoundException(USER_WITH_EMAIL_NOT_FOUND + email));
    }

    public User findUserByRoleAdmin() {
        Optional<User> user = userRepository.findByRole(UserRole.ADMIN);
        return user.orElseThrow(() -> new ElementNotFoundException(ADMIN_NOT_FOUND));
    }

    public User findUserByInvitation(Invitation invitation) {
        Optional<User> user = userRepository.findByInvitation(invitation);
        return user.orElseThrow(() -> new ElementNotFoundException(USER_WITH_EMAIL_NOT_FOUND + invitation.getEmail()));
    }

    public UserResponse loginUser(UserLoginRequest userLoginRequest) {

        User user = userRepository.findByEmailAndPassword(userLoginRequest.getEmail(), userLoginRequest.getPassword())
                .orElseThrow(() -> new WrongCredentialsException(WRONG_CREDENTIALS));

        return modelMapper.map(user, UserResponse.class);
    }

    private Integer generateLoginVerificationCode(){
        int low = LOW_VALUE_VERIFICATION_CODE;
        int high = HIGH_VALUE_VERIFICATION_CODE;
        return new Random().nextInt(high - low) + low;
    }

    private LoginVerification getLoginVerificationByUser(User user, Integer verificationCode) {

        LoginVerification loginVerification = loginVerificationService.findLoginVerificationByUser(user);

        if(loginVerification == null) {
            loginVerification = new LoginVerification(verificationCode, user);
        }
        else {
            loginVerification.setVerificationCode(verificationCode);
            loginVerification.setLoginDate(System.currentTimeMillis());
        }
        return loginVerificationService.save(loginVerification);
    }

    public LoginVerificationResponse sendVerificationEmail(String email) {

        User user = findUserByEmail(email);
        String formatUserName = user.getFirstName() + " " + user.getLastName();

        Integer verificationCode = generateLoginVerificationCode();

        LoginVerification loginVerification = getLoginVerificationByUser(user, verificationCode);

        mailService.sendLoginVerificationMail(new MailContent
                (email, formatUserName, verificationCode.toString()));

        return modelMapper.map(loginVerification, LoginVerificationResponse.class);
    }

    public void sendResetPasswordEmail(String email) {

        User user = findUserByEmail(email);
        user.setPasswordResetDate(System.currentTimeMillis());

        String formatUserName = user.getFirstName() + " " + user.getLastName();

        mailService.sendForgottenPasswordMail(new MailContent(email, formatUserName));
    }

    public EmailLinkValidResponse checkIfResetPassLinkIsValid(String userId) {

        User user = findUserById(userId);

        long currentDate = System.currentTimeMillis();
        long expirationDate = user.getPasswordResetDate() + RESET_PASSWORD_EXPIRATION_TIME;
        boolean isUrlExpired = currentDate >= expirationDate;

        return new EmailLinkValidResponse(user.getEmail(), isUrlExpired);
    }

    public String resetPassword(ResetPasswordRequest resetPasswordRequest) {

        checkIfMatchingPasswords(resetPasswordRequest.getPassword(), resetPasswordRequest.getRepeatPassword());

        User user = findUserById(resetPasswordRequest.getUid());
        user.setPassword(resetPasswordRequest.getPassword());
        userRepository.save(user);

        return SUCCESSFULLY_CHANGED_PASSWORD;
    }
}
