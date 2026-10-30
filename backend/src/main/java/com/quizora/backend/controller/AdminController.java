package com.quizora.backend.controller;

import com.quizora.backend.domain.Role;
import com.quizora.backend.dto.AdminDtos;
import com.quizora.backend.dto.CourseDtos;
import com.quizora.backend.dto.InstitutionDtos;
import com.quizora.backend.service.AdminService;
import com.quizora.backend.service.InstitutionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/** Admin panel API: content upload (question bank) + user management. */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final InstitutionService institutionService;

    public AdminController(AdminService adminService, InstitutionService institutionService) {
        this.adminService = adminService;
        this.institutionService = institutionService;
    }

    @GetMapping("/stats")
    public AdminDtos.AdminStatsResponse stats() {
        return adminService.stats();
    }

    @PostMapping("/courses")
    public CourseDtos.CourseResponse createCourse(@Valid @RequestBody AdminDtos.CreateCourseRequest request) {
        return adminService.createCourse(request);
    }

    @PostMapping("/questions")
    public AdminDtos.AdminQuestionResponse createQuestion(@Valid @RequestBody AdminDtos.QuestionRequest request) {
        return adminService.createQuestion(request);
    }

    /** Bulk upload a full past-question paper at once. */
    @PostMapping("/questions/bulk")
    public AdminDtos.MessageResponse createQuestionsBulk(
            @Valid @RequestBody AdminDtos.BulkQuestionRequest request) {
        return adminService.createQuestionsBulk(request);
    }

    @GetMapping("/questions")
    public List<AdminDtos.AdminQuestionResponse> listQuestions(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Integer year) {
        return adminService.listQuestions(Optional.ofNullable(courseId), Optional.ofNullable(year));
    }

    @DeleteMapping("/questions/{id}")
    public AdminDtos.MessageResponse deleteQuestion(@PathVariable Long id) {
        adminService.deleteQuestion(id);
        return new AdminDtos.MessageResponse("Question deleted");
    }

    @GetMapping("/users")
    public List<AdminDtos.UserResponse> listUsers(@RequestParam(required = false) Role role) {
        return adminService.listUsers(Optional.ofNullable(role));
    }

    /** Manually activate/extend a student's subscription (offline payments, comps). */
    @PostMapping("/subscriptions/activate")
    public AdminDtos.UserResponse activateSubscription(
            @Valid @RequestBody AdminDtos.ActivateSubscriptionRequest request) {
        return adminService.activateSubscription(request);
    }

    @PostMapping("/institutions")
    public InstitutionDtos.InstitutionResponse createInstitution(
            @Valid @RequestBody InstitutionDtos.CreateInstitutionRequest request) {
        return institutionService.create(request);
    }
}
