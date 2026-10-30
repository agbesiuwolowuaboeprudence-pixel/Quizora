package com.quizora.backend.dto;

import com.quizora.backend.domain.QuizAttempt;

/** Shared mapping helpers. */
public final class Mappers {

    private Mappers() {}

    public static QuizDtos.AttemptSummary attemptSummary(QuizAttempt a) {
        return new QuizDtos.AttemptSummary(
                a.getId(), a.getCourse().getId(), a.getCourse().getName(), a.getYear(),
                a.getStatus(), a.isTimed(), a.getStartedAt(), a.getSubmittedAt(),
                a.getTotalQuestions(), a.getAnsweredCount(), a.getCorrectCount(),
                a.getScorePercent(), a.getDurationSeconds());
    }
}
