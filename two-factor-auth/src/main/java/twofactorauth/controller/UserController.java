package twofactorauth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import twofactorauth.dto.LoginVerificationResponse;
import twofactorauth.dto.UserLoginRequest;
import twofactorauth.dto.UserRegistrationRequest;
import twofactorauth.dto.UserResponse;
import twofactorauth.service.UserService;

import javax.validation.Valid;

@RestController
@CrossOrigin
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@RequestBody @Valid UserRegistrationRequest user) {
        return ResponseEntity.ok(userService.registerUser(user));
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> loginUser(@RequestBody @Valid UserLoginRequest user) {
        return ResponseEntity.ok(userService.loginUser(user));
    }

    @PutMapping("/verify/{email}")
    public ResponseEntity<LoginVerificationResponse> sendVerificationEmail(@PathVariable(value = "email") String email) {
        return ResponseEntity.ok(userService.sendVerificationEmail(email));
    }
}
