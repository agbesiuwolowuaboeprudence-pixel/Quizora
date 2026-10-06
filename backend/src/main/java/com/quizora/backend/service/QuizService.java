package com.quizora.backend.service;

import com.quizora.backend.domain.AnswerOption;
import com.quizora.backend.domain.AttemptAnswer;
import com.quizora.backend.domain.AttemptStatus;
import com.quizora.backend.domain.Course;
import com.quizora.backend.domain.QuizAttempt;
import com.quizora.backend.domain.Question;
import com.quizora.backend.domain.User;
import com.quizora.backend.dto.Mappers;
import com.quizora.backend.dto.QuizDtos;
import com.quizora.backend.exception.AttemptExpiredException;
import com.quizora.backend.exception.BadRequestException;
import com.quizora.backend.exception.NotFoundException;
import com.quizora.backend.repository.AttemptAnswerRepository;
import com.quizora.backend.repository.QuizAttemptRepository;
import com.quizora.backend.repository.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class QuizService {

    private static final int DEFAULT_SECONDS_PER_QUESTION = 60;

    private final QuizAttemptRepository attemptRepository;
    private final AttemptAnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final CourseService courseService;
    private final SubscriptionService subscriptionService;

    public QuizService(QuizAttemptRepository attemptRepository,
                       AttemptAnswerRepository answerRepository,
                       QuestionRepository questionRepository,
                       CourseService courseService,
                       SubscriptionService subscriptionService) {
        this.attemptRepository = attemptRepository;
        this.answerRepository = answerRepository;
        this.questionRepository = questionRepository;
        this.courseService = courseService;
        this.subscriptionService = subscriptionService;
    }

    /** Starts a new attempt on a course/year paper. Requires an active subscription. */
    @Transactional
    public QuizDtos.StartResponse start(User user, QuizDtos.StartRequest request) {
        subscriptionService.requireActive(user);

        Course course = courseService.requireCourse(request.courseId());
        if (course.isArchived()) {
            throw new BadRequestException("This course is not available");
        }
        List<Question> paper = questionRepository
                .findByCourseIdAndYearOrderByIdAsc(course.getId(), request.year());
        if (paper.isEmpty()) {
            throw new BadRequestException("No questions found for " + course.getName() + " " + request.year());
        }

        boolean timed = Boolean.TRUE.equals(request.timed());
        Integer limit = timed
                ? (request.timeLimitSeconds() != null ? request.timeLimitSeconds()
                    : paper.size() * DEFAULT_SECONDS_PER_QUESTION)
                : null;

        QuizAttempt attempt = new QuizAttempt(user, course, request.year(), timed, limit, paper.size());
        attempt = attemptRepository.save(attempt);

        return toStartResponse(attempt);
    }

    /** Attempt metadata (used to resume an attempt). */
    @Transactional(readOnly = true)
    public QuizDtos.StartResponse getAttempt(Long attemptId, User user) {
        return toStartResponse(requireOwned(attemptId, user));
    }

    /** One question at a time - the correct answer is never revealed before submission. */
    @Transactional(noRollbackFor = AttemptExpiredException.class)
    public QuizDtos.QuestionResponse question(Long attemptId, int index, User user) {
        QuizAttempt attempt = requireOwned(attemptId, user);
        requireInProgress(attempt);

        List<Question> paper = questionRepository
                .findByCourseIdAndYearOrderByIdAsc(attempt.getCourse().getId(), attempt.getYear());
        if (index < 0 || index >= paper.size()) {
            throw new BadRequestException("Question index out of range (0.." + (paper.size() - 1) + ")");
        }
        Question question = paper.get(index);

        AnswerOption myAnswer = answerRepository
                .findByAttemptIdAndQuestionId(attempt.getId(), question.getId())
                .map(AttemptAnswer::getSelectedOption)
                .orElse(null);

        return new QuizDtos.QuestionResponse(
                index, paper.size(), question.getId(), question.getQuestionText(),
                question.getOptionA(), question.getOptionB(), question.getOptionC(), question.getOptionD(),
                myAnswer, remainingSeconds(attempt), attempt.getStatus());
    }

    /** Records (or replaces) the answer for one question. */
    @Transactional(noRollbackFor = AttemptExpiredException.class)
    public QuizDtos.AnswerResponse answer(Long attemptId, QuizDtos.AnswerRequest request, User user) {
        QuizAttempt attempt = requireOwned(attemptId, user);
        requireInProgress(attempt);

        Question question = questionRepository.findById(request.questionId())
                .orElseThrow(() -> new NotFoundException("Question not found"));
        if (question.getCourse().getId().equals(attempt.getCourse().getId())
                && question.getYear() == attempt.getYear()) {
            // ok - question belongs to this paper
        } else {
            throw new BadRequestException("Question does not belong to this attempt");
        }

        boolean correct = question.getCorrectOption() == request.selectedOption();
        int timeSpent = request.timeSpentSeconds() != null ? Math.max(0, request.timeSpentSeconds()) : 0;

        AttemptAnswer attemptAnswer = answerRepository
                .findByAttemptIdAndQuestionId(attempt.getId(), question.getId())
                .orElseGet(() -> new AttemptAnswer(attempt, question, request.selectedOption(), correct, timeSpent));
        attemptAnswer.setSelectedOption(request.selectedOption());
        attemptAnswer.setCorrect(correct);
        attemptAnswer.setTimeSpentSeconds(timeSpent);
        answerRepository.save(attemptAnswer);

        // update attempt's lastActiveAt for institutional "last active" reporting
        attempt.setLastActiveAt(LocalDateTime.now());
        attemptRepository.save(attempt);

        return new QuizDtos.AnswerResponse(question.getId(), request.selectedOption(), true);
    }

    /** Submits and grades the attempt (server-side). Idempotent. */
    @Transactional
    public QuizDtos.ResultResponse submit(Long attemptId, User user) {
        QuizAttempt attempt = requireOwned(attemptId, user);

        if (attempt.getStatus() == AttemptStatus.IN_PROGRESS) {
            boolean timeExpired = attempt.isExpiredNow();
            finalizeAttempt(attempt, timeExpired ? AttemptStatus.EXPIRED : AttemptStatus.COMPLETED);
        }
        return buildResult(attempt);
    }

    /** Graded result with full review (correct answers + explanations). */
    @Transactional
    public QuizDtos.ResultResponse result(Long attemptId, User user) {
        QuizAttempt attempt = requireOwned(attemptId, user);
        if (attempt.getStatus() == AttemptStatus.IN_PROGRESS) {
            throw new BadRequestException("This attempt has not been submitted yet");
        }
        return buildResult(attempt);
    }

    /** Attempt history, newest first. */
    @Transactional(readOnly = true)
    public List<QuizDtos.AttemptSummary> history(User user, int page, int size) {
        return attemptRepository
                .findByUser_IdOrderByStartedAtDesc(user.getId(),
                        org.springframework.data.domain.PageRequest.of(page, Math.min(size, 100)))
                .getContent()
                .stream()
                .map(Mappers::attemptSummary)
                .toList();
    }

    // ------------------------------------------------------------------ internals

    private QuizAttempt requireOwned(Long attemptId, User user) {
        return attemptRepository.findByIdAndUser_Id(attemptId, user.getId())
                .orElseThrow(() -> new NotFoundException("Attempt not found"));
    }

    /** Auto-grades and closes the attempt when the timer ran out, otherwise rejects. */
    private void requireInProgress(QuizAttempt attempt) {
        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new BadRequestException("This attempt has already been submitted");
        }
        if (attempt.isExpiredNow()) {
            finalizeAttempt(attempt, AttemptStatus.EXPIRED);
            throw new AttemptExpiredException(
                    "Time is up - your attempt was submitted automatically. Fetch the result.");
        }
    }

    private void finalizeAttempt(QuizAttempt attempt, AttemptStatus status) {
        List<AttemptAnswer> answers = answerRepository.findByAttemptId(attempt.getId());
        int answered = answers.size();
        int correct = (int) answers.stream().filter(AttemptAnswer::isCorrect).count();

        attempt.setAnsweredCount(answered);
        attempt.setCorrectCount(correct);
        attempt.setScorePercent(attempt.getTotalQuestions() == 0 ? 0
                : (correct * 100.0) / attempt.getTotalQuestions());
        attempt.setStatus(status);
        attempt.setSubmittedAt(LocalDateTime.now());

        long elapsed = Duration.between(attempt.getStartedAt(), attempt.getSubmittedAt()).getSeconds();
        if (attempt.getTimeLimitSeconds() != null) {
            elapsed = Math.min(elapsed, attempt.getTimeLimitSeconds());
        }
        attempt.setDurationSeconds(Math.max(0, elapsed));
        attemptRepository.save(attempt);
    }

    private QuizDtos.ResultResponse buildResult(QuizAttempt attempt) {
        List<Question> paper = questionRepository
                .findByCourseIdAndYearOrderByIdAsc(attempt.getCourse().getId(), attempt.getYear());
        List<AttemptAnswer> answers = answerRepository.findByAttemptId(attempt.getId());

        List<QuizDtos.ReviewItem> review = new ArrayList<>();
        for (int i = 0; i < paper.size(); i++) {
            Question q = paper.get(i);
            AttemptAnswer a = answers.stream()
                    .filter(ans -> ans.getQuestion().getId().equals(q.getId()))
                    .findFirst()
                    .orElse(null);
            review.add(new QuizDtos.ReviewItem(
                    i, q.getId(), q.getQuestionText(),
                    q.getOptionA(), q.getOptionB(), q.getOptionC(), q.getOptionD(),
                    a != null ? a.getSelectedOption() : null,
                    q.getCorrectOption(),
                    a != null && a.isCorrect(),
                    q.getExplanation()));
        }

        return new QuizDtos.ResultResponse(
                attempt.getId(), attempt.getCourse().getId(), attempt.getCourse().getName(),
                attempt.getYear(), attempt.getStatus(), attempt.isTimed(), attempt.getTimeLimitSeconds(),
                attempt.getTotalQuestions(), attempt.getAnsweredCount(), attempt.getCorrectCount(),
                attempt.getScorePercent(), attempt.getDurationSeconds(),
                attempt.getStatus() == AttemptStatus.EXPIRED, review);
    }

    private QuizDtos.StartResponse toStartResponse(QuizAttempt attempt) {
        return new QuizDtos.StartResponse(
                attempt.getId(), attempt.getCourse().getId(), attempt.getCourse().getName(),
                attempt.getYear(), attempt.getTotalQuestions(), attempt.isTimed(),
                attempt.getTimeLimitSeconds(), attempt.getStartedAt(), attempt.expiresAt(),
                attempt.getStatus());
    }

    private Integer remainingSeconds(QuizAttempt attempt) {
        if (!attempt.isTimed() || attempt.expiresAt() == null) return null;
        long remaining = Duration.between(LocalDateTime.now(), attempt.expiresAt()).getSeconds();
        return (int) Math.max(0, remaining);
    }
}
