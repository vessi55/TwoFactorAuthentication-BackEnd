package twofactorauth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import twofactorauth.dto.EmailLinkValidResponse;
import twofactorauth.dto.user.*;
import twofactorauth.dto.user.ResetPasswordEmailRequest;
import twofactorauth.dto.user.ResetPasswordRequest;
import twofactorauth.service.UserService;

import javax.validation.Valid;
import java.io.IOException;

@RestController
@CrossOrigin
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> loginUser(@RequestBody @Valid UserLoginRequest user) {
        return ResponseEntity.ok(userService.loginUser(user));
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@RequestBody @Valid UserRegistrationRequest user) {
        return ResponseEntity.ok(userService.registerUser(user));
    }


    @PutMapping("/verify/email")
    public ResponseEntity<LoginVerificationResponse> sendLoginVerificationEmail(@RequestBody @Valid LoginVerificationRequest loginVerificationRequest) {
        return ResponseEntity.ok(userService.sendLoginVerificationEmail(loginVerificationRequest));
    }

    @PutMapping("/verify/sms")
    public ResponseEntity<LoginVerificationResponse> sendLoginVerificationSMS(@RequestBody @Valid LoginVerificationRequest loginVerificationRequest) throws IOException {
        return ResponseEntity.ok(userService.sendLoginVerificationSMS(loginVerificationRequest));
    }

    @PostMapping("/verification")
    public ResponseEntity<UserResponse> submitLoginVerificationCode(@RequestBody @Valid UserVerificationRequest user) {
        return ResponseEntity.ok(userService.submitLoginVerificationCode(user.getEmail(), user.getVerificationCode()));
    }

    @PostMapping("/reset")
    public ResponseEntity<String> sendResetPasswordEmail(@RequestBody @Valid ResetPasswordEmailRequest resetPasswordEmailRequest) {
        return ResponseEntity.ok(userService.sendResetPasswordEmail(resetPasswordEmailRequest.getEmail()));
    }

    @GetMapping("/reset/valid/{userId}")
    public ResponseEntity<EmailLinkValidResponse> checkIfResetPassLinkIsValid(@PathVariable("userId") String userId) {
        return ResponseEntity.ok(userService.checkIfResetPassLinkIsValid(userId));
    }

    @PutMapping("/reset")
    public ResponseEntity<String> resetPassword(@RequestBody @Valid ResetPasswordRequest resetPasswordRequest) {
        return ResponseEntity.ok(userService.resetPassword(resetPasswordRequest));
    }
}
