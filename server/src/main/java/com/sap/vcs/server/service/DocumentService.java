package com.sap.vcs.server.service;

import com.sap.vcs.server.dto.DocumentHistoryResponseDto;
import com.sap.vcs.server.dto.DocumentRequestDto;
import com.sap.vcs.server.dto.DocumentResponseDto;
import com.sap.vcs.server.dto.DocumentVersionResponseDto;
import com.sap.vcs.server.entity.Document;
import com.sap.vcs.server.entity.DocumentVersion;
import com.sap.vcs.server.entity.enums.DocumentStatus;
import com.sap.vcs.server.exception.ResourceNotFoundException;
import com.sap.vcs.server.repository.DocumentRepository;
import com.sap.vcs.server.repository.DocumentVersionRepository;
import com.sap.vcs.server.specification.DocumentSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository documentVersionRepository;

    public DocumentService(DocumentRepository documentRepository,
                           DocumentVersionRepository documentVersionRepository) {
        this.documentRepository = documentRepository;
        this.documentVersionRepository = documentVersionRepository;
    }

    public DocumentResponseDto createDocument(DocumentRequestDto request) {
        Document document = new Document();
        document.setTitle(request.getTitle());
        document.setDescription(request.getDescription());

        Document savedDocument = documentRepository.save(document);
        return mapToResponse(savedDocument);
    }

    public Page<DocumentResponseDto> getAllDocuments(
            String title,
            DocumentStatus status,
            Pageable pageable
    ) {
        Specification<Document> spec = null;

        if (title != null && !title.isBlank()) {
            spec = DocumentSpecification.titleContains(title);
        }

        if (status != null) {
            Specification<Document> statusSpec = DocumentSpecification.hasStatus(status);
            spec = (spec == null) ? statusSpec : spec.and(statusSpec);
        }

        return documentRepository.findAll(spec, pageable)
                .map(this::mapToResponse);
    }

    public DocumentResponseDto getDocumentById(Integer id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Document not found with id: " + id));

        return mapToResponse(document);
    }

    public List<DocumentHistoryResponseDto> getDocumentHistory(Integer id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Document not found with id: " + id));

        Integer publishedVersionId = document.getPublishedVersion() != null
                ? document.getPublishedVersion().getId()
                : null;

        return documentVersionRepository.findByDocumentOrderByVersionNumberAsc(document)
                .stream()
                .map(version -> mapToHistoryResponse(version, publishedVersionId))
                .toList();
    }

    public DocumentVersionResponseDto getPublishedVersion(Integer id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Document not found with id: " + id));

        DocumentVersion publishedVersion = document.getPublishedVersion();

        if (publishedVersion == null) {
            throw new ResourceNotFoundException("No published version found for document id: " + id);
        }

        return mapToVersionResponse(publishedVersion);
    }

    private DocumentHistoryResponseDto mapToHistoryResponse(DocumentVersion version, Integer publishedVersionId) {
        return new DocumentHistoryResponseDto(
                version.getId(),
                version.getVersionNumber(),
                version.getMessage(),
                version.getCreatedAt(),
                publishedVersionId != null && publishedVersionId.equals(version.getId())
        );
    }

    private DocumentVersionResponseDto mapToVersionResponse(DocumentVersion version) {
        return new DocumentVersionResponseDto(
                version.getId(),
                version.getDocument().getId(),
                version.getVersionNumber(),
                version.getContent(),
                version.getMessage(),
                version.getCreatedAt()
        );
    }

    private DocumentResponseDto mapToResponse(Document document) {
        return new DocumentResponseDto(
                document.getId(),
                document.getTitle(),
                document.getDescription(),
                document.getStatus() != null ? document.getStatus().name() : null,
                document.getPublishedVersion() != null ? document.getPublishedVersion().getId() : null
        );
    }
}