package com.sap.vcs.server.controller;

import com.sap.vcs.server.entity.DocumentVersion;
import com.sap.vcs.server.service.DocumentVersionService;
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
    public DocumentVersion createVersion(
            @PathVariable Integer documentId,
            @RequestBody DocumentVersion version) {

        return versionService.createVersion(documentId, version);
    }

    @GetMapping
    public List<DocumentVersion> getVersions(@PathVariable Integer documentId) {
        return versionService.getVersions(documentId);
    }
}