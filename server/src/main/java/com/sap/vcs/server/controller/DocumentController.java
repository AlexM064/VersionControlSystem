package com.sap.vcs.server.controller;

import com.sap.vcs.server.dto.DocumentRequestDto;
import com.sap.vcs.server.dto.DocumentResponseDto;
import com.sap.vcs.server.service.DocumentService;
import jakarta.validation.Valid;
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
    public List<DocumentResponseDto> getAllDocuments() {
        return documentService.getAllDocuments();
    }

    @GetMapping("/{id}")
    public DocumentResponseDto getDocumentById(@PathVariable Integer id) {
        return documentService.getDocumentById(id);
    }
}