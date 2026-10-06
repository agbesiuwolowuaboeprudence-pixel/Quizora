package com.quizora.backend.dto;

import com.quizora.backend.domain.AnswerOption;
import com.quizora.backend.domain.Level;
import com.quizora.backend.domain.QuestionSection;
import com.quizora.backend.domain.Role;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

/** DTOs for the admin panel (content upload + user management). */
public final class AdminDtos {

    private AdminDtos() {}

    public record CreateCourseRequest(
            @NotBlank(message = "name is required") String name,
            @NotBlank(message = "code is required") String code,
            String description,
            Level level) {}

    public record QuestionRequest(
            @NotNull(message = "courseId is required") Long courseId,
            @NotNull(message = "year is required") Integer year,
            QuestionSection section,
            @NotBlank(message = "questionText is required") String questionText,
            @NotBlank(message = "optionA is required") String optionA,
            @NotBlank(message = "optionB is required") String optionB,
            @NotBlank(message = "optionC is required") String optionC,
            @NotBlank(message = "optionD is required") String optionD,
            @NotNull(message = "correctOption is required") AnswerOption correctOption,
            String explanation,
            Integer marks) {}

    public record BulkQuestionItem(
            @NotBlank(message = "questionText is required") String questionText,
            @NotBlank(message = "optionA is required") String optionA,
            @NotBlank(message = "optionB is required") String optionB,
            @NotBlank(message = "optionC is required") String optionC,
            @NotBlank(message = "optionD is required") String optionD,
            @NotNull(message = "correctOption is required") AnswerOption correctOption,
            String explanation,
            Integer marks) {}

    public record BulkQuestionRequest(
            @NotNull(message = "courseId is required") Long courseId,
            @NotNull(message = "year is required") Integer year,
            QuestionSection section,
            @NotEmpty(message = "questions list is required") @Valid List<BulkQuestionItem> questions) {}

    public record AdminQuestionResponse(Long id, Long courseId, String courseName, int year,
                                        QuestionSection section, String questionText,
                                        String optionA, String optionB, String optionC, String optionD,
                                        AnswerOption correctOption, String explanation, int marks) {}

    public record UserResponse(Long id, String fullName, String email, Role role,
                               Long institutionId, String institutionName,
                               LocalDateTime createdAt, String subscriptionStatus,
                               LocalDateTime subscriptionEndsAt) {}

    public record ActivateSubscriptionRequest(
            @NotNull(message = "userId is required") Long userId,
            @NotBlank(message = "plan is required") String plan,       // MONTHLY / YEARLY
            @Min(value = 1) @Max(value = 3650) Integer days) {}         // optional custom duration

    public record AdminStatsResponse(long users, long students, long institutionAdmins,
                                     long institutions, long courses, long questions, long attempts) {}

    public record MessageResponse(String message) {}
}
