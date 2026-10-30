package com.quizora.backend.controller;

import com.quizora.backend.dto.ProgressDtos;
import com.quizora.backend.dto.QuizDtos;
import com.quizora.backend.security.CurrentUser;
import com.quizora.backend.service.ProgressService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/progress")
public class ProgressController {

    private final ProgressService progressService;
    private final CurrentUser currentUser;

    public ProgressController(ProgressService progressService, CurrentUser currentUser) {
        this.progressService = progressService;
        this.currentUser = currentUser;
    }

    /** Progress page: totals, accuracy, per-course breakdown, performance trend. */
    @GetMapping
    public ProgressDtos.ProgressResponse progress() {
        return progressService.progress(currentUser.get());
    }

    /** Full attempt history, paginated. */
    @GetMapping("/attempts")
    public List<QuizDtos.AttemptSummary> attempts(@RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "20") int size) {
        return progressService.attemptHistory(currentUser.get(), page, size);
    }
}
