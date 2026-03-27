package com.sap.vcs.server.controller;

import com.sap.vcs.server.dto.DocumentVersionRequestDto;
import com.sap.vcs.server.dto.DocumentVersionResponseDto;
import com.sap.vcs.server.service.DocumentVersionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/documents/{documentId}/versions")
public class DocumentVersionController {

    private final DocumentVersionService versionService;

    public DocumentVersionController(DocumentVersionService versionService) {
        this.versionService = versionService;
    }

    @PostMapping
    public ResponseEntity<DocumentVersionResponseDto> createVersion(
            @PathVariable Integer documentId,
            @Valid @RequestBody DocumentVersionRequestDto request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(versionService.createVersion(documentId, request));
    }

    @GetMapping
    public ResponseEntity<List<DocumentVersionResponseDto>> getVersions(@PathVariable Integer documentId) {
        return ResponseEntity.ok(versionService.getVersions(documentId));
    }
}
