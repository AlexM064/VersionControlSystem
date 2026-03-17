package com.sap.vcs.server.service;

import com.sap.vcs.server.dto.DocumentVersionRequestDto;
import com.sap.vcs.server.dto.DocumentVersionResponseDto;
import com.sap.vcs.server.dto.PublishDocumentResponseDto;
import com.sap.vcs.server.entity.Document;
import com.sap.vcs.server.entity.DocumentVersion;
import com.sap.vcs.server.entity.enums.DocumentStatus;
import com.sap.vcs.server.exception.ResourceNotFoundException;
import com.sap.vcs.server.repository.DocumentRepository;
import com.sap.vcs.server.repository.DocumentVersionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentVersionService {

    private final DocumentVersionRepository versionRepository;
    private final DocumentRepository documentRepository;

    public DocumentVersionService(
            DocumentVersionRepository versionRepository,
            DocumentRepository documentRepository
    ) {
        this.versionRepository = versionRepository;
        this.documentRepository = documentRepository;
    }

    public DocumentVersionResponseDto createVersion(Integer documentId, DocumentVersionRequestDto request) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Document not found with id: " + documentId));

        Integer nextVersionNumber = versionRepository
                .findTopByDocumentOrderByVersionNumberDesc(document)
                .map(lastVersion -> lastVersion.getVersionNumber() + 1)
                .orElse(1);

        DocumentVersion version = new DocumentVersion();
        version.setDocument(document);
        version.setVersionNumber(nextVersionNumber);
        version.setContent(request.getContent());
        version.setMessage(request.getMessage());

        DocumentVersion savedVersion = versionRepository.save(version);
        return mapToResponse(savedVersion);
    }

    public List<DocumentVersionResponseDto> getVersions(Integer documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Document not found with id: " + documentId));

        return versionRepository.findByDocumentOrderByVersionNumberAsc(document)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public PublishDocumentResponseDto publishVersion(Integer versionId) {
        DocumentVersion version = versionRepository.findById(versionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Version not found with id: " + versionId));

        return applyPublishedVersion(version);
    }

    @Transactional
    public PublishDocumentResponseDto rollbackVersion(Integer versionId) {
        DocumentVersion version = versionRepository.findById(versionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Version not found with id: " + versionId));

        return applyPublishedVersion(version);
    }

    private PublishDocumentResponseDto applyPublishedVersion(DocumentVersion version) {
        Document document = version.getDocument();
        document.setPublishedVersion(version);
        document.setStatus(DocumentStatus.PUBLISHED);

        Document savedDocument = documentRepository.save(document);

        return new PublishDocumentResponseDto(
                savedDocument.getId(),
                savedDocument.getTitle(),
                savedDocument.getStatus() != null ? savedDocument.getStatus().name() : null,
                savedDocument.getPublishedVersion() != null ? savedDocument.getPublishedVersion().getId() : null,
                savedDocument.getPublishedVersion() != null
                        ? savedDocument.getPublishedVersion().getVersionNumber()
                        : null
        );
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