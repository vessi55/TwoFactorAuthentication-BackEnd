package twofactorauth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import twofactorauth.dto.UserInvitationRequest;
import twofactorauth.service.UserService;

import javax.validation.Valid;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @PostMapping("/sendInvitation")
    public ResponseEntity<String> sendInvitationMail(@Valid @RequestBody UserInvitationRequest userInvitationRequest) {
        return ResponseEntity.ok(userService.sendInvitation(userInvitationRequest));
    }
}
