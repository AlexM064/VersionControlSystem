package com.sap.vcs.server.service;

import com.sap.vcs.server.dto.DocumentVersionRequestDto;
import com.sap.vcs.server.dto.DocumentVersionResponseDto;
import com.sap.vcs.server.dto.PublishDocumentResponseDto;
import com.sap.vcs.server.entity.Document;
import com.sap.vcs.server.entity.DocumentVersion;
import com.sap.vcs.server.entity.User;
import com.sap.vcs.server.entity.enums.ApprovalDecision;
import com.sap.vcs.server.entity.enums.DocumentStatus;
import com.sap.vcs.server.entity.enums.VersionStatus;
import com.sap.vcs.server.exception.BusinessRuleViolationException;
import com.sap.vcs.server.exception.ResourceNotFoundException;
import com.sap.vcs.server.repository.ApprovalRepository;
import com.sap.vcs.server.repository.DocumentRepository;
import com.sap.vcs.server.repository.DocumentVersionRepository;
import com.sap.vcs.server.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentVersionService {

    private final DocumentVersionRepository versionRepository;
    private final DocumentRepository documentRepository;
    private final ApprovalRepository approvalRepository;
    private final UserRepository userRepository;

    public DocumentVersionService(
            DocumentVersionRepository versionRepository,
            DocumentRepository documentRepository,
            ApprovalRepository approvalRepository,
            UserRepository userRepository
    ) {
        this.versionRepository = versionRepository;
        this.documentRepository = documentRepository;
        this.approvalRepository = approvalRepository;
        this.userRepository = userRepository;
    }

    @PreAuthorize("hasAnyRole('AUTHOR', 'ADMIN')")
    public DocumentVersionResponseDto createVersion(Integer documentId, DocumentVersionRequestDto request) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Document not found with id: " + documentId));
        User currentUser = getCurrentAuthenticatedUser();

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

        validatePublishRules(version);

        return applyPublishedVersion(version);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('REVIEWER', 'ADMIN')")
    public PublishDocumentResponseDto rollbackVersion(Integer versionId) {
        DocumentVersion version = versionRepository.findById(versionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Version not found with id: " + versionId));

        return applyPublishedVersion(version);
    }

    private void validatePublishRules(DocumentVersion version) {
        if (version.getStatus() != VersionStatus.APPROVED) {
            throw new BusinessRuleViolationException(
                    "Only APPROVED versions can be published"
            );
        }
    }

    private PublishDocumentResponseDto applyPublishedVersion(DocumentVersion version) {
        Document document = version.getDocument();

        version.setStatus(VersionStatus.PUBLISHED);
        DocumentVersion savedVersion = versionRepository.save(version);

        document.setPublishedVersion(savedVersion);
        document.setStatus(DocumentStatus.PUBLISHED);

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

    @Transactional
    @PreAuthorize("hasAnyRole('AUTHOR', 'ADMIN')")
    public DocumentVersionResponseDto submitForReview(Integer versionId) {
        DocumentVersion version = versionRepository.findById(versionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Version not found with id: " + versionId));

        if (version.getStatus() != VersionStatus.DRAFT) {
            throw new BusinessRuleViolationException(
                    "Only DRAFT versions can be submitted for review"
            );
        }

        version.setStatus(VersionStatus.IN_REVIEW);

        DocumentVersion saved = versionRepository.save(version);

        return mapToResponse(saved);
    }
}
