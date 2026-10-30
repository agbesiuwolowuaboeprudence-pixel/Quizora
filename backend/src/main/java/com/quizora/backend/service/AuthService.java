package com.quizora.backend.service;

import com.quizora.backend.domain.Institution;
import com.quizora.backend.domain.LicenseCode;
import com.quizora.backend.domain.Role;
import com.quizora.backend.domain.Subscription;
import com.quizora.backend.domain.User;
import com.quizora.backend.dto.AuthDtos;
import com.quizora.backend.exception.BadRequestException;
import com.quizora.backend.exception.ConflictException;
import com.quizora.backend.exception.ForbiddenException;
import com.quizora.backend.exception.NotFoundException;
import com.quizora.backend.repository.LicenseCodeRepository;
import com.quizora.backend.repository.UserRepository;
import com.quizora.backend.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final LicenseCodeRepository licenseCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SubscriptionService subscriptionService;

    public AuthService(UserRepository userRepository,
                       LicenseCodeRepository licenseCodeRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       SubscriptionService subscriptionService) {
        this.userRepository = userRepository;
        this.licenseCodeRepository = licenseCodeRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.subscriptionService = subscriptionService;
    }

    /** Individual student sign-up - grants a free trial. */
    @Transactional
    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("An account with this email already exists");
        }
        User user = new User(request.fullName().trim(), email,
                passwordEncoder.encode(request.password()), Role.STUDENT);
        userRepository.save(user);
        subscriptionService.createTrial(user);
        return tokenResponse(user);
    }

    /** Student sign-up using an institution's bulk licence code. */
    @Transactional
    public AuthDtos.AuthResponse registerInstitutional(AuthDtos.InstitutionalRegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("An account with this email already exists - log in and join with your code instead");
        }
        LicenseCode license = requireUsableLicense(request.code());

        User user = new User(request.fullName().trim(), email,
                passwordEncoder.encode(request.password()), Role.STUDENT);
        user.setInstitution(license.getInstitution());
        user.setLicenseCode(license);
        userRepository.save(user);

        consumeSeat(license);
        subscriptionService.createInstitutional(user, license);
        return tokenResponse(user);
    }

    /** An already-registered student links their account to an institution via a code. */
    @Transactional
    public AuthDtos.UserDto joinCode(User user, AuthDtos.JoinCodeRequest request) {
        if (user.getInstitution() != null) {
            throw new ConflictException("Your account already belongs to an institution");
        }
        LicenseCode license = requireUsableLicense(request.code());
        user.setInstitution(license.getInstitution());
        user.setLicenseCode(license);
        userRepository.save(user);

        consumeSeat(license);
        subscriptionService.createInstitutional(user, license);
        return toUserDto(user);
    }

    @Transactional
    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(normalizeEmail(request.email()))
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("bad credentials");
        }
        return tokenResponse(user);
    }

    @Transactional
    public AuthDtos.UserDto toUserDto(User user) {
        Institution institution = user.getInstitution();   // safe: refreshed entity in caller tx
        Optional<Subscription> subscription = subscriptionService.findByUser(user.getId());
        AuthDtos.SubscriptionDto subscriptionDto = subscription
                .map(s -> new AuthDtos.SubscriptionDto(
                        s.getPlan().name(),
                        s.getStatus().name(),
                        s.getStartDate(),
                        s.getEndDate(),
                        Math.max(0, ChronoUnit.DAYS.between(java.time.LocalDate.now(), s.getEndDate()))))
                .orElse(null);
        return new AuthDtos.UserDto(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                institution != null ? institution.getId() : null,
                institution != null ? institution.getName() : null,
                subscriptionDto);
    }

    private AuthDtos.AuthResponse tokenResponse(User user) {
        String token = jwtService.generateToken(user);
        return new AuthDtos.AuthResponse(token, "Bearer",
                jwtService.getExpirationMs() / 1000, toUserDto(user));
    }

    private LicenseCode requireUsableLicense(String rawCode) {
        LicenseCode license = licenseCodeRepository.findByCodeIgnoreCase(rawCode.trim())
                .orElseThrow(() -> new NotFoundException("Invalid institutional code"));
        if (license.getStatus() != com.quizora.backend.domain.LicenseStatus.ACTIVE) {
            throw new BadRequestException("This institutional code is no longer active");
        }
        if (license.getUsedSeats() >= license.getTotalSeats()) {
            throw new BadRequestException("All seats on this institutional code have been used");
        }
        if (java.time.LocalDate.now().isAfter(license.getValidUntil())) {
            throw new BadRequestException("This institutional code expired on " + license.getValidUntil());
        }
        if (java.time.LocalDate.now().isBefore(license.getValidFrom())) {
            throw new BadRequestException("This institutional code is not valid until " + license.getValidFrom());
        }
        return license;
    }

    private void consumeSeat(LicenseCode license) {
        license.setUsedSeats(license.getUsedSeats() + 1);
        if (license.getUsedSeats() >= license.getTotalSeats()) {
            license.setStatus(com.quizora.backend.domain.LicenseStatus.EXHAUSTED);
        }
        licenseCodeRepository.save(license);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
