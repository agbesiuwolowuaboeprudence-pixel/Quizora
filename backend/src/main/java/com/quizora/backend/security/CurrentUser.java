package com.quizora.backend.security;

import com.quizora.backend.domain.User;
import com.quizora.backend.exception.ForbiddenException;
import com.quizora.backend.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** Resolves the database user behind the current JWT. */
@Component
public class CurrentUser {

    private final UserRepository userRepository;

    public CurrentUser(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** The logged-in user, or throws 403 when there is no authenticated principal. */
    public User get() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof QuizoraUserDetails details) {
            return userRepository.findWithInstitution(details.getId())
                    .orElseThrow(() -> new ForbiddenException("Account no longer exists"));
        }
        throw new ForbiddenException("Not authenticated");
    }

    /** Same as get(), but returns null when unauthenticated (for optional auth endpoints). */
    public User find() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof QuizoraUserDetails details) {
            return userRepository.findWithInstitution(details.getId()).orElse(null);
        }
        return null;
    }
}
