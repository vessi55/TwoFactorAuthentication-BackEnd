package twofactorauth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import twofactorauth.dto.RegisterUrlValidResponse;
import twofactorauth.dto.InvitationRequest;
import twofactorauth.dto.InvitationResponse;
import twofactorauth.entity.Invitation;
import twofactorauth.service.InvitationService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/invitations")
@CrossOrigin
public class InvitationController {

    @Autowired
    private InvitationService invitationService;

    @PostMapping("/send")
    public ResponseEntity sendInvitationMail(@Valid @RequestBody InvitationRequest invitationRequest) {
        invitationService.sendInvitationEmail(invitationRequest);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/resend/{invitationId}")
    public ResponseEntity resendInvitationEmail(@PathVariable("invitationId") String invitationId) {
        invitationService.resendInvitationEmail(invitationId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{invitationId}")
    public ResponseEntity<Invitation> getInvitationById(@PathVariable("invitationId") String invitationId) {
        return ResponseEntity.ok(invitationService.findInvitationById(invitationId));
    }

    @PutMapping("/{invitationId}")
    public ResponseEntity<String> deleteInvitationById(@PathVariable(value = "invitationId") String invitationId) {
        return ResponseEntity.ok(invitationService.deleteInvitationById(invitationId));
    }

    @GetMapping("/valid/{invitationId}")
    public ResponseEntity<RegisterUrlValidResponse> checkIfEmailLinkIsValid(@PathVariable("invitationId") String invitationId) {
        return ResponseEntity.ok(invitationService.checkIfEmailLinkIsValid(invitationId));
    }

    @GetMapping
    public ResponseEntity<List<InvitationResponse>> getAllInvitedUsers() {
        return ResponseEntity.ok(invitationService.getAllInvitedUsers());
    }
}
