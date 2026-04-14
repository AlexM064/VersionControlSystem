package com.sap.vcs.server.service;

import com.sap.vcs.server.dto.DocumentHistoryResponseDto;
import com.sap.vcs.server.dto.DocumentRequestDto;
import com.sap.vcs.server.dto.DocumentResponseDto;
import com.sap.vcs.server.dto.DocumentVersionResponseDto;
import com.sap.vcs.server.dto.UpdateDocumentMetadataRequestDto;
import com.sap.vcs.server.entity.Document;
import com.sap.vcs.server.entity.DocumentVersion;
import com.sap.vcs.server.entity.User;
import com.sap.vcs.server.entity.enums.AuditActionType;
import com.sap.vcs.server.entity.enums.DocumentStatus;
import com.sap.vcs.server.exception.BusinessRuleViolationException;
import com.sap.vcs.server.exception.ResourceNotFoundException;
import com.sap.vcs.server.repository.DocumentRepository;
import com.sap.vcs.server.specification.DocumentSpecification;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final AuditLogService auditLogService;
    private final DocumentVisibilityService documentVisibilityService;

    public DocumentService(
            DocumentRepository documentRepository,
            AuditLogService auditLogService,
            DocumentVisibilityService documentVisibilityService
    ) {
        this.documentRepository = documentRepository;
        this.auditLogService = auditLogService;
        this.documentVisibilityService = documentVisibilityService;
    }

    @PreAuthorize("hasAnyRole('AUTHOR', 'ADMIN')")
    public DocumentResponseDto createDocument(DocumentRequestDto request) {
        User currentUser = documentVisibilityService.getCurrentAuthenticatedUser();

        Document document = new Document();
        document.setTitle(request.getTitle());
        document.setDescription(request.getDescription());
        document.setOwner(currentUser);

        Document savedDocument = documentRepository.save(document);

        auditLogService.log(
                AuditActionType.DOCUMENT_CREATED,
                "DOCUMENT",
                savedDocument.getId(),
                currentUser.getUsername(),
                "Document created with title: " + savedDocument.getTitle()
        );

        return mapToResponse(savedDocument);
    }

    @PreAuthorize("hasAnyRole('AUTHOR', 'REVIEWER', 'ADMIN')")
    public Page<DocumentResponseDto> getAllDocuments(
            String title,
            DocumentStatus status,
            Pageable pageable
    ) {
        User currentUser = documentVisibilityService.getCurrentAuthenticatedUser();

        Specification<Document> spec = null;

        if (title != null && !title.isBlank()) {
            spec = DocumentSpecification.titleContains(title);
        }

        if (status != null) {
            Specification<Document> statusSpec = DocumentSpecification.hasStatus(status);
            spec = (spec == null) ? statusSpec : spec.and(statusSpec);
        }

        Sort sort = pageable.getSort().isSorted() ? pageable.getSort() : Sort.by("createdAt");

        List<DocumentResponseDto> visibleDocuments = documentRepository.findAll(spec, sort)
                .stream()
                .filter(document -> documentVisibilityService.canViewDocument(currentUser, document))
                .map(this::mapToResponse)
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), visibleDocuments.size());

        List<DocumentResponseDto> pageContent =
                start >= visibleDocuments.size() ? List.of() : visibleDocuments.subList(start, end);

        return new PageImpl<>(pageContent, pageable, visibleDocuments.size());
    }

    @PreAuthorize("hasAnyRole('AUTHOR', 'REVIEWER', 'ADMIN')")
    public DocumentResponseDto getDocumentById(Integer id) {
        User currentUser = documentVisibilityService.getCurrentAuthenticatedUser();

        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + id));

        documentVisibilityService.ensureCanViewDocument(currentUser, document);

        return mapToResponse(document);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('AUTHOR', 'ADMIN')")
    public void archiveDocument(Integer documentId) {
        User currentUser = documentVisibilityService.getCurrentAuthenticatedUser();

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

        documentVisibilityService.validateOwnershipOrAdmin(
                document,
                currentUser,
                "You do not have permission to modify this document"
        );

        if (document.getStatus() == DocumentStatus.ARCHIVED) {
            throw new BusinessRuleViolationException("Document is already archived");
        }

        document.setStatus(DocumentStatus.ARCHIVED);
        documentRepository.save(document);

        auditLogService.log(
                AuditActionType.DOCUMENT_ARCHIVED,
                "DOCUMENT",
                document.getId(),
                currentUser.getUsername(),
                "Document archived"
        );
    }

    @PreAuthorize("hasAnyRole('AUTHOR', 'REVIEWER', 'ADMIN')")
    public List<DocumentHistoryResponseDto> getDocumentHistory(Integer id) {
        User currentUser = documentVisibilityService.getCurrentAuthenticatedUser();

        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + id));

        documentVisibilityService.ensureCanViewDocument(currentUser, document);

        Integer publishedVersionId = document.getPublishedVersion() != null
                ? document.getPublishedVersion().getId()
                : null;

        return documentVisibilityService.getVisibleVersionsForUser(currentUser, document).stream()
                .map(version -> mapToHistoryResponse(version, publishedVersionId))
                .toList();
    }

    @PreAuthorize("hasAnyRole('READER', 'AUTHOR', 'REVIEWER', 'ADMIN')")
    public DocumentVersionResponseDto getPublishedVersion(Integer id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + id));

        DocumentVersion publishedVersion = document.getPublishedVersion();

        if (publishedVersion == null) {
            throw new ResourceNotFoundException("No published version found for document id: " + id);
        }

        return mapToVersionResponse(publishedVersion);
    }

    @PreAuthorize("hasAnyRole('AUTHOR', 'ADMIN')")
    public DocumentResponseDto updateDocument(Integer id, UpdateDocumentMetadataRequestDto request) {
        User currentUser = documentVisibilityService.getCurrentAuthenticatedUser();

        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + id));

        documentVisibilityService.validateOwnershipOrAdmin(
                document,
                currentUser,
                "You do not have permission to modify this document"
        );

        document.setTitle(request.getTitle());
        document.setDescription(request.getDescription());

        Document updatedDocument = documentRepository.save(document);

        auditLogService.log(
                AuditActionType.DOCUMENT_UPDATED,
                "DOCUMENT",
                updatedDocument.getId(),
                currentUser.getUsername(),
                "Document metadata updated"
        );

        return mapToResponse(updatedDocument);
    }

    public List<DocumentResponseDto> getPublishedDocuments() {
        return documentRepository.findAll().stream()
                .filter(doc -> doc.getPublishedVersion() != null)
                .map(this::mapToResponse)
                .toList();
    }

    public DocumentResponseDto getPublishedOnly(Integer id) {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + id));

        if (doc.getPublishedVersion() == null) {
            throw new ResourceNotFoundException("No published version available for document id: " + id);
        }

        return mapToResponse(doc);
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
                version.getStatus(),
                version.getCreatedBy() != null ? version.getCreatedBy().getUsername() : null,
                version.getCreatedAt()
        );
    }

    private DocumentResponseDto mapToResponse(Document document) {
        return new DocumentResponseDto(
                document.getId(),
                document.getTitle(),
                document.getDescription(),
                document.getStatus().name(),
                document.getPublishedVersion() != null ? document.getPublishedVersion().getId() : null,
                document.getOwner() != null ? document.getOwner().getUsername() : null,
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }
}