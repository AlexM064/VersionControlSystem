package com.sap.vcs.server.controller;

import com.sap.vcs.server.dto.PublishDocumentResponseDto;
import com.sap.vcs.server.service.DocumentVersionService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/versions")
public class VersionWorkflowController {

    private final DocumentVersionService documentVersionService;

    public VersionWorkflowController(DocumentVersionService documentVersionService) {
        this.documentVersionService = documentVersionService;
    }

    @PostMapping("/{versionId}/publish")
    public PublishDocumentResponseDto publishVersion(@PathVariable Integer versionId) {
        return documentVersionService.publishVersion(versionId);
    }
}