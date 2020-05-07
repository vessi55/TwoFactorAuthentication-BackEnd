package twofactorauth.service;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import twofactorauth.dto.UserRegistrationRequest;
import twofactorauth.dto.UserRegistrationResponse;
import twofactorauth.entity.Invitation;
import twofactorauth.entity.User;
import twofactorauth.enums.UserStatus;
import twofactorauth.exception.ElementAlreadyExistsException;
import twofactorauth.exception.PasswordsDoNotMatchException;
import twofactorauth.repository.UserRepository;

@Service
public class UserService {

    private static final String EMAIL_ALREADY_TAKEN = "Email is already taken by another user!";
    private static final String PHONE_NUMBER_ALREADY_TAKEN = "Phone Number is already taken by another user!";
    private static final String NOT_MATCHING_PASSWORDS = "Passwords do not match!";

    private final UserRepository userRepository;

    private final InvitationService invitationService;

    private final ModelMapper modelMapper;

    @Autowired
    public UserService(UserRepository userRepository, InvitationService invitationService, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.invitationService = invitationService;
        this.modelMapper = modelMapper;
    }

    public UserRegistrationResponse registerUser(UserRegistrationRequest userRegistrationRequest) {

        checkIfEmailAlreadyTaken(userRegistrationRequest.getEmail());
        checkIfPhoneAlreadyTaken(userRegistrationRequest.getPhone());

        checkIfMatchingPasswords(userRegistrationRequest.getPassword(), userRegistrationRequest.getRepeatPassword());

        Invitation invitation = invitationService.getInvitationByEmail(userRegistrationRequest.getEmail());
        invitation.setStatus(UserStatus.REGISTERED);
        invitationService.saveInvitation(invitation);

        User user = saveUserInformation(userRegistrationRequest, invitation);

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
}
