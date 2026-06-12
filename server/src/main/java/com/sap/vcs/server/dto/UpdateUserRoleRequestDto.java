package com.sap.vcs.server.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateUserRoleRequestDto {

    @NotBlank(message = "Role is required")
    private String role;

    public UpdateUserRoleRequestDto() {
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}