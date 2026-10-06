package com.quizora.backend.exception;

import org.springframework.http.HttpStatus;

/** Base class for errors that map to an HTTP status. */
public abstract class ApiException extends RuntimeException {

    private final HttpStatus status;

    protected ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
