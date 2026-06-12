package com.sap.vcs.server.service;

import com.sap.vcs.server.entity.Document;
import com.sap.vcs.server.entity.DocumentVersion;
import com.sap.vcs.server.entity.User;
import com.sap.vcs.server.entity.enums.VersionStatus;
import com.sap.vcs.server.exception.ResourceNotFoundException;
import com.sap.vcs.server.repository.DocumentVersionRepository;
import com.sap.vcs.server.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentVisibilityService {

    private final UserRepository userRepository;
    private final DocumentVersionRepository documentVersionRepository;

    public DocumentVisibilityService(
            UserRepository userRepository,
            DocumentVersionRepository documentVersionRepository
    ) {
        this.userRepository = userRepository;
        this.documentVersionRepository = documentVersionRepository;
    }

    public User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new IllegalStateException("No authenticated user available in security context");
        }

        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Authenticated user not found with username: " + authentication.getName()
                ));
    }

    public boolean canViewDocument(User user, Document document) {
        if (isAdmin(user)) {
            return true;
        }

        if (isOwner(document, user)) {
            return true;
        }

        if (document.getPublishedVersion() != null) {
            return true;
        }

        return isReviewer(user) && hasReviewerWorkflowVisibleVersion(document);
    }

    public void ensureCanViewDocument(User user, Document document) {
        if (!canViewDocument(user, document)) {
            throw new AccessDeniedException("You do not have permission to access this document");
        }
    }

    public boolean canViewVersion(User user, DocumentVersion version) {
        Document document = version.getDocument();

        if (!canViewDocument(user, document)) {
            return false;
        }

        if (isAdmin(user)) {
            return true;
        }

        if (isOwner(document, user)) {
            return true;
        }

        if (isReviewer(user) && hasReviewerWorkflowVisibleVersion(document)) {
            return true;
        }

        return document.getPublishedVersion() != null
                && document.getPublishedVersion().getId().equals(version.getId());
    }

    public void ensureCanViewVersion(User user, DocumentVersion version) {
        if (!canViewVersion(user, version)) {
            throw new AccessDeniedException("You do not have permission to access this version");
        }
    }

    public List<DocumentVersion> getVisibleVersionsForUser(User user, Document document) {
        List<DocumentVersion> allVersions = documentVersionRepository.findByDocumentOrderByVersionNumberAsc(document);

        if (isAdmin(user)) {
            return allVersions;
        }

        if (isOwner(document, user)) {
            return allVersions;
        }

        if (isReviewer(user) && hasReviewerWorkflowVisibleVersion(document)) {
            return allVersions;
        }

        DocumentVersion publishedVersion = document.getPublishedVersion();
        if (publishedVersion != null) {
            return List.of(publishedVersion);
        }

        return List.of();
    }

    public void validateOwnershipOrAdmin(Document document, User user, String message) {
        boolean isAdmin = isAdmin(user);
        boolean isOwner = isOwner(document, user);

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException(message);
        }
    }

    public boolean hasReviewerWorkflowVisibleVersion(Document document) {
        return documentVersionRepository.existsByDocumentAndStatusIn(
                document,
                List.of(VersionStatus.IN_REVIEW, VersionStatus.APPROVED)
        );
    }

    public boolean isOwner(Document document, User user) {
        return document.getOwner() != null
                && user.getId() != null
                && user.getId().equals(document.getOwner().getId());
    }

    public boolean isAdmin(User user) {
        return hasRole(user, "ADMIN");
    }

    public boolean isReviewer(User user) {
        return hasRole(user, "REVIEWER");
    }

    public boolean hasRole(User user, String roleName) {
        return user.getRoles() != null
                && user.getRoles().stream().anyMatch(role -> roleName.equalsIgnoreCase(role.getName()));
    }
}