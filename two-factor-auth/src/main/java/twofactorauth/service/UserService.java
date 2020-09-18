package twofactorauth.service;

import io.jsonwebtoken.ExpiredJwtException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import twofactorauth.config.JwtTokenUtil;
import twofactorauth.dto.EmailLinkValidResponse;
import twofactorauth.dto.user.*;
import twofactorauth.dto.user.ResetPasswordRequest;
import twofactorauth.entity.Invitation;
import twofactorauth.entity.LoginVerification;
import twofactorauth.entity.User;
import twofactorauth.enums.UserGender;
import twofactorauth.enums.UserRole;
import twofactorauth.enums.UserStatus;
import twofactorauth.exception.*;
import twofactorauth.repository.UserRepository;
import twofactorauth.util.MailContent;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {

    private static final String EMAIL_ALREADY_TAKEN = "Email is already taken by another user!";
    private static final String PHONE_NUMBER_ALREADY_TAKEN = "Phone Number is already taken by another user!";
    private static final String NOT_MATCHING_PASSWORDS = "Passwords do not match!";
    private static final String WRONG_CREDENTIALS = "Wrong Credentials";
    private static final String INVALID_VERIFICATION_CODE = "Invalid Verification Code!";
    private static final String VERIFICATION_CODE_HAS_EXPIRED = "Verification Code Has Expired !";
    private static final String TOKEN_HAS_EXPIRED = "Token has expired";
    private static final String USER_WITH_ID_NOT_FOUND = "Not Found User With ID : ";
    private static final String USER_WITH_PHONE_NOT_FOUND = "Not Found User With Phone Number : ";
    private static final String USER_WITH_EMAIL_NOT_FOUND = "User with this email does not exist !";
    private static final String ADMIN_NOT_FOUND = "Not Found User With Role ADMIN";
    private static final String SUCCESSFULLY_CHANGED_PASSWORD = "Successfully Changed Password!";
    private static final String RESET_PASS_EMAIL_SUCCESS = "Reset Password Email is Sent Successfully ! ";
    private static final String RESET_PASS_DATE_ERROR = "Error with reset password date";
    private static final String INVALID_GENDER = "Invalid Gender !";


    private static final Long REGISTER_EMAIL_EXPIRATION_TIME = TimeUnit.HOURS.toMillis(24);
    private static final Long RESET_PASSWORD_EXPIRATION_TIME = TimeUnit.MINUTES.toMillis(30);
    private static final Long LOGIN_VERIFICATION_EXPIRATION_TIME = TimeUnit.MINUTES.toMillis(1);

    private static final List<UserGender> USER_GENDERS = List.of(UserGender.values());

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InvitationService invitationService;

    @Autowired
    private MailService mailService;

    @Autowired
    private SMSService smsService;

    @Autowired
    private LoginVerificationService loginVerificationService;

    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

        checkIfRegistrationVerificationCodeIsValid(userRegistrationRequest, invitation);

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

    private void checkIfRegistrationVerificationCodeIsValid(UserRegistrationRequest userRegistrationRequest, Invitation invitation) {
        if(!userRegistrationRequest.getVerificationCode().equals(invitation.getVerificationCode())) {
            throw new VerificationCodeException(INVALID_VERIFICATION_CODE);
        }
    }

    private User saveUserInformation(UserRegistrationRequest userRegistrationRequest, Invitation invitation) {

        UserGender userGender = UserGender.valueOf(userRegistrationRequest.getGender().toUpperCase());

        if (!USER_GENDERS.contains(userGender)) {
            throw new IllegalArgumentException(INVALID_GENDER);
        }

        User user = User.builder()
                .firstName(userRegistrationRequest.getFirstName())
                .lastName(userRegistrationRequest.getLastName())
                .email(userRegistrationRequest.getEmail())
                .phone(userRegistrationRequest.getPhone())
                .gender(userGender)
                .role(UserRole.USER)
                .password(passwordEncoder.encode(userRegistrationRequest.getPassword()))
                .invitation(invitation).build();

        return userRepository.save(user);
    }

    public User findUserById(String id) {
        Optional<User> user = userRepository.findByUidAndIsDeleted(id, false);
        return user.orElseThrow(() -> new ElementNotFoundException(USER_WITH_ID_NOT_FOUND + id));
    }

    public User findUserByEmail(String email) {
        Optional<User> user = userRepository.findByEmailAndIsDeleted(email, false);
        return user.orElseThrow(() -> new ElementNotFoundException(USER_WITH_EMAIL_NOT_FOUND));
    }

    public User findUserByPhone(String phone) {
        Optional<User> user = userRepository.findByPhoneAndIsDeleted(phone, false);
        return user.orElseThrow(() -> new ElementNotFoundException(USER_WITH_PHONE_NOT_FOUND + phone));
    }

    public User findUserByRoleAdmin() {
        Optional<User> user = userRepository.findByRole(UserRole.ADMIN);
        return user.orElseThrow(() -> new ElementNotFoundException(ADMIN_NOT_FOUND));
    }

    public User findUserByInvitation(Invitation invitation) {
        return userRepository.findByInvitation(invitation).orElse(null);
    }

    public UserResponse loginUser(UserLoginRequest userLoginRequest) {

        User user = userRepository.findByEmailAndIsDeleted(userLoginRequest.getEmail(), false)
                .orElseThrow(() -> new BadCredentialsException(WRONG_CREDENTIALS));

        try {
            authenticate(userLoginRequest, user);
        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException(TOKEN_HAS_EXPIRED);
        }

        return modelMapper.map(user, UserResponse.class);
    }

    private void authenticate(UserLoginRequest userLoginRequest, User user) {
        if (!passwordEncoder.matches(userLoginRequest.getPassword(), user.getPassword())) {
            throw new BadCredentialsException(WRONG_CREDENTIALS);
        }
    }

    public LoginVerificationResponse sendLoginVerificationEmail(LoginVerificationRequest loginVerificationRequest) {

        String email = loginVerificationRequest.getContact();

        User user = findUserByEmail(email);
        String formatUserName = user.getFirstName() + " " + user.getLastName();

        LoginVerification loginVerification = loginVerificationService.getLoginVerificationByUser(user);
        mailService.sendLoginVerificationMail(new MailContent(email, formatUserName, loginVerification.getVerificationCode()));

        return modelMapper.map(loginVerification, LoginVerificationResponse.class);
    }

    public LoginVerificationResponse sendLoginVerificationSMS(LoginVerificationRequest loginVerificationRequest) {

        String phone = loginVerificationRequest.getContact();

        User user = findUserByPhone(phone);

        LoginVerification loginVerification = loginVerificationService.getLoginVerificationByUser(user);
        smsService.sendLoginVerificationSMS(user.getPhone(), loginVerification.getVerificationCode());

        return modelMapper.map(loginVerification, LoginVerificationResponse.class);
    }

    private UserResponse getAuthenticatedUserResponse(User user) {

        final UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(user.getEmail());

        UserResponse userResponse = modelMapper.map(user, UserResponse.class);
        userResponse.setToken(jwtTokenUtil.generateToken(userDetails));
        return userResponse;
    }

    private void checkIfLoginVerificationCodeExpired(LoginVerification loginVerification) {
        long currentDate = System.currentTimeMillis();
        long expirationDate = loginVerification.getLoginDate() + LOGIN_VERIFICATION_EXPIRATION_TIME;
        boolean isCodeExpired = currentDate >= expirationDate;
        if(isCodeExpired) {
            throw new VerificationCodeException(VERIFICATION_CODE_HAS_EXPIRED);
        }
    }

    public UserResponse submitLoginVerificationCode(String email, String verificationCode) {
        User user = findUserByEmail(email);
        LoginVerification loginVerification = loginVerificationService.findLoginVerificationByUser(user);
        checkIfLoginVerificationCodeExpired(loginVerification);

        if(!verificationCode.equals(loginVerification.getVerificationCode())) {
            throw new VerificationCodeException(INVALID_VERIFICATION_CODE);
        }

        UserResponse authenticatedUser = getAuthenticatedUserResponse(user);
        return authenticatedUser;
    }

    public String sendResetPasswordEmail(String email) {

        User user = findUserByEmail(email);
        user.setResetPasswordDate(System.currentTimeMillis());
        userRepository.save(user);

        String formatUserName = user.getFirstName() + " " + user.getLastName();

        mailService.sendForgottenPasswordMail(new MailContent(email, formatUserName));

        return RESET_PASS_EMAIL_SUCCESS;
    }

    private void checkResetPasswordDate(User user) {
        if (user.getResetPasswordDate() == null) {
            throw new NotAllowedException(RESET_PASS_DATE_ERROR);
        }
    }

    public EmailLinkValidResponse checkIfResetPassLinkIsValid(String userId) {
        User user = findUserById(userId);

        checkResetPasswordDate(user);

        long currentDate = System.currentTimeMillis();
        long expirationDate = user.getResetPasswordDate() + RESET_PASSWORD_EXPIRATION_TIME;
        boolean isUrlExpired = currentDate >= expirationDate;

        return new EmailLinkValidResponse(user.getEmail(), isUrlExpired);
    }

    public String resetPassword(ResetPasswordRequest resetPasswordRequest) {

        checkIfMatchingPasswords(resetPasswordRequest.getPassword(), resetPasswordRequest.getRepeatPassword());

        User user = findUserById(resetPasswordRequest.getId());

        checkResetPasswordDate(user);

        // invalidate 'reset password' link in email after successful password reset
        user.setPassword(passwordEncoder.encode(resetPasswordRequest.getPassword()));
        user.setResetPasswordDate(System.currentTimeMillis() - RESET_PASSWORD_EXPIRATION_TIME);
        userRepository.save(user);

        return SUCCESSFULLY_CHANGED_PASSWORD;
    }
}
