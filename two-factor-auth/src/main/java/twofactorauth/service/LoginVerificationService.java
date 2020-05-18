package twofactorauth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import twofactorauth.entity.LoginVerification;
import twofactorauth.entity.User;
import twofactorauth.repository.LoginVerificationRepository;

import java.util.Optional;

@Service
public class LoginVerificationService {

    @Autowired
    private LoginVerificationRepository loginVerificationRepository;

    public LoginVerification save(LoginVerification loginVerification) {
        return loginVerificationRepository.save(loginVerification);
    }

    public LoginVerification findLoginVerificationByUser(User user) {
        Optional<LoginVerification> loginVerification = loginVerificationRepository.findByUser(user);
        return loginVerification.orElse(null);
    }
}
