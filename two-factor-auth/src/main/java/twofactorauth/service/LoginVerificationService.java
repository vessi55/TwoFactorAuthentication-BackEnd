package twofactorauth.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import twofactorauth.entity.LoginVerification;
import twofactorauth.entity.User;
import twofactorauth.repository.LoginVerificationRepository;

import java.util.Optional;

@Service
public class LoginVerificationService {

    private static final int LOGIN_VERIFICATION_CODE_LENGTH = 6;

    @Autowired
    private LoginVerificationRepository loginVerificationRepository;

    public LoginVerification findLoginVerificationByUser(User user) {
        Optional<LoginVerification> loginVerification = loginVerificationRepository.findByUser(user);
        return loginVerification.orElse(null);
    }

    private String generateLoginVerificationCode() {
        return RandomStringUtils.randomNumeric(LOGIN_VERIFICATION_CODE_LENGTH);
    }

    public LoginVerification getLoginVerificationByUser(User user) {

        LoginVerification loginVerification = findLoginVerificationByUser(user);

        String verificationCode = generateLoginVerificationCode();

        if(loginVerification == null) {
            loginVerification = new LoginVerification(verificationCode, user);
        }
        else {
            loginVerification.setVerificationCode(verificationCode);
            loginVerification.setLoginDate(System.currentTimeMillis());
        }
        return loginVerificationRepository.save(loginVerification);
    }
}
