package com.sap.vcs.server.controller;

import com.sap.vcs.server.dto.DocumentVersionRequestDto;
import com.sap.vcs.server.dto.DocumentVersionResponseDto;
import com.sap.vcs.server.service.DocumentVersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/documents/{documentId}/versions")
@Tag(name = "Document Versions", description = "Version creation and review submission endpoints")
@SecurityRequirement(name = "bearerAuth")
public class DocumentVersionController {

    private final DocumentVersionService documentVersionService;

    public DocumentVersionController(DocumentVersionService documentVersionService) {
        this.documentVersionService = documentVersionService;
    }

    @PostMapping
    @Operation(summary = "Create a new version for a document")
    public ResponseEntity<DocumentVersionResponseDto> createVersion(
            @PathVariable Integer documentId,
            @Valid @RequestBody DocumentVersionRequestDto request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentVersionService.createVersion(documentId, request));
    }

    @GetMapping
    @Operation(summary = "Get all versions for a document")
    public ResponseEntity<List<DocumentVersionResponseDto>> getVersions(@PathVariable Integer documentId) {
        return ResponseEntity.ok(documentVersionService.getVersions(documentId));
    }

    @PostMapping("/{versionId}/submit")
    @PreAuthorize("hasAnyRole('AUTHOR', 'ADMIN')")
    @Operation(summary = "Submit a version for review")
    public DocumentVersionResponseDto submitForReview(
            @PathVariable Integer documentId,
            @PathVariable Integer versionId
    ) {
        return documentVersionService.submitForReview(versionId);
    }
}