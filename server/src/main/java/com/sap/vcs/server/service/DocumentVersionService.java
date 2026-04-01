package com.sap.vcs.server.service;

import com.sap.vcs.server.dto.CompareVersionsResponseDto;
import com.sap.vcs.server.dto.DocumentVersionRequestDto;
import com.sap.vcs.server.dto.DocumentVersionResponseDto;
import com.sap.vcs.server.dto.PublishDocumentResponseDto;
import com.sap.vcs.server.entity.Document;
import com.sap.vcs.server.entity.DocumentVersion;
import com.sap.vcs.server.entity.User;
import com.sap.vcs.server.entity.enums.AuditActionType;
import com.sap.vcs.server.entity.enums.DocumentStatus;
import com.sap.vcs.server.entity.enums.VersionStatus;
import com.sap.vcs.server.exception.BusinessRuleViolationException;
import com.sap.vcs.server.exception.ResourceNotFoundException;
import com.sap.vcs.server.repository.ApprovalRepository;
import com.sap.vcs.server.repository.DocumentRepository;
import com.sap.vcs.server.repository.DocumentVersionRepository;
import com.sap.vcs.server.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class DocumentVersionService {

    private final DocumentVersionRepository versionRepository;
    private final DocumentRepository documentRepository;
    private final ApprovalRepository approvalRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public DocumentVersionService(
            DocumentVersionRepository versionRepository,
            DocumentRepository documentRepository,
            ApprovalRepository approvalRepository,
            UserRepository userRepository,
            AuditLogService auditLogService
    ) {
        this.versionRepository = versionRepository;
        this.documentRepository = documentRepository;
        this.approvalRepository = approvalRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    @PreAuthorize("hasAnyRole('AUTHOR', 'ADMIN')")
    public DocumentVersionResponseDto createVersion(Integer documentId, DocumentVersionRequestDto request) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Document not found with id: " + documentId));

        validateDocumentIsNotArchived(document);

        User currentUser = getCurrentAuthenticatedUser();
        validateOwnershipOrAdmin(document, currentUser);

        Integer nextVersionNumber = versionRepository
                .findTopByDocumentOrderByVersionNumberDesc(document)
                .map(lastVersion -> lastVersion.getVersionNumber() + 1)
                .orElse(1);

        DocumentVersion version = new DocumentVersion();
        version.setStatus(VersionStatus.DRAFT);
        version.setDocument(document);
        version.setVersionNumber(nextVersionNumber);
        version.setContent(request.getContent());
        version.setMessage(request.getMessage());
        version.setCreatedBy(currentUser);

        DocumentVersion savedVersion = versionRepository.save(version);

        auditLogService.log(
                AuditActionType.VERSION_CREATED,
                "DOCUMENT_VERSION",
                savedVersion.getId(),
                currentUser.getUsername(),
                "Created version " + savedVersion.getVersionNumber() + " for document " + document.getId()
        );

        return mapToResponse(savedVersion);
    }

    @PreAuthorize("hasAnyRole('AUTHOR', 'REVIEWER', 'ADMIN')")
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
    @PreAuthorize("hasAnyRole('REVIEWER', 'ADMIN')")
    public PublishDocumentResponseDto publishVersion(Integer versionId) {
        DocumentVersion version = versionRepository.findById(versionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Version not found with id: " + versionId));

        validateDocumentIsNotArchived(version.getDocument());
        validatePublishRules(version);

        PublishDocumentResponseDto response = applyPublishedVersion(version);

        String username = getCurrentAuthenticatedUser().getUsername();
        auditLogService.log(
                AuditActionType.VERSION_PUBLISHED,
                "DOCUMENT_VERSION",
                version.getId(),
                username,
                "Published version " + version.getVersionNumber() + " for document " + version.getDocument().getId()
        );

        return response;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('REVIEWER', 'ADMIN')")
    public PublishDocumentResponseDto rollbackVersion(Integer versionId) {
        DocumentVersion version = versionRepository.findById(versionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Version not found with id: " + versionId));

        validateDocumentIsNotArchived(version.getDocument());
        validateRollbackRules(version);

        PublishDocumentResponseDto response = applyPublishedVersion(version);

        String username = getCurrentAuthenticatedUser().getUsername();
        auditLogService.log(
                AuditActionType.VERSION_ROLLED_BACK,
                "DOCUMENT_VERSION",
                version.getId(),
                username,
                "Rolled back to version " + version.getVersionNumber() + " for document " + version.getDocument().getId()
        );

        return response;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('AUTHOR', 'ADMIN')")
    public DocumentVersionResponseDto submitForReview(Integer versionId) {
        DocumentVersion version = versionRepository.findById(versionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Version not found with id: " + versionId));

        validateDocumentIsNotArchived(version.getDocument());

        User currentUser = getCurrentAuthenticatedUser();
        validateOwnershipOrAdmin(version.getDocument(), currentUser);

        if (version.getStatus() != VersionStatus.DRAFT) {
            throw new BusinessRuleViolationException(
                    "Only DRAFT versions can be submitted for review"
            );
        }

        version.setStatus(VersionStatus.IN_REVIEW);

        DocumentVersion saved = versionRepository.save(version);

        auditLogService.log(
                AuditActionType.VERSION_SUBMITTED_FOR_REVIEW,
                "DOCUMENT_VERSION",
                saved.getId(),
                currentUser.getUsername(),
                "Submitted version " + saved.getVersionNumber() + " for review"
        );

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('AUTHOR', 'REVIEWER', 'ADMIN')")
    public CompareVersionsResponseDto compareVersions(Integer leftVersionId, Integer rightVersionId) {
        DocumentVersion leftVersion = versionRepository.findById(leftVersionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Version not found with id: " + leftVersionId));

        DocumentVersion rightVersion = versionRepository.findById(rightVersionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Version not found with id: " + rightVersionId));

        validateSameDocument(leftVersion, rightVersion);

        boolean identical = Objects.equals(leftVersion.getContent(), rightVersion.getContent());

        return new CompareVersionsResponseDto(
                leftVersion.getDocument().getId(),
                leftVersion.getId(),
                leftVersion.getVersionNumber(),
                leftVersion.getContent(),
                rightVersion.getId(),
                rightVersion.getVersionNumber(),
                rightVersion.getContent(),
                identical
        );
    }

    private void validatePublishRules(DocumentVersion version) {
        if (version.getStatus() != VersionStatus.APPROVED) {
            throw new BusinessRuleViolationException(
                    "Only APPROVED versions can be published"
            );
        }
    }

    private void validateRollbackRules(DocumentVersion version) {
        if (version.getStatus() != VersionStatus.APPROVED
                && version.getStatus() != VersionStatus.PUBLISHED) {
            throw new BusinessRuleViolationException(
                    "Rollback is allowed only to APPROVED or PUBLISHED versions"
            );
        }
    }

    private void validateDocumentIsNotArchived(Document document) {
        if (document.getStatus() == DocumentStatus.ARCHIVED) {
            throw new BusinessRuleViolationException("Cannot perform operation on archived document");
        }
    }

    private void validateSameDocument(DocumentVersion leftVersion, DocumentVersion rightVersion) {
        if (!leftVersion.getDocument().getId().equals(rightVersion.getDocument().getId())) {
            throw new BusinessRuleViolationException(
                    "Versions can be compared only if they belong to the same document"
            );
        }
    }

    private PublishDocumentResponseDto applyPublishedVersion(DocumentVersion version) {
        Document document = version.getDocument();
        DocumentVersion currentPublishedVersion = document.getPublishedVersion();

        if (currentPublishedVersion != null && !currentPublishedVersion.getId().equals(version.getId())) {
            currentPublishedVersion.setStatus(VersionStatus.APPROVED);
            versionRepository.save(currentPublishedVersion);
        }

        version.setStatus(VersionStatus.PUBLISHED);
        DocumentVersion savedVersion = versionRepository.save(version);

        document.setPublishedVersion(savedVersion);
        document.setStatus(DocumentStatus.ACTIVE);
        documentRepository.save(document);

        return new PublishDocumentResponseDto(
                document.getId(),
                document.getTitle(),
                document.getStatus() != null ? document.getStatus().name() : null,
                document.getPublishedVersion() != null ? document.getPublishedVersion().getId() : null,
                document.getPublishedVersion() != null
                        ? document.getPublishedVersion().getVersionNumber()
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
                version.getStatus(),
                version.getCreatedBy() != null ? version.getCreatedBy().getUsername() : null,
                version.getCreatedAt()
        );
    }

    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new IllegalStateException("No authenticated user available in security context");
        }

        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Authenticated user not found with username: " + authentication.getName()
                ));
    }

    private void validateOwnershipOrAdmin(Document document, User user) {
        boolean isAdmin = user.getRoles() != null &&
                user.getRoles().stream().anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getName()));

        boolean isOwner = document.getOwner() != null &&
                document.getOwner().getId().equals(user.getId());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("You do not have permission to create versions for this document");
        }
    }
}