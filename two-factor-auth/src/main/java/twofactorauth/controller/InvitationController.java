package twofactorauth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import twofactorauth.dto.EmailLinkValidResponse;
import twofactorauth.dto.invitation.InvitationRequest;
import twofactorauth.dto.invitation.InvitationResponse;
import twofactorauth.entity.Invitation;
import twofactorauth.service.InvitationService;

import javax.validation.Valid;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/invitations")
@PreAuthorize("hasRole('ADMIN')")
public class InvitationController {
    
    @Autowired
    private InvitationService invitationService;

    @PostMapping("/send")
    public ResponseEntity<InvitationResponse> sendInvitationMail(@Valid @RequestBody InvitationRequest invitationRequest) {
        return ResponseEntity.ok(invitationService.sendInvitationEmail(invitationRequest));
    }

    @PutMapping("/resend/{invitationId}")
    public ResponseEntity<InvitationResponse> resendInvitationEmail(@PathVariable("invitationId") String invitationId) {
        return ResponseEntity.ok(invitationService.resendInvitationEmail(invitationId));
    }

    @GetMapping("/valid/{invitationId}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<EmailLinkValidResponse> checkIfRegisterLinkIsValid(@PathVariable("invitationId") String invitationId) {
        return ResponseEntity.ok(invitationService.checkIfRegisterLinkIsValid(invitationId));
    }

    @GetMapping("/id/{invitationId}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Invitation> getInvitationById(@PathVariable("invitationId") String invitationId) {
        return ResponseEntity.ok(invitationService.findInvitationById(invitationId));
    }

    @PutMapping("/{invitationId}")
    public ResponseEntity<String> deleteInvitationById(@PathVariable(value = "invitationId") String invitationId) {
        return ResponseEntity.ok(invitationService.deleteInvitationById(invitationId));
    }

    @PutMapping("/recover/{invitationId}")
    public ResponseEntity<String> recoverDeletedUser(@PathVariable("invitationId") String invitationId) {
        return ResponseEntity.ok(invitationService.recoverInvitationById(invitationId));
    }

    @GetMapping
    public ResponseEntity<List<InvitationResponse>> getAllInvitedUsers() {
        return ResponseEntity.ok(invitationService.getAllInvitedUsers());
    }

    @GetMapping("/archive")
    public ResponseEntity<List<InvitationResponse>> getUsersArchive() {
        return ResponseEntity.ok(invitationService.getUsersArchive());
    }
}
