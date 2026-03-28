package com.sap.vcs.server.controller;

import com.sap.vcs.server.dto.ApprovalResponseDto;
import com.sap.vcs.server.service.ApprovalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/versions")
public class ApprovalController {

    private final ApprovalService approvalService;

    public ApprovalController(ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApprovalResponseDto> approve(
            @PathVariable Integer id,
            @RequestParam Integer reviewerId) {
        return ResponseEntity.ok(approvalService.approve(id, reviewerId));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ApprovalResponseDto> reject(
            @PathVariable Integer id,
            @RequestParam Integer reviewerId) {
        return ResponseEntity.ok(approvalService.reject(id, reviewerId));
    }
}
