package com.sap.vcs.server.controller;

import com.sap.vcs.server.dto.UpdateUserRoleRequestDto;
import com.sap.vcs.server.dto.auth.AuthResponseDto;
import com.sap.vcs.server.dto.auth.MeResponseDto;
import com.sap.vcs.server.dto.auth.RegisterRequestDto;
import com.sap.vcs.server.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "User management endpoints")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @Operation(summary = "Register a new user")
    public AuthResponseDto register(@Valid @RequestBody RegisterRequestDto request) {
        return userService.register(request);
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Return the currently authenticated user")
    public MeResponseDto me(Authentication authentication) {
        if (authentication == null) {
            throw new RuntimeException("Authentication is required");
        }
        return userService.me(authentication.getName());
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update user role (ADMIN only)")
    public ResponseEntity<Void> updateUserRole(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateUserRoleRequestDto request
    ) {
        userService.updateUserRole(id, request.getRole());
        return ResponseEntity.noContent().build();
    }
}