package com.sap.vcs.server.service;

import com.sap.vcs.server.dto.*;
import com.sap.vcs.server.entity.Document;
import com.sap.vcs.server.entity.DocumentVersion;
import com.sap.vcs.server.entity.User;
import com.sap.vcs.server.entity.enums.DocumentStatus;
import com.sap.vcs.server.exception.ResourceNotFoundException;
import com.sap.vcs.server.repository.DocumentRepository;
import com.sap.vcs.server.repository.DocumentVersionRepository;
import com.sap.vcs.server.repository.UserRepository;
import com.sap.vcs.server.specification.DocumentSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final UserRepository userRepository;

    public DocumentService(DocumentRepository documentRepository,
                           DocumentVersionRepository documentVersionRepository,
                           UserRepository userRepository) {
        this.documentRepository = documentRepository;
        this.documentVersionRepository = documentVersionRepository;
        this.userRepository = userRepository;
    }

    @PreAuthorize("hasAnyRole('AUTHOR', 'ADMIN')")
    public DocumentResponseDto createDocument(DocumentRequestDto request) {
        User currentUser = getCurrentAuthenticatedUser();

        Document document = new Document();
        document.setTitle(request.getTitle());
        document.setDescription(request.getDescription());
        document.setOwner(currentUser);

        Document savedDocument = documentRepository.save(document);
        return mapToResponse(savedDocument);
    }

    @PreAuthorize("hasAnyRole('AUTHOR', 'REVIEWER', 'ADMIN')")
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

    @PreAuthorize("hasAnyRole('AUTHOR', 'REVIEWER', 'ADMIN')")
    public DocumentResponseDto getDocumentById(Integer id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Document not found with id: " + id));

        return mapToResponse(document);
    }

    @PreAuthorize("hasAnyRole('AUTHOR', 'REVIEWER', 'ADMIN')")
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

    @PreAuthorize("hasAnyRole('READER', 'AUTHOR', 'REVIEWER', 'ADMIN')")
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

    @PreAuthorize("hasAnyRole('AUTHOR', 'ADMIN')")
    public DocumentResponseDto updateDocument(Integer id, UpdateDocumentMetadataRequestDto request) {
        User currentUser = getCurrentAuthenticatedUser();

        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + id));

        validateOwnershipOrAdmin(document, currentUser);

        document.setTitle(request.getTitle());
        document.setDescription(request.getDescription());

        Document updatedDocument = documentRepository.save(document);
        return mapToResponse(updatedDocument);
    }

    private User getCurrentAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found: " + username));
    }

    private void validateOwnershipOrAdmin(Document document, User user) {
        boolean isAdmin = user.getRoles() != null &&
                user.getRoles().stream().anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getName()));

        boolean isOwner = document.getOwner() != null &&
                document.getOwner().getId().equals(user.getId());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("You do not have permission to modify this document");
        }
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