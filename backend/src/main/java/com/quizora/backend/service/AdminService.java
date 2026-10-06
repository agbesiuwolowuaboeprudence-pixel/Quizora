package com.quizora.backend.service;

import com.quizora.backend.domain.AnswerOption;
import com.quizora.backend.domain.Course;
import com.quizora.backend.domain.Level;
import com.quizora.backend.domain.PlanType;
import com.quizora.backend.domain.Question;
import com.quizora.backend.domain.QuestionSection;
import com.quizora.backend.domain.Role;
import com.quizora.backend.domain.User;
import com.quizora.backend.dto.AdminDtos;
import com.quizora.backend.dto.CourseDtos;
import com.quizora.backend.exception.ConflictException;
import com.quizora.backend.exception.NotFoundException;
import com.quizora.backend.repository.CourseRepository;
import com.quizora.backend.repository.InstitutionRepository;
import com.quizora.backend.repository.QuestionRepository;
import com.quizora.backend.repository.QuizAttemptRepository;
import com.quizora.backend.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    private final CourseRepository courseRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final InstitutionRepository institutionRepository;
    private final QuizAttemptRepository attemptRepository;
    private final SubscriptionService subscriptionService;
    private final CourseService courseService;

    public AdminService(CourseRepository courseRepository,
                        QuestionRepository questionRepository,
                        UserRepository userRepository,
                        InstitutionRepository institutionRepository,
                        QuizAttemptRepository attemptRepository,
                        SubscriptionService subscriptionService,
                        CourseService courseService) {
        this.courseRepository = courseRepository;
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
        this.institutionRepository = institutionRepository;
        this.attemptRepository = attemptRepository;
        this.subscriptionService = subscriptionService;
        this.courseService = courseService;
    }

    public AdminDtos.AdminStatsResponse stats() {
        return new AdminDtos.AdminStatsResponse(
                userRepository.count(),
                userRepository.countByRole(Role.STUDENT),
                userRepository.countByRole(Role.INSTITUTION_ADMIN),
                institutionRepository.count(),
                courseRepository.count(),
                questionRepository.count(),
                attemptRepository.count());
    }

    @Transactional
    public CourseDtos.CourseResponse createCourse(AdminDtos.CreateCourseRequest request) {
        if (courseRepository.existsByCodeIgnoreCase(request.code().trim())) {
            throw new ConflictException("A course with this code already exists");
        }
        Level level = request.level() != null ? request.level() : Level.JHS;
        Course course = courseRepository.save(new Course(
                request.name().trim(), request.code().trim().toUpperCase(),
                request.description(), level));
        return new CourseDtos.CourseResponse(course.getId(), course.getName(), course.getCode(),
                course.getDescription(), course.getLevel(), 0, List.of());
    }

    @Transactional
    public AdminDtos.AdminQuestionResponse createQuestion(AdminDtos.QuestionRequest request) {
        Course course = courseService.requireCourse(request.courseId());
        Question question = questionRepository.save(build(course, request.year(),
                request.section(), request.questionText(), request.optionA(), request.optionB(),
                request.optionC(), request.optionD(), request.correctOption(),
                request.explanation(), request.marks()));
        return toQuestionResponse(question);
    }

    @Transactional
    public AdminDtos.MessageResponse createQuestionsBulk(AdminDtos.BulkQuestionRequest request) {
        Course course = courseService.requireCourse(request.courseId());
        QuestionSection section = request.section() != null ? request.section() : QuestionSection.OBJECTIVE;
        List<Question> questions = request.questions().stream()
                .map(item -> build(course, request.year(), section, item.questionText(),
                        item.optionA(), item.optionB(), item.optionC(), item.optionD(),
                        item.correctOption(), item.explanation(), item.marks()))
                .toList();
        questionRepository.saveAll(questions);
        return new AdminDtos.MessageResponse(
                "Imported " + questions.size() + " questions for " + course.getName() + " " + request.year());
    }

    /** Question bank listing - admins can see correct answers. */
    @Transactional(readOnly = true)
    public List<AdminDtos.AdminQuestionResponse> listQuestions(Optional<Long> courseId, Optional<Integer> year) {
        List<Question> questions;
        if (courseId.isPresent() && year.isPresent()) {
            questions = questionRepository.findByCourseIdAndYearOrderByIdAsc(courseId.get(), year.get());
        } else if (courseId.isPresent()) {
            questions = questionRepository.findByCourseIdOrderByIdAsc(courseId.get());
        } else if (year.isPresent()) {
            questions = questionRepository.findByYearOrderByIdAsc(year.get());
        } else {
            questions = questionRepository.findAll(Sort.by("id"));
        }
        return questions.stream().map(this::toQuestionResponse).toList();
    }

    @Transactional
    public void deleteQuestion(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new NotFoundException("Question not found"));
        questionRepository.delete(question);
    }

    @Transactional(readOnly = true)
    public List<AdminDtos.UserResponse> listUsers(Optional<Role> role) {
        List<User> users = role.isPresent()
                ? userRepository.findByRoleOrderByCreatedAtDesc(role.get())
                : userRepository.findAll(Sort.by("createdAt").descending());
        return users.stream().map(this::toUserResponse).toList();
    }

    @Transactional
    public AdminDtos.UserResponse activateSubscription(AdminDtos.ActivateSubscriptionRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new NotFoundException("User not found"));
        PlanType plan;
        try {
            plan = PlanType.valueOf(request.plan().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ConflictException("Plan must be MONTHLY or YEARLY");
        }
        subscriptionService.activate(user, plan, request.days(), "ADMIN-MANUAL");
        return toUserResponse(user);
    }

    // ------------------------------------------------------------------ internals

    private Question build(Course course, int year, QuestionSection section, String text,
                           String a, String b, String c, String d,
                           AnswerOption correct, String explanation, Integer marks) {
        return new Question(course, year,
                section != null ? section : QuestionSection.OBJECTIVE,
                text.trim(), a, b, c, d, correct, explanation,
                marks != null ? marks : 1);
    }

    private AdminDtos.AdminQuestionResponse toQuestionResponse(Question q) {
        return new AdminDtos.AdminQuestionResponse(
                q.getId(), q.getCourse().getId(), q.getCourse().getName(), q.getYear(),
                q.getSection(), q.getQuestionText(), q.getOptionA(), q.getOptionB(),
                q.getOptionC(), q.getOptionD(), q.getCorrectOption(), q.getExplanation(), q.getMarks());
    }

    private AdminDtos.UserResponse toUserResponse(User user) {
        var subscription = subscriptionService.findByUser(user.getId());
        return new AdminDtos.UserResponse(
                user.getId(), user.getFullName(), user.getEmail(), user.getRole(),
                user.getInstitution() != null ? user.getInstitution().getId() : null,
                user.getInstitution() != null ? user.getInstitution().getName() : null,
                user.getCreatedAt(),
                subscription.map(s -> s.getStatus().name()).orElse("NONE"),
                subscription.map(s -> s.getEndDate().atStartOfDay()).orElse(null));
    }
}
