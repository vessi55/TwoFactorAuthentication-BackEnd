package twofactorauth.service;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import twofactorauth.repository.UserRepository;

@Service
public class JwtUserDetailsService implements UserDetailsService {

    private static final String ROLE_PREFIX = "ROLE_";
    private static final String WRONG_CREDENTIALS = "Wrong Credentials";

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) {

        twofactorauth.entity.User user = userRepository.findByEmailAndIsDeleted(email, false)
                .orElseThrow(() -> new BadCredentialsException(WRONG_CREDENTIALS));

        return new User(user.getEmail(), user.getPassword(), Collections.singletonList(
                new SimpleGrantedAuthority(ROLE_PREFIX + user.getRole().name())));
    }
}