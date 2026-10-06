package com.quizora.backend.security;

import com.quizora.backend.domain.User;
import com.quizora.backend.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class QuizoraUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public QuizoraUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public QuizoraUserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        QuizoraUserDetails details = loadByEmail(email);
        if (details == null) {
            throw new UsernameNotFoundException("No user with email " + email);
        }
        return details;
    }

    /** Null-safe lookup used by the JWT filter. */
    public QuizoraUserDetails loadByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .map(this::toDetails)
                .orElse(null);
    }

    private QuizoraUserDetails toDetails(User user) {
        return new QuizoraUserDetails(user.getId(), user.getEmail(), user.getPasswordHash(), user.getRole());
    }
}
