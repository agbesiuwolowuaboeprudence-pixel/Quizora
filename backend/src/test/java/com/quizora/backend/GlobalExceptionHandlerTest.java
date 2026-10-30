package com.quizora.backend;

import com.quizora.backend.exception.GlobalExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    @Test
    void unexpectedErrorsDoNotExposeInternalMessages() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        HttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");

        ResponseEntity<GlobalExceptionHandler.ErrorBody> response =
                handler.handleOther(new IllegalStateException("database password leaked"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().message()).isEqualTo("An unexpected error occurred");
    }
}
