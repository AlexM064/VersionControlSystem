package com.sap.vcs.server.service;

import com.sap.vcs.server.dto.CompareVersionsResponseDto;
import com.sap.vcs.server.entity.Document;
import com.sap.vcs.server.entity.DocumentVersion;
import com.sap.vcs.server.entity.Role;
import com.sap.vcs.server.entity.User;
import com.sap.vcs.server.exception.BusinessRuleViolationException;
import com.sap.vcs.server.repository.DocumentRepository;
import com.sap.vcs.server.repository.DocumentVersionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.sap.vcs.server.dto.DocumentVersionRequestDto;
import com.sap.vcs.server.dto.DocumentVersionResponseDto;
import com.sap.vcs.server.dto.PublishDocumentResponseDto;
import com.sap.vcs.server.entity.enums.AuditActionType;
import com.sap.vcs.server.entity.enums.DocumentStatus;
import com.sap.vcs.server.entity.enums.VersionStatus;
import com.sap.vcs.server.exception.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DocumentVersionServiceTest {

    @Mock
    private DocumentVersionRepository documentVersionRepository;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private DocumentVisibilityService documentVisibilityService;

    private DocumentVersionService createService() {
        return new DocumentVersionService(
                documentVersionRepository,
                documentRepository,
                auditLogService,
                documentVisibilityService
        );
    }

    @Test
    void compareVersions_returnsExpectedPayloadForSameDocument() {
        DocumentVersionService documentVersionService = createService();

        User reviewer = createUser(1, "reviewer.local", "REVIEWER");

        Document document = new Document();
        document.setId(1);

        DocumentVersion leftVersion = new DocumentVersion();
        leftVersion.setId(10);
        leftVersion.setDocument(document);
        leftVersion.setVersionNumber(1);
        leftVersion.setContent("First content");

        DocumentVersion rightVersion = new DocumentVersion();
        rightVersion.setId(11);
        rightVersion.setDocument(document);
        rightVersion.setVersionNumber(2);
        rightVersion.setContent("Second content");

        when(documentVisibilityService.getCurrentAuthenticatedUser())
                .thenReturn(reviewer);
        when(documentVersionRepository.findById(10)).thenReturn(Optional.of(leftVersion));
        when(documentVersionRepository.findById(11)).thenReturn(Optional.of(rightVersion));
        doNothing().when(documentVisibilityService).ensureCanViewVersion(reviewer, leftVersion);
        doNothing().when(documentVisibilityService).ensureCanViewVersion(reviewer, rightVersion);

        CompareVersionsResponseDto response = documentVersionService.compareVersions(10, 11);

        assertEquals(1, response.getDocumentId());
        assertEquals(10, response.getLeftVersionId());
        assertEquals(1, response.getLeftVersionNumber());
        assertEquals("First content", response.getLeftContent());
        assertEquals(11, response.getRightVersionId());
        assertEquals(2, response.getRightVersionNumber());
        assertEquals("Second content", response.getRightContent());
        assertFalse(response.isIdentical());
    }

    @Test
    void compareVersions_throwsWhenVersionsBelongToDifferentDocuments() {
        DocumentVersionService documentVersionService = createService();

        Document leftDocument = new Document();
        leftDocument.setId(1);

        Document rightDocument = new Document();
        rightDocument.setId(2);

        DocumentVersion leftVersion = new DocumentVersion();
        leftVersion.setId(10);
        leftVersion.setDocument(leftDocument);

        DocumentVersion rightVersion = new DocumentVersion();
        rightVersion.setId(11);
        rightVersion.setDocument(rightDocument);

        when(documentVersionRepository.findById(10)).thenReturn(Optional.of(leftVersion));
        when(documentVersionRepository.findById(11)).thenReturn(Optional.of(rightVersion));

        assertThrows(
                BusinessRuleViolationException.class,
                () -> documentVersionService.compareVersions(10, 11)
        );
    }

    private User createUser(Integer id, String username, String roleName) {
        Role role = new Role();
        role.setId(id);
        role.setName(roleName);

        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setRoles(Set.of(role));
        return user;
    }


    @Test
    void createVersion_createsDraftForOwner() {
        DocumentVersionService documentVersionService = createService();

        User owner = createUser(1, "author.local", "AUTHOR");

        Document document = new Document();
        document.setId(50);
        document.setTitle("Spec");
        document.setOwner(owner);
        document.setStatus(DocumentStatus.ACTIVE);

        DocumentVersion previousVersion = new DocumentVersion();
        previousVersion.setId(8);
        previousVersion.setDocument(document);
        previousVersion.setVersionNumber(3);
        previousVersion.setStatus(VersionStatus.PUBLISHED);

        DocumentVersionRequestDto request = new DocumentVersionRequestDto();
        request.setContent("New content");
        request.setMessage("Added summary");

        when(documentRepository.findById(50)).thenReturn(Optional.of(document));
        when(documentVisibilityService.getCurrentAuthenticatedUser()).thenReturn(owner);
        doNothing().when(documentVisibilityService).validateOwnershipOrAdmin(
                document,
                owner,
                "You do not have permission to create versions for this document"
        );
        when(documentVersionRepository.findTopByDocumentOrderByVersionNumberDesc(document))
                .thenReturn(Optional.of(previousVersion));
        when(documentVersionRepository.save(any(DocumentVersion.class))).thenAnswer(invocation -> {
            DocumentVersion saved = invocation.getArgument(0);
            saved.setId(9);
            return saved;
        });

        DocumentVersionResponseDto response = documentVersionService.createVersion(50, request);

        assertNotNull(response);
        assertEquals(9, response.getId());
        assertEquals(50, response.getDocumentId());
        assertEquals(4, response.getVersionNumber());
        assertEquals("New content", response.getContent());
        assertEquals("Added summary", response.getMessage());
        assertEquals(VersionStatus.DRAFT, response.getStatus());
        assertEquals("author.local", response.getCreatedByUsername());

        verify(auditLogService).log(
                eq(AuditActionType.VERSION_CREATED),
                eq("DOCUMENT_VERSION"),
                eq(9),
                eq("author.local"),
                contains("Created version 4 for document 50")
        );
    }

    @Test
    void createVersion_startsFromOneWhenNoPreviousVersions() {
        DocumentVersionService documentVersionService = createService();

        User owner = createUser(1, "author.local", "AUTHOR");

        Document document = new Document();
        document.setId(50);
        document.setTitle("Spec");
        document.setOwner(owner);
        document.setStatus(DocumentStatus.ACTIVE);

        DocumentVersionRequestDto request = new DocumentVersionRequestDto();
        request.setContent("Initial content");
        request.setMessage("Initial version");

        when(documentRepository.findById(50)).thenReturn(Optional.of(document));
        when(documentVisibilityService.getCurrentAuthenticatedUser()).thenReturn(owner);
        doNothing().when(documentVisibilityService).validateOwnershipOrAdmin(
                document,
                owner,
                "You do not have permission to create versions for this document"
        );
        when(documentVersionRepository.findTopByDocumentOrderByVersionNumberDesc(document))
                .thenReturn(Optional.empty());
        when(documentVersionRepository.save(any(DocumentVersion.class))).thenAnswer(invocation -> {
            DocumentVersion saved = invocation.getArgument(0);
            saved.setId(1);
            return saved;
        });

        DocumentVersionResponseDto response = documentVersionService.createVersion(50, request);

        assertEquals(1, response.getVersionNumber());
        assertEquals(VersionStatus.DRAFT, response.getStatus());
        assertEquals("author.local", response.getCreatedByUsername());
    }

        @Test
        void createVersion_createsDraftForAdminWhenNotOwner() {
        DocumentVersionService documentVersionService = createService();

        User owner = createUser(1, "owner.local", "AUTHOR");
        User admin = createUser(2, "admin.local", "ADMIN");

        Document document = new Document();
        document.setId(50);
        document.setTitle("Spec");
        document.setOwner(owner);
        document.setStatus(DocumentStatus.ACTIVE);

        DocumentVersionRequestDto request = new DocumentVersionRequestDto();
        request.setContent("Admin content");
        request.setMessage("Admin change");

        when(documentRepository.findById(50)).thenReturn(Optional.of(document));
        when(documentVisibilityService.getCurrentAuthenticatedUser()).thenReturn(admin);
        doNothing().when(documentVisibilityService).validateOwnershipOrAdmin(
            document,
            admin,
            "You do not have permission to create versions for this document"
        );
        when(documentVersionRepository.findTopByDocumentOrderByVersionNumberDesc(document))
            .thenReturn(Optional.empty());
        when(documentVersionRepository.save(any(DocumentVersion.class))).thenAnswer(invocation -> {
            DocumentVersion saved = invocation.getArgument(0);
            saved.setId(33);
            return saved;
        });

        DocumentVersionResponseDto response = documentVersionService.createVersion(50, request);

        assertEquals(33, response.getId());
        assertEquals(1, response.getVersionNumber());
        assertEquals("admin.local", response.getCreatedByUsername());

        verify(auditLogService).log(
            eq(AuditActionType.VERSION_CREATED),
            eq("DOCUMENT_VERSION"),
            eq(33),
            eq("admin.local"),
            contains("Created version 1 for document 50")
        );
        }

        @Test
        void createVersion_throwsWhenOwnershipValidationFails() {
        DocumentVersionService documentVersionService = createService();

        User owner = createUser(1, "owner.local", "AUTHOR");
        User otherAuthor = createUser(2, "other.local", "AUTHOR");

        Document document = new Document();
        document.setId(50);
        document.setOwner(owner);
        document.setStatus(DocumentStatus.ACTIVE);

        DocumentVersionRequestDto request = new DocumentVersionRequestDto();
        request.setContent("New content");
        request.setMessage("msg");

        when(documentRepository.findById(50)).thenReturn(Optional.of(document));
        when(documentVisibilityService.getCurrentAuthenticatedUser()).thenReturn(otherAuthor);
        doThrow(new AccessDeniedException("Forbidden")).when(documentVisibilityService)
            .validateOwnershipOrAdmin(
                document,
                otherAuthor,
                "You do not have permission to create versions for this document"
            );

        assertThrows(AccessDeniedException.class, () -> documentVersionService.createVersion(50, request));

        verify(documentVersionRepository, never()).findTopByDocumentOrderByVersionNumberDesc(any());
        verify(documentVersionRepository, never()).save(any());
        verify(auditLogService, never()).log(any(), any(), any(), any(), any());
        }

    @Test
    void createVersion_throwsWhenDocumentNotFound() {
        DocumentVersionService documentVersionService = createService();

        DocumentVersionRequestDto request = new DocumentVersionRequestDto();
        request.setContent("New content");
        request.setMessage("msg");

        when(documentRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> documentVersionService.createVersion(999, request));

        verify(documentVisibilityService, never()).getCurrentAuthenticatedUser();
        verify(documentVersionRepository, never()).save(any());
        verify(auditLogService, never()).log(any(), any(), any(), any(), any());
    }

    @Test
    void createVersion_throwsWhenDocumentIsArchived() {
        DocumentVersionService documentVersionService = createService();

        Document document = new Document();
        document.setId(50);
        document.setStatus(DocumentStatus.ARCHIVED);

        DocumentVersionRequestDto request = new DocumentVersionRequestDto();
        request.setContent("New content");
        request.setMessage("msg");

        when(documentRepository.findById(50)).thenReturn(Optional.of(document));

        assertThrows(BusinessRuleViolationException.class, () -> documentVersionService.createVersion(50, request));

        verify(documentVisibilityService, never()).getCurrentAuthenticatedUser();
        verify(documentVersionRepository, never()).save(any());
        verify(auditLogService, never()).log(any(), any(), any(), any(), any());
    }

    @Test
    void submitForReview_movesDraftToInReview() {
        DocumentVersionService documentVersionService = createService();

        User owner = createUser(1, "author.local", "AUTHOR");

        Document document = new Document();
        document.setId(50);
        document.setOwner(owner);
        document.setStatus(DocumentStatus.ACTIVE);

        DocumentVersion version = new DocumentVersion();
        version.setId(7);
        version.setDocument(document);
        version.setVersionNumber(2);
        version.setStatus(VersionStatus.DRAFT);
        version.setContent("Draft content");
        version.setMessage("Draft message");
        version.setCreatedBy(owner);

        when(documentVersionRepository.findById(7)).thenReturn(Optional.of(version));
        when(documentVisibilityService.getCurrentAuthenticatedUser()).thenReturn(owner);
        doNothing().when(documentVisibilityService).validateOwnershipOrAdmin(
                document,
                owner,
                "You do not have permission to create versions for this document"
        );
        when(documentVersionRepository.save(any(DocumentVersion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DocumentVersionResponseDto response = documentVersionService.submitForReview(7);

        assertEquals(VersionStatus.IN_REVIEW, response.getStatus());
        assertEquals(VersionStatus.IN_REVIEW, version.getStatus());

        verify(auditLogService).log(
                eq(AuditActionType.VERSION_SUBMITTED_FOR_REVIEW),
                eq("DOCUMENT_VERSION"),
                eq(7),
                eq("author.local"),
                contains("Submitted version 2 for review")
        );
    }

    @Test
    void submitForReview_throwsWhenVersionIsNotDraft() {
        DocumentVersionService documentVersionService = createService();

        User owner = createUser(1, "author.local", "AUTHOR");

        Document document = new Document();
        document.setId(50);
        document.setOwner(owner);
        document.setStatus(DocumentStatus.ACTIVE);

        DocumentVersion version = new DocumentVersion();
        version.setId(7);
        version.setDocument(document);
        version.setVersionNumber(2);
        version.setStatus(VersionStatus.IN_REVIEW);

        when(documentVersionRepository.findById(7)).thenReturn(Optional.of(version));
        when(documentVisibilityService.getCurrentAuthenticatedUser()).thenReturn(owner);
        doNothing().when(documentVisibilityService).validateOwnershipOrAdmin(
                document,
                owner,
                "You do not have permission to create versions for this document"
        );

        assertThrows(BusinessRuleViolationException.class, () -> documentVersionService.submitForReview(7));

        verify(documentVersionRepository, never()).save(any());
        verify(auditLogService, never()).log(any(), any(), any(), any(), any());
    }

    @Test
    void submitForReview_throwsWhenVersionNotFound() {
        DocumentVersionService documentVersionService = createService();

        when(documentVersionRepository.findById(123)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> documentVersionService.submitForReview(123));

        verify(documentVisibilityService, never()).getCurrentAuthenticatedUser();
        verify(documentVersionRepository, never()).save(any());
    }

    @Test
    void publishVersion_publishesApprovedVersionAndDemotesCurrentPublished() {
        DocumentVersionService documentVersionService = createService();

        User reviewer = createUser(2, "reviewer.local", "REVIEWER");

        Document document = new Document();
        document.setId(50);
        document.setTitle("Spec");
        document.setStatus(DocumentStatus.ACTIVE);

        DocumentVersion currentPublished = new DocumentVersion();
        currentPublished.setId(5);
        currentPublished.setDocument(document);
        currentPublished.setVersionNumber(2);
        currentPublished.setStatus(VersionStatus.PUBLISHED);

        DocumentVersion versionToPublish = new DocumentVersion();
        versionToPublish.setId(6);
        versionToPublish.setDocument(document);
        versionToPublish.setVersionNumber(3);
        versionToPublish.setStatus(VersionStatus.APPROVED);

        document.setPublishedVersion(currentPublished);

        when(documentVersionRepository.findById(6)).thenReturn(Optional.of(versionToPublish));
        when(documentVisibilityService.getCurrentAuthenticatedUser()).thenReturn(reviewer);
        when(documentVersionRepository.save(any(DocumentVersion.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PublishDocumentResponseDto response = documentVersionService.publishVersion(6);

        assertNotNull(response);
        assertEquals(50, response.getDocumentId());
        assertEquals("Spec", response.getTitle());
        assertEquals("ACTIVE", response.getStatus());
        assertEquals(6, response.getPublishedVersionId());
        assertEquals(3, response.getPublishedVersionNumber());

        assertEquals(VersionStatus.APPROVED, currentPublished.getStatus());
        assertEquals(VersionStatus.PUBLISHED, versionToPublish.getStatus());
        assertEquals(versionToPublish, document.getPublishedVersion());

        verify(auditLogService).log(
                eq(AuditActionType.VERSION_PUBLISHED),
                eq("DOCUMENT_VERSION"),
                eq(6),
                eq("reviewer.local"),
                contains("Published version 3 for document 50")
        );
    }

    @Test
    void publishVersion_throwsWhenVersionIsNotApproved() {
        DocumentVersionService documentVersionService = createService();

        Document document = new Document();
        document.setId(50);
        document.setStatus(DocumentStatus.ACTIVE);

        DocumentVersion version = new DocumentVersion();
        version.setId(6);
        version.setDocument(document);
        version.setVersionNumber(3);
        version.setStatus(VersionStatus.DRAFT);

        when(documentVersionRepository.findById(6)).thenReturn(Optional.of(version));

        assertThrows(BusinessRuleViolationException.class, () -> documentVersionService.publishVersion(6));

        verify(documentVersionRepository, never()).save(any());
        verify(documentRepository, never()).save(any());
        verify(auditLogService, never()).log(any(), any(), any(), any(), any());
    }

    @Test
    void publishVersion_throwsWhenVersionNotFound() {
        DocumentVersionService documentVersionService = createService();

        when(documentVersionRepository.findById(404)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> documentVersionService.publishVersion(404));

        verify(documentVersionRepository, never()).save(any());
        verify(documentRepository, never()).save(any());
        verify(auditLogService, never()).log(any(), any(), any(), any(), any());
    }

    @Test
    void rollbackVersion_allowsPublishedTargetVersion() {
        DocumentVersionService documentVersionService = createService();

        User reviewer = createUser(2, "reviewer.local", "REVIEWER");

        Document document = new Document();
        document.setId(50);
        document.setTitle("Spec");
        document.setStatus(DocumentStatus.ACTIVE);

        DocumentVersion currentPublished = new DocumentVersion();
        currentPublished.setId(8);
        currentPublished.setDocument(document);
        currentPublished.setVersionNumber(4);
        currentPublished.setStatus(VersionStatus.PUBLISHED);

        DocumentVersion rollbackTarget = new DocumentVersion();
        rollbackTarget.setId(7);
        rollbackTarget.setDocument(document);
        rollbackTarget.setVersionNumber(2);
        rollbackTarget.setStatus(VersionStatus.APPROVED);

        document.setPublishedVersion(currentPublished);

        when(documentVersionRepository.findById(7)).thenReturn(Optional.of(rollbackTarget));
        when(documentVisibilityService.getCurrentAuthenticatedUser()).thenReturn(reviewer);
        when(documentVersionRepository.save(any(DocumentVersion.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PublishDocumentResponseDto response = documentVersionService.rollbackVersion(7);

        assertEquals(50, response.getDocumentId());
        assertEquals(7, response.getPublishedVersionId());
        assertEquals(2, response.getPublishedVersionNumber());
        assertEquals(VersionStatus.APPROVED, currentPublished.getStatus());
        assertEquals(VersionStatus.PUBLISHED, rollbackTarget.getStatus());

        verify(auditLogService).log(
                eq(AuditActionType.VERSION_ROLLED_BACK),
                eq("DOCUMENT_VERSION"),
                eq(7),
                eq("reviewer.local"),
                contains("Rolled back to version 2 for document 50")
        );
    }

    @Test
    void rollbackVersion_throwsWhenTargetStatusIsInvalid() {
        DocumentVersionService documentVersionService = createService();

        Document document = new Document();
        document.setId(50);
        document.setStatus(DocumentStatus.ACTIVE);

        DocumentVersion version = new DocumentVersion();
        version.setId(7);
        version.setDocument(document);
        version.setVersionNumber(2);
        version.setStatus(VersionStatus.DRAFT);

        when(documentVersionRepository.findById(7)).thenReturn(Optional.of(version));

        assertThrows(BusinessRuleViolationException.class, () -> documentVersionService.rollbackVersion(7));

        verify(documentVersionRepository, never()).save(any());
        verify(documentRepository, never()).save(any());
        verify(auditLogService, never()).log(any(), any(), any(), any(), any());
    }

    @Test
    void rollbackVersion_throwsWhenVersionNotFound() {
        DocumentVersionService documentVersionService = createService();

        when(documentVersionRepository.findById(404)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> documentVersionService.rollbackVersion(404));

        verify(documentVersionRepository, never()).save(any());
        verify(documentRepository, never()).save(any());
        verify(auditLogService, never()).log(any(), any(), any(), any(), any());
    }

    private Document createDocument(Integer id, String title, User owner, DocumentStatus status) {
        Document document = new Document();
        document.setId(id);
        document.setTitle(title);
        document.setOwner(owner);
        document.setStatus(status);
        return document;
    }

    private DocumentVersion createVersion(
            Integer id,
            Document document,
            Integer versionNumber,
            VersionStatus status,
            String content
    ) {
        DocumentVersion version = new DocumentVersion();
        version.setId(id);
        version.setDocument(document);
        version.setVersionNumber(versionNumber);
        version.setStatus(status);
        version.setContent(content);
        return version;
    }
}