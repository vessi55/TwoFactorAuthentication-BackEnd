package twofactorauth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import twofactorauth.entity.LoginVerification;
import twofactorauth.entity.User;
import twofactorauth.repository.LoginVerificationRepository;

import java.util.Optional;
import java.util.Random;

@Service
public class LoginVerificationService {

    private static final int LOW_VALUE_VERIFICATION_CODE = 100;
    private static final int HIGH_VALUE_VERIFICATION_CODE = 999;

    @Autowired
    private LoginVerificationRepository loginVerificationRepository;

    public LoginVerification findLoginVerificationByUser(User user) {
        Optional<LoginVerification> loginVerification = loginVerificationRepository.findByUser(user);
        return loginVerification.orElse(null);
    }

    public LoginVerification getLoginVerificationByUser(User user) {

        LoginVerification loginVerification = findLoginVerificationByUser(user);

        String verificationCode = String.valueOf(new Random().nextInt(HIGH_VALUE_VERIFICATION_CODE) + LOW_VALUE_VERIFICATION_CODE);

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
