package twofactorauth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import twofactorauth.entity.Invitation;
import twofactorauth.util.mailContents.RegisterAndLoginMailContent;

@Slf4j
@Service
public class MailService {

    private static final String MAIL_REGISTER_URL = "registerUrl";
    private static final String MAIL_PROJECT_TEAM = "projectTeam";
    private static final String MAIL_SERVICE = "mailService";
    private static final String AUTO_EMAIL = "twofactorauthProject@outlook.com";
    private static final String SEND_MAIL_FAILURE = "Error with sending the email -> ";

    private static final String USER_EMAIL = "email";
    private static final String USER_NAME = "userName";
    private static final String VERIFICATION_CODE = "verificationCode";

    private static final String REGISTRATION_MAIL_SUBJECT = "Registration";
    private static final String LOGIN_VERIFICATION_MAIL_SUBJECT = "Login Verification";

    @Autowired
    private UserService userService;

    @Autowired
    private InvitationService invitationService;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${mail.registerUrl}")
    private String registerUrl;

    private String buildRegisterMailContent(RegisterAndLoginMailContent registerMailContent) {

        Context context = new Context();
        context.setVariable(USER_EMAIL, registerMailContent.getEmail());
        context.setVariable(USER_NAME, registerMailContent.getUserName());
        context.setVariable(VERIFICATION_CODE, registerMailContent.getVerificationCode());
        context.setVariable(MAIL_REGISTER_URL, registerUrl);
        context.setVariable(MAIL_SERVICE, this);

        return templateEngine.process("RegistrationMailTemplate", context);
    }

    private String buildLoginVerificationMailContent(RegisterAndLoginMailContent loginMailContent) {

        Context context = new Context();
        context.setVariable(USER_NAME, loginMailContent.getUserName());
        context.setVariable(VERIFICATION_CODE, loginMailContent.getVerificationCode());

        return templateEngine.process("LoginVerificationMailTemplate", context);
    }

    @Async
    public void sendRegistrationMail(RegisterAndLoginMailContent registerMailContent, Invitation invitation) {

        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setSubject(REGISTRATION_MAIL_SUBJECT);
            messageHelper.setFrom(AUTO_EMAIL);
            messageHelper.setTo(registerMailContent.getEmail());

            String content = buildRegisterMailContent(registerMailContent);
            messageHelper.setText(content, true);
        };

        try{
            mailSender.send(messagePreparator);
        }catch (Exception ex){
            invitationService.delete(invitation);
            log.error(SEND_MAIL_FAILURE + ex.getMessage());
        }
    }

    @Async
    public void sendLoginVerificationMail(RegisterAndLoginMailContent loginMailContent) {

        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setSubject(LOGIN_VERIFICATION_MAIL_SUBJECT);
            messageHelper.setFrom(AUTO_EMAIL);
            messageHelper.setTo(loginMailContent.getEmail());

            String content = buildLoginVerificationMailContent(loginMailContent);
            messageHelper.setText(content, true);
        };

        try{
            mailSender.send(messagePreparator);
        }catch (Exception ex){
            log.error(SEND_MAIL_FAILURE + ex.getMessage());
        }
    }

    public String getRegisterUrl(String email) {
        Invitation invitation = invitationService.findInvitationByEmail(email);
        return registerUrl + invitation.getUid();
    }
}
