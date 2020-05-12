package twofactorauth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import twofactorauth.entity.Invitation;
import twofactorauth.entity.User;
import twofactorauth.util.mailContents.RegistrationMailContent;

@Slf4j
@Service
public class MailService {

    private static final String MAIL_URL = "url";
    private static final String MAIL_SERVICE = "mailService";
    private static final String AUTO_EMAIL = "twofactorauthProject@outlook.com";
    private static final String ERROR_WITH_SENDING_THE_EMAIL = "Error with sending the email -> ";

    private static final String USER_EMAIL = "email";
    private static final String ADMIN_NAME = "adminName";

    private static final String REGISTRATION_MAIL_SUBJECT = "Registration";

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

    private String buildRegisterMailContent(RegistrationMailContent registrationMailContent) {

        Context context = new Context();
        context.setVariable(USER_EMAIL, registrationMailContent.getEmail());
        context.setVariable(ADMIN_NAME, registrationMailContent.getAdminName());
        context.setVariable(MAIL_URL, registerUrl);
        context.setVariable(MAIL_SERVICE, this);

        return templateEngine.process("RegistrationMailTemplate", context);
    }

    @Async
    void sendRegistrationMail(String recipient, RegistrationMailContent registrationMailContent) {

        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom(AUTO_EMAIL);
            messageHelper.setTo(recipient);
            messageHelper.setSubject(REGISTRATION_MAIL_SUBJECT);

            String content = buildRegisterMailContent(registrationMailContent);
            messageHelper.setText(content, true);
        };

        sendMail(messagePreparator);
    }

    private void sendMail(MimeMessagePreparator messagePreparator) {
        try {
            mailSender.send(messagePreparator);
        } catch (MailException e) {
            log.error(ERROR_WITH_SENDING_THE_EMAIL + e.getMessage());
        }
    }

    public String getUserName(String email) {
        User user = userService.findUserByEmail(email);
        return user.getFirstName() + " " + user.getLastName();
    }

    public String getRegisterUrl(String email) {
        Invitation invitation = invitationService.getInvitationByEmail(email);
        return registerUrl + "/register/" + invitation.getUid();
    }
}
