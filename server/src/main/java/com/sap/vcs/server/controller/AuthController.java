package com.sap.vcs.server.controller;

import com.sap.vcs.server.dto.auth.AuthResponseDto;
import com.sap.vcs.server.dto.auth.LoginRequestDto;
import com.sap.vcs.server.dto.auth.MeResponseDto;
import com.sap.vcs.server.dto.auth.RegisterRequestDto;
import com.sap.vcs.server.service.AuthService;
import com.sap.vcs.server.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Authentication and legacy user identity endpoints")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user (legacy endpoint)")
    public AuthResponseDto register(@Valid @RequestBody RegisterRequestDto request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and return JWT")
    public AuthResponseDto login(@Valid @RequestBody LoginRequestDto request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    @Operation(summary = "Return the currently authenticated user (legacy endpoint)")
    public MeResponseDto me(Authentication authentication) {
        if (authentication == null) {
            throw new RuntimeException("Authentication is required");
        }
        return userService.me(authentication.getName());
    }
}