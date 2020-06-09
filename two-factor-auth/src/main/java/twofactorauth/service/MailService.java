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
import twofactorauth.entity.User;
import twofactorauth.util.MailContent;

@Slf4j
@Service
public class MailService {

    private static final String AUTO_EMAIL = System.getenv("MAIL_NAME");
    private static final String MAIL_URL = "mailUrl";
    private static final String MAIL_SERVICE = "mailService";
    private static final String SEND_MAIL_FAILURE = "Error with sending the email -> ";

    private static final String USER_EMAIL = "email";
    private static final String USER_NAME = "userName";
    private static final String VERIFICATION_CODE = "verificationCode";

    private static final String REGISTRATION_MAIL_SUBJECT = "Registration";
    private static final String LOGIN_VERIFICATION_MAIL_SUBJECT = "Login Verification";
    private static final String FORGOTTEN_PASSWORD_MAIL_SUBJECT = "Forgotten Password";

    public static final String REGISTRATION_MAIL_TEMPLATE = "RegistrationMailTemplate";
    public static final String LOGIN_VERIFICATION_MAIL_TEMPLATE = "LoginVerificationMailTemplate";
    public static final String FORGOTTEN_PASSWORD_MAIL_TEMPLATE = "ForgottenPasswordMailTemplate";

    @Autowired
    private UserService userService;

    @Autowired
    private InvitationService invitationService;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${mail.url}")
    private String mailUrl;

    private String buildRegisterMailContent(MailContent registerMailContent) {

        Context context = new Context();
        context.setVariable(USER_EMAIL, registerMailContent.getEmail());
        context.setVariable(USER_NAME, registerMailContent.getUserName());
        context.setVariable(VERIFICATION_CODE, registerMailContent.getVerificationCode());
        context.setVariable(MAIL_URL, mailUrl);
        context.setVariable(MAIL_SERVICE, this);

        return templateEngine.process(REGISTRATION_MAIL_TEMPLATE, context);
    }

    private String buildLoginVerificationMailContent(MailContent loginMailContent) {

        Context context = new Context();
        context.setVariable(USER_NAME, loginMailContent.getUserName());
        context.setVariable(VERIFICATION_CODE, loginMailContent.getVerificationCode());

        return templateEngine.process(LOGIN_VERIFICATION_MAIL_TEMPLATE, context);
    }

    private String buildForgottenPasswordMailContent(MailContent forgottenMailContent) {

        Context context = new Context();
        context.setVariable(USER_EMAIL, forgottenMailContent.getEmail());
        context.setVariable(USER_NAME, forgottenMailContent.getUserName());
        context.setVariable(MAIL_URL, mailUrl);
        context.setVariable(MAIL_SERVICE, this);

        return templateEngine.process(FORGOTTEN_PASSWORD_MAIL_TEMPLATE, context);
    }

    @Async
    public void sendRegistrationMail(MailContent registerMailContent, Invitation invitation) {

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
    public void sendLoginVerificationMail(MailContent loginMailContent) {

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

    @Async
    public void sendForgottenPasswordMail(MailContent forgottenPasswordMailContent) {

        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setSubject(FORGOTTEN_PASSWORD_MAIL_SUBJECT);
            messageHelper.setFrom(AUTO_EMAIL);
            messageHelper.setTo(forgottenPasswordMailContent.getEmail());

            String content = buildForgottenPasswordMailContent(forgottenPasswordMailContent);
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
        return mailUrl + "/register/" + invitation.getUid();
    }

    public String getResetPasswordUrl(String email) {
        User user = userService.findUserByEmail(email);
        return mailUrl + "/reset-password/" + user.getUid();
    }
}
