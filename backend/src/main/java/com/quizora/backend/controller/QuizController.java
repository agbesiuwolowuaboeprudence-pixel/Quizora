package com.quizora.backend.controller;

import com.quizora.backend.dto.QuizDtos;
import com.quizora.backend.security.CurrentUser;
import com.quizora.backend.service.QuizService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
public class QuizController {

    private final QuizService quizService;
    private final CurrentUser currentUser;

    public QuizController(QuizService quizService, CurrentUser currentUser) {
        this.quizService = quizService;
        this.currentUser = currentUser;
    }

    /** Start an attempt (optionally timed). */
    @PostMapping("/start")
    public QuizDtos.StartResponse start(@Valid @RequestBody QuizDtos.StartRequest request) {
        return quizService.start(currentUser.get(), request);
    }

    /** My attempt history, newest first. */
    @GetMapping
    public List<QuizDtos.AttemptSummary> history(@RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        return quizService.history(currentUser.get(), page, size);
    }

    /** Attempt metadata - use to resume. */
    @GetMapping("/{id}")
    public QuizDtos.StartResponse get(@PathVariable Long id) {
        return quizService.getAttempt(id, currentUser.get());
    }

    /** One question at a time (correct answer never revealed before submission). */
    @GetMapping("/{id}/questions/{index}")
    public QuizDtos.QuestionResponse question(@PathVariable Long id, @PathVariable int index) {
        return quizService.question(id, index, currentUser.get());
    }

    @PostMapping("/{id}/answers")
    public QuizDtos.AnswerResponse answer(@PathVariable Long id,
                                          @Valid @RequestBody QuizDtos.AnswerRequest request) {
        return quizService.answer(id, request, currentUser.get());
    }

    /** Submit for grading (server-side, idempotent). */
    @PostMapping("/{id}/submit")
    public QuizDtos.ResultResponse submit(@PathVariable Long id) {
        return quizService.submit(id, currentUser.get());
    }

    /** Graded result with full review. */
    @GetMapping("/{id}/result")
    public QuizDtos.ResultResponse result(@PathVariable Long id) {
        return quizService.result(id, currentUser.get());
    }
}
