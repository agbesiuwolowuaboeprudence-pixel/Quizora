package com.quizora.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** DTOs for institutions, licence codes and the reporting dashboard. */
public final class InstitutionDtos {

    private InstitutionDtos() {}

    public record CreateInstitutionRequest(
            @NotBlank(message = "institution name is required") String name,
            @NotBlank(message = "contact name is required") String contactName,
            @NotBlank(message = "contact email is required") @Email(message = "must be a valid email") String contactEmail,
            String phone,
            @NotBlank(message = "admin password is required") String adminPassword) {}

    public record LicenseDto(Long id, String code, int totalSeats, int usedSeats,
                             LocalDate validFrom, LocalDate validUntil, String status) {}

    public record InstitutionResponse(Long id, String name, String contactName, String contactEmail,
                                      String phone, long studentCount, List<LicenseDto> licenses) {}

    public record CreateLicenseRequest(
            @NotNull(message = "totalSeats is required") @Min(value = 1, message = "totalSeats must be at least 1") Integer totalSeats,
            @NotNull(message = "validFrom is required") LocalDate validFrom,
            @NotNull(message = "validUntil is required") LocalDate validUntil) {}

    public record StudentDto(Long id, String fullName, String email, LocalDateTime joinedAt,
                             long attempts, long questionsAttempted, double accuracyPercent,
                             double averageScorePercent, LocalDateTime lastActiveAt,
                             String subscriptionStatus) {}

    public record InstitutionTotalsDto(long students, long studentsActive30Days, long attempts,
                                       long questionsAttempted, double accuracyPercent,
                                       int licenseSeatsTotal, int licenseSeatsUsed) {}

    public record ReportSummaryResponse(Long institutionId, String institutionName,
                                        InstitutionTotalsDto totals, List<StudentDto> students) {}
}
