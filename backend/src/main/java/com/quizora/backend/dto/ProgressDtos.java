package com.quizora.backend.dto;

import java.time.LocalDate;
import java.util.List;

/** DTOs for the progress/analytics page. */
public final class ProgressDtos {

    private ProgressDtos() {}

    public record TotalsDto(long attemptsCompleted, long questionsAttempted, long correctAnswers,
                            double accuracyPercent, long totalTimeSpentSeconds) {}

    public record CourseProgressDto(Long courseId, String courseName, long attempts,
                                    long questionsAttempted, long correctAnswers,
                                    double accuracyPercent, double averageScorePercent,
                                    long totalTimeSpentSeconds) {}

    public record TrendPointDto(LocalDate date, int attempts, double averageScorePercent) {}

    public record ProgressResponse(TotalsDto totals, List<CourseProgressDto> perCourse,
                                   List<TrendPointDto> trend,
                                   List<QuizDtos.AttemptSummary> recentAttempts) {}
}
