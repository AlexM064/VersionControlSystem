package com.sap.vcs.server.service;

import com.sap.vcs.server.entity.Document;
import com.sap.vcs.server.entity.DocumentVersion;
import com.sap.vcs.server.repository.DocumentRepository;
import com.sap.vcs.server.repository.DocumentVersionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentVersionService {

    private final DocumentVersionRepository versionRepository;
    private final DocumentRepository documentRepository;

    public DocumentVersionService(
            DocumentVersionRepository versionRepository,
            DocumentRepository documentRepository) {
        this.versionRepository = versionRepository;
        this.documentRepository = documentRepository;
    }

    public DocumentVersion createVersion(Integer documentId, DocumentVersion version) {

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        Integer versionNumber = versionRepository.countByDocument(document) + 1;

        version.setVersionNumber(versionNumber);
        version.setDocument(document);

        return versionRepository.save(version);
    }

    public List<DocumentVersion> getVersions(Integer documentId) {

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        return versionRepository.findByDocument(document);

    }
}