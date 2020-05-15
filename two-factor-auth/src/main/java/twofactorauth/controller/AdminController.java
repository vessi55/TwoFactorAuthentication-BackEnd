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
    public ResponseEntity sendInvitationMail(@Valid @RequestBody UserInvitationRequest userInvitationRequest) {
        invitationService.sendInvitationEmail(userInvitationRequest);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/resendInvitation/{invitationId}")
    public ResponseEntity resendInvitationEmail(@PathVariable("invitationId") String invitationId) {
        invitationService.resendInvitationEmail(invitationId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserInvitationResponse>> getAllInvitedUsers() {
        return ResponseEntity.ok(invitationService.getAllInvitedUsers());
    }
}
