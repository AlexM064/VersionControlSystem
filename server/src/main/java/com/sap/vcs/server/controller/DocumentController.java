package com.sap.vcs.server.controller;

import com.sap.vcs.server.entity.Document;
import com.sap.vcs.server.service.DocumentService;
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
    public Document createDocument(@RequestBody Document document) {
        return documentService.createDocument(document);
    }

    @GetMapping
    public List<Document> getAllDocuments() {
        return documentService.getAllDocuments();
    }

    @GetMapping("/{id}")
    public Document getDocumentById(@PathVariable Integer id) {
        return documentService.getDocumentById(id);
    }
}