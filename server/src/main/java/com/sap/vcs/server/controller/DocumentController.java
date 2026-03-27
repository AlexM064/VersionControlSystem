package com.sap.vcs.server.controller;

import com.sap.vcs.server.dto.DocumentHistoryResponseDto;
import com.sap.vcs.server.dto.DocumentRequestDto;
import com.sap.vcs.server.dto.DocumentResponseDto;
import com.sap.vcs.server.dto.DocumentVersionResponseDto;
import com.sap.vcs.server.entity.enums.DocumentStatus;
import com.sap.vcs.server.service.DocumentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping
    public DocumentResponseDto createDocument(@Valid @RequestBody DocumentRequestDto request) {
        return documentService.createDocument(request);
    }

    @GetMapping
    public Page<DocumentResponseDto> getAllDocuments(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) DocumentStatus status,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        return documentService.getAllDocuments(title, status, pageable);
    }

    @GetMapping("/{id}")
    public DocumentResponseDto getDocumentById(@PathVariable Integer id) {
        return documentService.getDocumentById(id);
    }

    @GetMapping("/{id}/history")
    public List<DocumentHistoryResponseDto> getDocumentHistory(@PathVariable Integer id) {
        return documentService.getDocumentHistory(id);
    }

    @GetMapping("/{id}/published-version")
    public DocumentVersionResponseDto getPublishedVersion(@PathVariable Integer id) {
        return documentService.getPublishedVersion(id);
    }
}