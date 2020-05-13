package twofactorauth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import twofactorauth.dto.UserInvitationRequest;
import twofactorauth.dto.UserInvitationResponse;
import twofactorauth.service.InvitationService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private InvitationService invitationService;

    @PostMapping("/sendInvitation")
    public ResponseEntity<String> sendInvitationMail(@Valid @RequestBody UserInvitationRequest userInvitationRequest) {
        return ResponseEntity.ok(invitationService.sendInvitationEmail(userInvitationRequest));
    }

    @PutMapping("/resendInvitation/{invitationId}")
    public ResponseEntity<String> resendInvitationEmail(@PathVariable("invitationId") String invitationId) {
        return ResponseEntity.ok(invitationService.resendInvitationEmail(invitationId));
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserInvitationResponse>> getAllInvitedUsers() {
        return ResponseEntity.ok(invitationService.getAllInvitedUsers());
    }
}
