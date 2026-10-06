package com.quizora.backend.dto;

import com.quizora.backend.domain.AnswerOption;
import com.quizora.backend.domain.AttemptStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

/** Request/response records for the quiz engine. */
public final class QuizDtos {

    private QuizDtos() {}

    public record StartRequest(
            @NotNull(message = "courseId is required") Long courseId,
            @NotNull(message = "year is required") Integer year,
            Boolean timed,
            @Min(value = 30, message = "timeLimitSeconds must be at least 30")
            @Max(value = 14400, message = "timeLimitSeconds must be at most 14400") Integer timeLimitSeconds) {}

    public record StartResponse(Long attemptId, Long courseId, String courseName, int year,
                                int totalQuestions, boolean timed, Integer timeLimitSeconds,
                                LocalDateTime startedAt, LocalDateTime expiresAt, AttemptStatus status) {}

    /** One question at a time - never includes the correct answer before submission. */
    public record QuestionResponse(int index, int totalQuestions, Long questionId, String questionText,
                                   String optionA, String optionB, String optionC, String optionD,
                                   AnswerOption myAnswer, Integer remainingSeconds, AttemptStatus status) {}

    public record AnswerRequest(
            @NotNull(message = "questionId is required") Long questionId,
            @NotNull(message = "selectedOption is required") AnswerOption selectedOption,
            @Min(value = 0) Integer timeSpentSeconds) {}

    public record AnswerResponse(Long questionId, AnswerOption selectedOption, boolean saved) {}

    public record ReviewItem(int index, Long questionId, String questionText,
                             String optionA, String optionB, String optionC, String optionD,
                             AnswerOption myAnswer, AnswerOption correctOption,
                             boolean correct, String explanation) {}

    public record ResultResponse(Long attemptId, Long courseId, String courseName, int year,
                                 AttemptStatus status, boolean timed, Integer timeLimitSeconds,
                                 int totalQuestions, int answeredCount, int correctCount,
                                 double scorePercent, long durationSeconds,
                                 boolean timeExpired, List<ReviewItem> review) {}

    public record AttemptSummary(Long attemptId, Long courseId, String courseName, int year,
                                 AttemptStatus status, boolean timed, LocalDateTime startedAt,
                                 LocalDateTime submittedAt, int totalQuestions, int answeredCount,
                                 int correctCount, double scorePercent, long durationSeconds) {}
}
