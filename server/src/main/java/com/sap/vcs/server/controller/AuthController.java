package com.sap.vcs.server.controller;

import com.sap.vcs.server.dto.auth.*;
import com.sap.vcs.server.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthResponseDto register(@Valid @RequestBody RegisterRequestDto request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponseDto login(@Valid @RequestBody LoginRequestDto request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public MeResponseDto me(Authentication authentication) {
        if (authentication == null) {
            throw new RuntimeException("Authentication is required");
        }
        return authService.me(authentication.getName());
    }
}