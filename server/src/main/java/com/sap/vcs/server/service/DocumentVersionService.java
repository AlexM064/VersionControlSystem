package com.sap.vcs.server.service;

import com.sap.vcs.server.dto.DocumentVersionRequestDto;
import com.sap.vcs.server.dto.DocumentVersionResponseDto;
import com.sap.vcs.server.entity.Document;
import com.sap.vcs.server.entity.DocumentVersion;
import com.sap.vcs.server.exception.ResourceNotFoundException;
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

    public DocumentVersionResponseDto createVersion(Integer documentId, DocumentVersionRequestDto request) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Document not found with id: " + documentId));

        Integer versionNumber = versionRepository.countByDocument(document) + 1;

        DocumentVersion version = new DocumentVersion();
        version.setDocument(document);
        version.setVersionNumber(versionNumber);
        version.setContent(request.getContent());
        version.setMessage(request.getMessage());

        DocumentVersion savedVersion = versionRepository.save(version);
        return mapToResponse(savedVersion);
    }

    public List<DocumentVersionResponseDto> getVersions(Integer documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Document not found with id: " + documentId));

        return versionRepository.findByDocument(document)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private DocumentVersionResponseDto mapToResponse(DocumentVersion version) {
        return new DocumentVersionResponseDto(
                version.getId(),
                version.getDocument().getId(),
                version.getVersionNumber(),
                version.getContent(),
                version.getMessage(),
                version.getCreatedAt()
        );
    }
}