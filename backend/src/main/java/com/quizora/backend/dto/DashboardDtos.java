package com.quizora.backend.dto;

import java.time.LocalDateTime;

/** DTOs for the student dashboard (welcome, daily quote, analytics widget). */
public final class DashboardDtos {

    private DashboardDtos() {}

    public record QuoteDto(String text, String author, LocalDateTime validUntil) {}

    public record AnalyticsDto(long coursesAttempted, long questionsAttempted, long correctAnswers,
                               double accuracyPercent, double averageScorePercent,
                               long totalTimeSpentSeconds, int currentStreakDays) {}

    public record DashboardResponse(String greeting, String fullName, QuoteDto quote,
                                    AnalyticsDto analytics, AuthDtos.SubscriptionDto subscription) {}
}
