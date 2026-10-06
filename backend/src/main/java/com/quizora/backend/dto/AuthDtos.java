package com.quizora.backend.dto;

import com.quizora.backend.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Request/response records for authentication, profile and subscriptions. */
public final class AuthDtos {

    private AuthDtos() {}

    public record RegisterRequest(
            @NotBlank(message = "full name is required") String fullName,
            @NotBlank(message = "email is required") @Email(message = "must be a valid email") String email,
            @NotBlank(message = "password is required") @Size(min = 6, message = "password must be at least 6 characters") String password) {}

    public record InstitutionalRegisterRequest(
            @NotBlank(message = "institutional code is required") String code,
            @NotBlank(message = "full name is required") String fullName,
            @NotBlank(message = "email is required") @Email(message = "must be a valid email") String email,
            @NotBlank(message = "password is required") @Size(min = 6, message = "password must be at least 6 characters") String password) {}

    public record LoginRequest(
            @NotBlank(message = "email is required") String email,
            @NotBlank(message = "password is required") String password) {}

    public record JoinCodeRequest(
            @NotBlank(message = "code is required") String code) {}

    public record SubscriptionDto(String plan, String status, LocalDate startDate,
                                  LocalDate endDate, long daysRemaining) {}

    public record UserDto(Long id, String fullName, String email, Role role,
                          Long institutionId, String institutionName, SubscriptionDto subscription) {}

    public record AuthResponse(String token, String tokenType, long expiresInSeconds, UserDto user) {}

    public record SubscribeRequest(
            @NotBlank(message = "plan is required") String plan) {}   // MONTHLY or YEARLY
}
