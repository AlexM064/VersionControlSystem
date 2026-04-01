package com.sap.vcs.server.controller;

import com.sap.vcs.server.dto.auth.AuthResponseDto;
import com.sap.vcs.server.dto.auth.LoginRequestDto;
import com.sap.vcs.server.dto.auth.MeResponseDto;
import com.sap.vcs.server.dto.auth.RegisterRequestDto;
import com.sap.vcs.server.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Authentication and user identity endpoints")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public AuthResponseDto register(@Valid @RequestBody RegisterRequestDto request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and return JWT")
    public AuthResponseDto login(@Valid @RequestBody LoginRequestDto request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    @Operation(summary = "Return the currently authenticated user")
    public MeResponseDto me(Authentication authentication) {
        if (authentication == null) {
            throw new RuntimeException("Authentication is required");
        }
        return authService.me(authentication.getName());
    }
}