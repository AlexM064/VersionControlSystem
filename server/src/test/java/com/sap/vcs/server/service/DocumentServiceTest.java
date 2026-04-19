package com.sap.vcs.server.service;

import com.sap.vcs.server.entity.Document;
import com.sap.vcs.server.entity.Role;
import com.sap.vcs.server.entity.User;
import com.sap.vcs.server.entity.enums.DocumentStatus;
import com.sap.vcs.server.exception.ResourceNotFoundException;
import com.sap.vcs.server.repository.DocumentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;

import com.sap.vcs.server.dto.DocumentHistoryResponseDto;
import com.sap.vcs.server.dto.DocumentRequestDto;
import com.sap.vcs.server.dto.DocumentResponseDto;
import com.sap.vcs.server.dto.DocumentVersionResponseDto;
import com.sap.vcs.server.dto.UpdateDocumentMetadataRequestDto;
import com.sap.vcs.server.entity.DocumentVersion;
import com.sap.vcs.server.exception.BusinessRuleViolationException;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private DocumentVisibilityService documentVisibilityService;

    private DocumentService documentService;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        SecurityContextHolder.clearContext();

        documentService = new DocumentService(
                documentRepository,
                auditLogService,
                documentVisibilityService
        );
    }

    @AfterEach
    void tearDown() throws Exception {
        SecurityContextHolder.clearContext();
        closeable.close();
    }

    @Test
    void archiveDocument_shouldSetStatusToArchived() {
        User author = createUser(1, "author.local", "AUTHOR");
        setAuthenticatedUser("author.local");

        Document document = new Document();
        document.setId(1);
        document.setTitle("Spec");
        document.setStatus(DocumentStatus.ACTIVE);
        document.setOwner(author);

        when(documentVisibilityService.getCurrentAuthenticatedUser())
                .thenReturn(author);
        when(documentRepository.findById(1))
                .thenReturn(Optional.of(document));
        doNothing().when(documentVisibilityService)
                .validateOwnershipOrAdmin(document, author, "You do not have permission to modify this document");
        when(documentRepository.save(document))
                .thenReturn(document);

        documentService.archiveDocument(1);

        ArgumentCaptor<Document> captor = ArgumentCaptor.forClass(Document.class);
        verify(documentRepository).save(captor.capture());

        Document saved = captor.getValue();
        assertEquals(DocumentStatus.ARCHIVED, saved.getStatus());

        verify(auditLogService).log(
                eq(com.sap.vcs.server.entity.enums.AuditActionType.DOCUMENT_ARCHIVED),
                eq("DOCUMENT"),
                eq(1),
                eq("author.local"),
                eq("Document archived")
        );
    }

    @Test
    void archiveDocument_shouldThrowWhenDocumentNotFound() {
        User author = createUser(1, "author.local", "AUTHOR");
        setAuthenticatedUser("author.local");

        when(documentVisibilityService.getCurrentAuthenticatedUser())
                .thenReturn(author);
        when(documentRepository.findById(999))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> documentService.archiveDocument(999));
    }

    private void setAuthenticatedUser(String username) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, null)
        );
    }

    private User createUser(Integer id, String username, String roleName) {
        Role role = new Role();
        role.setId(1);
        role.setName(roleName);

        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setRoles(Set.of(role));
        return user;
    }

    @Test
    void createDocument_shouldSetOwnerToAuthenticatedUser() {
        User author = createUser(1, "author.local", "AUTHOR");
        setAuthenticatedUser("author.local");

        DocumentRequestDto request = new DocumentRequestDto();
        request.setTitle("Spec");
        request.setDescription("Document description");

        when(documentVisibilityService.getCurrentAuthenticatedUser())
                .thenReturn(author);
        when(documentRepository.save(any(Document.class)))
                .thenAnswer(invocation -> {
                    Document saved = invocation.getArgument(0);
                    saved.setId(10);
                    return saved;
                });

        DocumentResponseDto response = documentService.createDocument(request);

        assertNotNull(response);
        assertEquals(10, response.getId());
        assertEquals("Spec", response.getTitle());
        assertEquals("Document description", response.getDescription());
        assertEquals("author.local", response.getOwnerUsername());

        ArgumentCaptor<Document> captor = ArgumentCaptor.forClass(Document.class);
        verify(documentRepository).save(captor.capture());

        Document saved = captor.getValue();
        assertEquals(author, saved.getOwner());
        assertEquals("Spec", saved.getTitle());
        assertEquals("Document description", saved.getDescription());

        verify(auditLogService).log(
                eq(com.sap.vcs.server.entity.enums.AuditActionType.DOCUMENT_CREATED),
                eq("DOCUMENT"),
                eq(10),
                eq("author.local"),
                contains("Document created with title: Spec")
        );
    }

    @Test
    void archiveDocument_shouldThrowWhenAlreadyArchived() {
        User author = createUser(1, "author.local", "AUTHOR");
        setAuthenticatedUser("author.local");

        Document document = new Document();
        document.setId(1);
        document.setTitle("Spec");
        document.setStatus(DocumentStatus.ARCHIVED);
        document.setOwner(author);

        when(documentVisibilityService.getCurrentAuthenticatedUser())
                .thenReturn(author);
        when(documentRepository.findById(1))
                .thenReturn(Optional.of(document));

        assertThrows(BusinessRuleViolationException.class, () -> documentService.archiveDocument(1));

        verify(documentRepository, never()).save(any());
        verify(auditLogService, never()).log(any(), any(), any(), any(), any());
    }

    @Test
    void archiveDocument_shouldThrowWhenNonOwnerAuthorAttemptsArchive() {
        User owner = createUser(1, "owner.local", "AUTHOR");
        User otherAuthor = createUser(2, "other.local", "AUTHOR");

        Document document = new Document();
        document.setId(1);
        document.setTitle("Spec");
        document.setStatus(DocumentStatus.ACTIVE);
        document.setOwner(owner);

        when(documentRepository.findById(1))
                .thenReturn(Optional.of(document));
        when(documentVisibilityService.getCurrentAuthenticatedUser())
                .thenReturn(otherAuthor);

        doThrow(new AccessDeniedException("Forbidden"))
                .when(documentVisibilityService)
                .validateOwnershipOrAdmin(
                        eq(document),
                        eq(otherAuthor),
                        anyString()
                );

        assertThrows(AccessDeniedException.class, () -> documentService.archiveDocument(1));

        verify(documentRepository, never()).save(any());
        verify(auditLogService, never()).log(any(), any(), any(), any(), any());
    }

    @Test
    void updateDocument_shouldUpdateMetadataForOwner() {
        User author = createUser(1, "author.local", "AUTHOR");
        setAuthenticatedUser("author.local");

        Document document = new Document();
        document.setId(5);
        document.setTitle("Old title");
        document.setDescription("Old desc");
        document.setStatus(DocumentStatus.ACTIVE);
        document.setOwner(author);

        UpdateDocumentMetadataRequestDto request = new UpdateDocumentMetadataRequestDto();
        request.setTitle("New title");
        request.setDescription("New desc");

        when(documentVisibilityService.getCurrentAuthenticatedUser())
                .thenReturn(author);
        when(documentRepository.findById(5))
                .thenReturn(Optional.of(document));
        when(documentRepository.save(any(Document.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DocumentResponseDto response = documentService.updateDocument(5, request);

        assertEquals("New title", response.getTitle());
        assertEquals("New desc", response.getDescription());
        assertEquals("author.local", response.getOwnerUsername());

        assertEquals("New title", document.getTitle());
        assertEquals("New desc", document.getDescription());

        verify(auditLogService).log(
                eq(com.sap.vcs.server.entity.enums.AuditActionType.DOCUMENT_UPDATED),
                eq("DOCUMENT"),
                eq(5),
                eq("author.local"),
                eq("Document metadata updated")
        );
    }

    @Test
    void updateDocument_shouldThrowWhenNonOwnerAuthorAttemptsUpdate() {
        User owner = createUser(1, "owner.local", "AUTHOR");
        User otherAuthor = createUser(2, "other.local", "AUTHOR");

        Document document = new Document();
        document.setId(5);
        document.setTitle("Old title");
        document.setDescription("Old desc");
        document.setStatus(DocumentStatus.ACTIVE);
        document.setOwner(owner);

        UpdateDocumentMetadataRequestDto request = new UpdateDocumentMetadataRequestDto();
        request.setTitle("New title");
        request.setDescription("New desc");

        when(documentRepository.findById(5))
                .thenReturn(Optional.of(document));
        when(documentVisibilityService.getCurrentAuthenticatedUser())
                .thenReturn(otherAuthor);

        doThrow(new AccessDeniedException("Forbidden"))
                .when(documentVisibilityService)
                .validateOwnershipOrAdmin(
                        eq(document),
                        eq(otherAuthor),
                        anyString()
                );

        assertThrows(AccessDeniedException.class, () -> documentService.updateDocument(5, request));

        verify(documentRepository, never()).save(any());
        verify(auditLogService, never()).log(any(), any(), any(), any(), any());
    }

    @Test
    void getPublishedVersion_shouldReturnPublishedVersion() {
        Document document = new Document();
        document.setId(7);
        document.setTitle("Spec");
        document.setStatus(DocumentStatus.ACTIVE);

        User author = createUser(1, "author.local", "AUTHOR");

        DocumentVersion publishedVersion = new DocumentVersion();
        publishedVersion.setId(20);
        publishedVersion.setDocument(document);
        publishedVersion.setVersionNumber(3);
        publishedVersion.setContent("Published content");
        publishedVersion.setMessage("Approved version");
        publishedVersion.setStatus(com.sap.vcs.server.entity.enums.VersionStatus.PUBLISHED);
        publishedVersion.setCreatedBy(author);

        document.setPublishedVersion(publishedVersion);

        when(documentRepository.findById(7))
                .thenReturn(Optional.of(document));

        DocumentVersionResponseDto response = documentService.getPublishedVersion(7);

        assertEquals(20, response.getId());
        assertEquals(7, response.getDocumentId());
        assertEquals(3, response.getVersionNumber());
        assertEquals("Published content", response.getContent());
        assertEquals("Approved version", response.getMessage());
        assertEquals(com.sap.vcs.server.entity.enums.VersionStatus.PUBLISHED, response.getStatus());
        assertEquals("author.local", response.getCreatedByUsername());
    }

    @Test
    void getPublishedVersion_shouldThrowWhenNoPublishedVersionExists() {
        Document document = new Document();
        document.setId(7);
        document.setTitle("Spec");
        document.setStatus(DocumentStatus.ACTIVE);
        document.setPublishedVersion(null);

        when(documentRepository.findById(7))
                .thenReturn(Optional.of(document));

        assertThrows(ResourceNotFoundException.class, () -> documentService.getPublishedVersion(7));
    }

    @Test
    void getDocumentHistory_shouldMarkPublishedVersionCorrectly() {
        Document document = new Document();
        document.setId(7);
        document.setTitle("Spec");
        document.setStatus(DocumentStatus.ACTIVE);

        User owner = createUser(1, "owner.local", "AUTHOR");
        User currentUser = createUser(2, "reviewer.local", "REVIEWER");

        DocumentVersion v1 = new DocumentVersion();
        v1.setId(11);
        v1.setDocument(document);
        v1.setVersionNumber(1);
        v1.setMessage("Initial");
        v1.setCreatedBy(owner);

        DocumentVersion v2 = new DocumentVersion();
        v2.setId(12);
        v2.setDocument(document);
        v2.setVersionNumber(2);
        v2.setMessage("Updated");
        v2.setCreatedBy(owner);

        document.setPublishedVersion(v2);

        when(documentVisibilityService.getCurrentAuthenticatedUser())
                .thenReturn(currentUser);
        when(documentRepository.findById(7))
                .thenReturn(Optional.of(document));
        doNothing().when(documentVisibilityService)
                .ensureCanViewDocument(currentUser, document);
        when(documentVisibilityService.getVisibleVersionsForUser(currentUser, document))
                .thenReturn(List.of(v1, v2));

        List<DocumentHistoryResponseDto> history = documentService.getDocumentHistory(7);

        assertEquals(2, history.size());

        assertEquals(11, history.get(0).getVersionId());
        assertEquals(1, history.get(0).getVersionNumber());
        assertFalse(history.get(0).isPublished());

        assertEquals(12, history.get(1).getVersionId());
        assertEquals(2, history.get(1).getVersionNumber());
        assertTrue(history.get(1).isPublished());

        verify(documentVisibilityService).ensureCanViewDocument(currentUser, document);
    }

    @Test
    void getDocumentHistory_shouldThrowWhenDocumentNotFound() {
        User currentUser = createUser(2, "reviewer.local", "REVIEWER");

        when(documentVisibilityService.getCurrentAuthenticatedUser())
                .thenReturn(currentUser);
        when(documentRepository.findById(999))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> documentService.getDocumentHistory(999));

        verify(documentVisibilityService, never()).ensureCanViewDocument(any(), any());
        verify(documentVisibilityService, never()).getVisibleVersionsForUser(any(), any());
    }

    @Test
    void updateDocument_shouldAllowAdminEvenWhenNotOwner() {
        User owner = createUser(1, "owner.local", "AUTHOR");
        User admin = createUser(2, "admin.local", "ADMIN");
        setAuthenticatedUser("admin.local");

        Document document = new Document();
        document.setId(5);
        document.setTitle("Old title");
        document.setDescription("Old desc");
        document.setStatus(DocumentStatus.ACTIVE);
        document.setOwner(owner);

        UpdateDocumentMetadataRequestDto request = new UpdateDocumentMetadataRequestDto();
        request.setTitle("Admin title");
        request.setDescription("Admin desc");

        when(documentVisibilityService.getCurrentAuthenticatedUser())
                .thenReturn(admin);
        when(documentRepository.findById(5))
                .thenReturn(Optional.of(document));
        when(documentRepository.save(any(Document.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DocumentResponseDto response = documentService.updateDocument(5, request);

        assertEquals("Admin title", response.getTitle());
        assertEquals("Admin desc", response.getDescription());

        verify(documentVisibilityService).validateOwnershipOrAdmin(
                eq(document),
                eq(admin),
                eq("You do not have permission to modify this document")
        );
    }

    @Test
    void updateDocument_shouldThrowWhenDocumentNotFound() {
        User admin = createUser(2, "admin.local", "ADMIN");

        UpdateDocumentMetadataRequestDto request = new UpdateDocumentMetadataRequestDto();
        request.setTitle("Any");
        request.setDescription("Any");

        when(documentVisibilityService.getCurrentAuthenticatedUser())
                .thenReturn(admin);
        when(documentRepository.findById(404))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> documentService.updateDocument(404, request));

        verify(documentVisibilityService, never()).validateOwnershipOrAdmin(any(), any(), anyString());
        verify(documentRepository, never()).save(any());
        verify(auditLogService, never()).log(any(), any(), any(), any(), any());
    }
}