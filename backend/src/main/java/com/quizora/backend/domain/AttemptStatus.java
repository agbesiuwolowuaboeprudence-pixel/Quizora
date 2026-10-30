package com.quizora.backend.domain;

public enum AttemptStatus {
    IN_PROGRESS,
    COMPLETED,
    EXPIRED      // timed attempt whose clock ran out - graded on answered questions
}
