package com.quizora.backend.service;

import com.quizora.backend.domain.Institution;
import com.quizora.backend.domain.LicenseCode;
import com.quizora.backend.domain.Role;
import com.quizora.backend.domain.SubscriptionStatus;
import com.quizora.backend.domain.User;
import com.quizora.backend.domain.AttemptStatus;
import com.quizora.backend.dto.InstitutionDtos;
import com.quizora.backend.exception.ConflictException;
import com.quizora.backend.exception.ForbiddenException;
import com.quizora.backend.exception.NotFoundException;
import com.quizora.backend.repository.InstitutionRepository;
import com.quizora.backend.repository.LicenseCodeRepository;
import com.quizora.backend.repository.QuizAttemptRepository;
import com.quizora.backend.repository.SubscriptionRepository;
import com.quizora.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InstitutionService {

    private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // no 0/O/1/I
    private static final SecureRandom RANDOM = new SecureRandom();

    private final InstitutionRepository institutionRepository;
    private final UserRepository userRepository;
    private final LicenseCodeRepository licenseCodeRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final QuizAttemptRepository attemptRepository;
    private final PasswordEncoder passwordEncoder;

    public InstitutionService(InstitutionRepository institutionRepository,
                              UserRepository userRepository,
                              LicenseCodeRepository licenseCodeRepository,
                              SubscriptionRepository subscriptionRepository,
                              QuizAttemptRepository attemptRepository,
                              PasswordEncoder passwordEncoder) {
        this.institutionRepository = institutionRepository;
        this.userRepository = userRepository;
        this.licenseCodeRepository = licenseCodeRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.attemptRepository = attemptRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Admin creates an institution together with its admin account. */
    @Transactional
    public InstitutionDtos.InstitutionResponse create(InstitutionDtos.CreateInstitutionRequest request) {
        String email = request.contactEmail().trim().toLowerCase();
        if (institutionRepository.findByContactEmailIgnoreCase(email).isPresent()
                || userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("An account with this email already exists");
        }
        Institution institution = institutionRepository.save(
                new Institution(request.name().trim(), request.contactName().trim(), email, request.phone()));

        User admin = new User(request.contactName().trim(), email,
                passwordEncoder.encode(request.adminPassword()), Role.INSTITUTION_ADMIN);
        admin.setInstitution(institution);
        userRepository.save(admin);

        return toResponse(institution);
    }

    /** The institution's own overview (licence codes + seat usage). */
    @Transactional(readOnly = true)
    public InstitutionDtos.InstitutionResponse get(Long institutionId, User actor) {
        requireInstitutionAccess(actor, institutionId);
        return toResponse(requireInstitution(institutionId));
    }

    /** Generates a new bulk licence code for an institution. */
    @Transactional
    public InstitutionDtos.LicenseDto createLicense(Long institutionId,
                                                    InstitutionDtos.CreateLicenseRequest request,
                                                    User actor) {
        requireInstitutionAccess(actor, institutionId);
        Institution institution = requireInstitution(institutionId);
        if (request.validUntil().isBefore(request.validFrom())) {
            throw new ConflictException("validUntil must be on or after validFrom");
        }
        LicenseCode license = licenseCodeRepository.save(
                new LicenseCode(generateCode(), institution, request.totalSeats(),
                        request.validFrom(), request.validUntil()));
        return toLicenseDto(license);
    }

    /** Students onboarded under this institution with a progress snapshot each. */
    @Transactional(readOnly = true)
    public List<InstitutionDtos.StudentDto> students(Long institutionId, User actor) {
        requireInstitutionAccess(actor, institutionId);
        return buildStudents(institutionId);
    }

    /** Summary for the institutional reporting dashboard. */
    @Transactional(readOnly = true)
    public InstitutionDtos.ReportSummaryResponse summary(Long institutionId, User actor) {
        requireInstitutionAccess(actor, institutionId);
        Institution institution = requireInstitution(institutionId);
        List<InstitutionDtos.StudentDto> students = buildStudents(institutionId);

        long active30 = students.stream()
                .filter(s -> s.lastActiveAt() != null
                        && s.lastActiveAt().isAfter(LocalDateTime.now().minusDays(30)))
                .count();

        Object[] totalsRow = attemptRepository
                .findTotalsForInstitution(institutionId, AttemptStatus.IN_PROGRESS)
                .stream().findFirst().orElse(null);

        long attempts = totalsRow != null ? (Long) totalsRow[0] : 0;
        long answered = totalsRow != null ? (Long) totalsRow[1] : 0;
        long correct = totalsRow != null ? (Long) totalsRow[2] : 0;
        double accuracy = answered == 0 ? 0 : Math.round(correct * 1000.0 / answered) / 10.0;

        List<LicenseCode> licenses = licenseCodeRepository.findByInstitutionIdOrderByCreatedAtDesc(institutionId);
        int seatsTotal = licenses.stream().mapToInt(LicenseCode::getTotalSeats).sum();
        int seatsUsed = licenses.stream().mapToInt(LicenseCode::getUsedSeats).sum();

        InstitutionDtos.InstitutionTotalsDto totals = new InstitutionDtos.InstitutionTotalsDto(
                students.size(), active30, attempts, answered, accuracy, seatsTotal, seatsUsed);

        return new InstitutionDtos.ReportSummaryResponse(
                institution.getId(), institution.getName(), totals, students);
    }

    public void requireInstitutionAccess(User actor, Long institutionId) {
        if (actor.getRole() == Role.ADMIN) return;
        if (actor.getRole() == Role.INSTITUTION_ADMIN
                && actor.getInstitution() != null
                && institutionId.equals(actor.getInstitution().getId())) return;
        throw new ForbiddenException("You do not have access to this institution's data");
    }

    public Institution requireInstitution(Long institutionId) {
        return institutionRepository.findById(institutionId)
                .orElseThrow(() -> new NotFoundException("Institution not found"));
    }

    // ------------------------------------------------------------------ internals

    private List<InstitutionDtos.StudentDto> buildStudents(Long institutionId) {
        // students only - the institution's own admin account is not a learner
        List<User> users = userRepository.findByInstitutionIdOrderByFullNameAsc(institutionId).stream()
                .filter(u -> u.getRole() == Role.STUDENT)
                .toList();

        Map<Long, Object[]> rollup = new HashMap<>();
        attemptRepository.findStudentRollupForInstitution(institutionId, AttemptStatus.IN_PROGRESS)
                .forEach(row -> rollup.put((Long) row[0], row));

        return users.stream().map(user -> {
            Object[] row = rollup.get(user.getId());
            long attempts = row != null ? (Long) row[1] : 0;
            long answered = row != null ? (Long) row[2] : 0;
            long correct = row != null ? (Long) row[3] : 0;
            LocalDateTime lastActive = row != null ? (LocalDateTime) row[4] : null;
            double avgScore = row != null && row[5] != null
                    ? Math.round((Double) row[5] * 10.0) / 10.0 : 0;

            String subscriptionStatus = subscriptionRepository.findByUserId(user.getId())
                    .map(s -> s.getStatus().name())
                    .orElse(SubscriptionStatus.PENDING.name());

            return new InstitutionDtos.StudentDto(
                    user.getId(), user.getFullName(), user.getEmail(), user.getCreatedAt(),
                    attempts, answered,
                    answered == 0 ? 0 : Math.round(correct * 1000.0 / answered) / 10.0,
                    avgScore, lastActive, subscriptionStatus);
        }).toList();
    }

    private InstitutionDtos.InstitutionResponse toResponse(Institution institution) {
        List<InstitutionDtos.LicenseDto> licenses =
                licenseCodeRepository.findByInstitutionIdOrderByCreatedAtDesc(institution.getId())
                        .stream().map(InstitutionService::toLicenseDto).toList();
        return new InstitutionDtos.InstitutionResponse(
                institution.getId(), institution.getName(), institution.getContactName(),
                institution.getContactEmail(), institution.getPhone(),
                userRepository.countByInstitutionIdAndRole(institution.getId(), Role.STUDENT), licenses);
    }

    private static InstitutionDtos.LicenseDto toLicenseDto(LicenseCode license) {
        return new InstitutionDtos.LicenseDto(
                license.getId(), license.getCode(), license.getTotalSeats(), license.getUsedSeats(),
                license.getValidFrom(), license.getValidUntil(), license.getStatus().name());
    }

    private String generateCode() {
        for (int attempt = 0; attempt < 20; attempt++) {
            String candidate = "QZRA-" + randomBlock() + "-" + randomBlock();
            if (!licenseCodeRepository.findByCodeIgnoreCase(candidate).isPresent()) {
                return candidate;
            }
        }
        throw new IllegalStateException("Could not generate a unique licence code");
    }

    private String randomBlock() {
        StringBuilder sb = new StringBuilder(4);
        for (int i = 0; i < 4; i++) {
            sb.append(CODE_ALPHABET.charAt(RANDOM.nextInt(CODE_ALPHABET.length())));
        }
        return sb.toString();
    }
}
