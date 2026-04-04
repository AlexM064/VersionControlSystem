package com.sap.vcs.server.controller;

import com.sap.vcs.server.dto.UpdateUserRoleRequestDto;
import com.sap.vcs.server.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@Tag(name = "Users (Legacy)", description = "Legacy user management endpoints")
public class LegacyUserController {

    private final UserService userService;

    public LegacyUserController(UserService userService) {
        this.userService = userService;
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update user role (legacy endpoint, ADMIN only)")
    public ResponseEntity<Void> updateUserRole(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateUserRoleRequestDto request
    ) {
        userService.updateUserRole(id, request.getRole());
        return ResponseEntity.noContent().build();
    }
}