package com.quizora.backend.exception;

public class AttemptExpiredException extends BadRequestException {
    public AttemptExpiredException(String message) {
        super(message);
    }
}
