package com.quizora.backend.service;

import com.quizora.backend.domain.QuizAttempt;
import com.quizora.backend.domain.User;
import com.quizora.backend.dto.AuthDtos;
import com.quizora.backend.dto.DashboardDtos;
import com.quizora.backend.repository.QuizAttemptRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final QuizAttemptRepository attemptRepository;
    private final QuoteService quoteService;
    private final SubscriptionService subscriptionService;
    private final AuthService authService;

    public DashboardService(QuizAttemptRepository attemptRepository,
                            QuoteService quoteService,
                            SubscriptionService subscriptionService,
                            AuthService authService) {
        this.attemptRepository = attemptRepository;
        this.quoteService = quoteService;
        this.subscriptionService = subscriptionService;
        this.authService = authService;
    }

    @Transactional(readOnly = true)
    public DashboardDtos.DashboardResponse dashboard(User user) {
        com.quizora.backend.domain.AttemptStatus finished =
                com.quizora.backend.domain.AttemptStatus.IN_PROGRESS;
        // Use the pre-aggregated per-course roll-up: one row per course.
        List<Object[]> rollup = attemptRepository.findCourseRollupByUser(user.getId(), finished);
        long coursesCount = rollup.size();
        long answered = 0, correct = 0, seconds = 0;
        double scoreSum = 0.0;
        for (Object[] r : rollup) {
            answered += (Long) r[3];
            correct += (Long) r[4];
            seconds += (Long) r[5];
            scoreSum += ((Number) r[6]).doubleValue();
        }
        double accuracy = answered == 0 ? 0 : Math.round(correct * 1000.0 / answered) / 10.0;
        double avgScore = coursesCount == 0 ? 0
                : Math.round(scoreSum / coursesCount * 10.0) / 10.0;

        return new DashboardDtos.DashboardResponse(
                greeting(),
                user.getFullName(),
                quoteService.todayQuote(),
                new DashboardDtos.AnalyticsDto(coursesCount, answered, correct, accuracy, avgScore,
                        seconds, streakDays(user.getId())),
                subscriptionDto(user));
    }

    private AuthDtos.SubscriptionDto subscriptionDto(User user) {
        return subscriptionService.findByUser(user.getId())
                .map(s -> new AuthDtos.SubscriptionDto(
                        s.getPlan().name(), s.getStatus().name(), s.getStartDate(), s.getEndDate(),
                        Math.max(0, java.time.temporal.ChronoUnit.DAYS.between(
                                LocalDate.now(), s.getEndDate()))))
                .orElse(null);
    }

    private String greeting() {
        int hour = LocalDateTime.now().getHour();
        if (hour < 12) return "Good morning";
        if (hour < 17) return "Good afternoon";
        return "Good evening";
    }

    /** Consecutive days (ending today or yesterday) with at least one attempt. */
    private int streakDays(Long userId) {
        Set<LocalDate> activeDays = attemptRepository.findActiveDatesByUser(userId).stream()
                .map(LocalDateTime::toLocalDate).collect(Collectors.toSet());
        if (activeDays.isEmpty()) return 0;

        LocalDate today = LocalDate.now();
        LocalDate cursor = activeDays.contains(today) ? today : today.minusDays(1);
        if (!activeDays.contains(cursor)) return 0;

        int streak = 0;
        while (activeDays.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }
}
