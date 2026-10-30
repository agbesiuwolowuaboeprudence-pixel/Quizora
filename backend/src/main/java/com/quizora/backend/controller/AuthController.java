package com.quizora.backend.controller;

import com.quizora.backend.dto.AuthDtos;
import com.quizora.backend.security.CurrentUser;
import com.quizora.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final CurrentUser currentUser;

    public AuthController(AuthService authService, CurrentUser currentUser) {
        this.authService = authService;
        this.currentUser = currentUser;
    }

    /** Individual student registration (grants a free trial). */
    @PostMapping("/register")
    public AuthDtos.AuthResponse register(@Valid @RequestBody AuthDtos.RegisterRequest request) {
        return authService.register(request);
    }

    /** Student registration using an institution's bulk licence code. */
    @PostMapping("/register-institutional")
    public AuthDtos.AuthResponse registerInstitutional(
            @Valid @RequestBody AuthDtos.InstitutionalRegisterRequest request) {
        return authService.registerInstitutional(request);
    }

    @PostMapping("/login")
    public AuthDtos.AuthResponse login(@Valid @RequestBody AuthDtos.LoginRequest request) {
        return authService.login(request);
    }

    /** Link the current student account to an institution via a licence code. */
    @PostMapping("/join-code")
    public AuthDtos.UserDto joinCode(@Valid @RequestBody AuthDtos.JoinCodeRequest request) {
        return authService.joinCode(currentUser.get(), request);
    }

    /** Current profile + subscription. */
    @GetMapping("/me")
    public AuthDtos.UserDto me() {
        return authService.toUserDto(currentUser.get());
    }
}
