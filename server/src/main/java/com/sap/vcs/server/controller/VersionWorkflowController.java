package com.sap.vcs.server.controller;

import com.sap.vcs.server.dto.PublishDocumentResponseDto;
import com.sap.vcs.server.service.DocumentVersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/versions")
@Tag(name = "Version Workflow", description = "Version publishing and rollback endpoints")
@SecurityRequirement(name = "bearerAuth")
public class VersionWorkflowController {

    private final DocumentVersionService documentVersionService;

    public VersionWorkflowController(DocumentVersionService documentVersionService) {
        this.documentVersionService = documentVersionService;
    }

    @PostMapping("/{versionId}/publish")
    @Operation(summary = "Publish an approved version")
    public ResponseEntity<PublishDocumentResponseDto> publishVersion(@PathVariable Integer versionId) {
        return ResponseEntity.ok(documentVersionService.publishVersion(versionId));
    }

    @PostMapping("/{versionId}/rollback")
    @Operation(summary = "Rollback document to a previous version")
    public ResponseEntity<PublishDocumentResponseDto> rollbackVersion(@PathVariable Integer versionId) {
        return ResponseEntity.ok(documentVersionService.rollbackVersion(versionId));
    }
}