package com.sap.vcs.server.controller;

import com.sap.vcs.server.dto.ApprovalResponseDto;
import com.sap.vcs.server.service.ApprovalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/versions")
@Tag(name = "Approvals", description = "Version approval workflow endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ApprovalController {

    private final ApprovalService approvalService;

    public ApprovalController(ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve a version")
    public ResponseEntity<ApprovalResponseDto> approve(@PathVariable Integer id) {
        return ResponseEntity.ok(approvalService.approve(id));
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject a version")
    public ResponseEntity<ApprovalResponseDto> reject(@PathVariable Integer id) {
        return ResponseEntity.ok(approvalService.reject(id));
    }
}