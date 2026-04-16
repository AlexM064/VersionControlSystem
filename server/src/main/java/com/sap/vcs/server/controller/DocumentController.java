package com.sap.vcs.server.controller;

import com.sap.vcs.server.dto.*;
import com.sap.vcs.server.entity.enums.DocumentStatus;
import com.sap.vcs.server.service.DocumentService;
import com.sap.vcs.server.service.DocumentVersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/documents", "/api/v1/documents"})
@Tag(name = "Documents", description = "Document management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentVersionService documentVersionService;

    public DocumentController(DocumentService documentService, DocumentVersionService documentVersionService) {
        this.documentService = documentService;
        this.documentVersionService = documentVersionService;
    }

    @PostMapping
    @Operation(summary = "Create a new document")
    public ResponseEntity<DocumentResponseDto> createDocument(@Valid @RequestBody DocumentRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentService.createDocument(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update document metadata")
    public ResponseEntity<DocumentResponseDto> updateDocument(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateDocumentMetadataRequestDto request
    ) {
        return ResponseEntity.ok(documentService.updateDocument(id, request));
    }

    @PatchMapping("/{id}/archive")
    @PreAuthorize("hasAnyRole('AUTHOR', 'ADMIN')")
    @Operation(summary = "Archive a document")
    public ResponseEntity<Void> archiveDocument(@PathVariable Integer id) {
        documentService.archiveDocument(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Permanently delete a document")
    public ResponseEntity<Void> deleteDocument(@PathVariable Integer id) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/compare")
    @PreAuthorize("hasAnyRole('AUTHOR', 'REVIEWER', 'ADMIN')")
    @Operation(summary = "Compare two document versions")
    public ResponseEntity<CompareVersionsResponseDto> compareVersions(
            @RequestParam Integer leftVersionId,
            @RequestParam Integer rightVersionId
    ) {
        CompareVersionsResponseDto response =
                documentVersionService.compareVersions(leftVersionId, rightVersionId);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all documents with optional filters and pagination")
    public ResponseEntity<Page<DocumentResponseDto>> getAllDocuments(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) DocumentStatus status,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        return ResponseEntity.ok(documentService.getAllDocuments(title, status, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get document by id")
    public ResponseEntity<DocumentResponseDto> getDocumentById(@PathVariable Integer id) {
        return ResponseEntity.ok(documentService.getDocumentById(id));
    }

    @GetMapping("/{id}/history")
    @Operation(summary = "Get full version history for a document")
    public ResponseEntity<List<DocumentHistoryResponseDto>> getDocumentHistory(@PathVariable Integer id) {
        return ResponseEntity.ok(documentService.getDocumentHistory(id));
    }

    @GetMapping("/{id}/published-version")
    @Operation(summary = "Get the currently published version of a document")
    public ResponseEntity<DocumentVersionResponseDto> getPublishedVersion(@PathVariable Integer id) {
        return ResponseEntity.ok(documentService.getPublishedVersion(id));
    }
    @GetMapping("/published")
    public ResponseEntity<List<DocumentResponseDto>> getPublishedDocuments() {
        return ResponseEntity.ok(documentService.getPublishedDocuments());
    }
    @GetMapping("/{id}/published-only")
    public ResponseEntity<DocumentResponseDto> getPublishedOnly(@PathVariable Integer id) {
        return ResponseEntity.ok(documentService.getPublishedOnly(id));
    }
}