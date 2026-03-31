package com.sap.vcs.server.controller;

import com.sap.vcs.server.dto.DocumentHistoryResponseDto;
import com.sap.vcs.server.dto.DocumentRequestDto;
import com.sap.vcs.server.dto.DocumentResponseDto;
import com.sap.vcs.server.dto.DocumentVersionResponseDto;
import com.sap.vcs.server.dto.UpdateDocumentMetadataRequestDto;
import com.sap.vcs.server.dto.*;
import com.sap.vcs.server.entity.enums.DocumentStatus;
import com.sap.vcs.server.service.DocumentService;
import com.sap.vcs.server.service.DocumentVersionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.sap.vcs.server.dto.CompareVersionsResponseDto;

import java.util.List;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentVersionService documentVersionService;

    public DocumentController(DocumentService documentService, DocumentVersionService documentVersionService) {
        this.documentService = documentService;
        this.documentVersionService = documentVersionService;
    }

    @PostMapping
    public ResponseEntity<DocumentResponseDto> createDocument(@Valid @RequestBody DocumentRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentService.createDocument(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DocumentResponseDto> updateDocument(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateDocumentMetadataRequestDto request
    ) {
        return ResponseEntity.ok(documentService.updateDocument(id, request));
    }

    @PatchMapping("/{id}/archive")
    @PreAuthorize("hasAnyRole('AUTHOR', 'ADMIN')")
    public ResponseEntity<Void> archiveDocument(@PathVariable Integer id) {
        documentService.archiveDocument(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/compare")
    @PreAuthorize("hasAnyRole('AUTHOR', 'REVIEWER', 'ADMIN')")
    public ResponseEntity<CompareVersionsResponseDto> compareVersions(
            @RequestParam Integer leftVersionId,
            @RequestParam Integer rightVersionId) {

        CompareVersionsResponseDto response =
                documentVersionService.compareVersions(leftVersionId, rightVersionId);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<DocumentResponseDto>> getAllDocuments(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) DocumentStatus status,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        return ResponseEntity.ok(documentService.getAllDocuments(title, status, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponseDto> getDocumentById(@PathVariable Integer id) {
        return ResponseEntity.ok(documentService.getDocumentById(id));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<DocumentHistoryResponseDto>> getDocumentHistory(@PathVariable Integer id) {
        return ResponseEntity.ok(documentService.getDocumentHistory(id));
    }

    @GetMapping("/{id}/published-version")
    public ResponseEntity<DocumentVersionResponseDto> getPublishedVersion(@PathVariable Integer id) {
        return ResponseEntity.ok(documentService.getPublishedVersion(id));
    }
}