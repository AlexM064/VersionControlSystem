package com.sap.vcs.server.service;

import com.sap.vcs.server.dto.DocumentRequestDto;
import com.sap.vcs.server.dto.DocumentVersionRequestDto;
import com.sap.vcs.server.dto.DocumentVersionResponseDto;
import com.sap.vcs.server.dto.UpdateDocumentMetadataRequestDto;
import com.sap.vcs.server.entity.*;
import com.sap.vcs.server.entity.enums.ApprovalDecision;
import com.sap.vcs.server.entity.enums.DocumentStatus;
import com.sap.vcs.server.entity.enums.VersionStatus;
import com.sap.vcs.server.repository.ApprovalRepository;
import com.sap.vcs.server.repository.DocumentRepository;
import com.sap.vcs.server.repository.DocumentVersionRepository;
import com.sap.vcs.server.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringJUnitConfig(ServiceMethodSecurityTest.MethodSecurityTestConfig.class)
@TestExecutionListeners(
        listeners = WithSecurityContextTestExecutionListener.class,
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
class ServiceMethodSecurityTest {

    @Configuration
    @EnableMethodSecurity
    static class MethodSecurityTestConfig {

        private static final DocumentRepository DOCUMENT_REPOSITORY = Mockito.mock(DocumentRepository.class);
        private static final DocumentVersionRepository DOCUMENT_VERSION_REPOSITORY = Mockito.mock(DocumentVersionRepository.class);
        private static final ApprovalRepository APPROVAL_REPOSITORY = Mockito.mock(ApprovalRepository.class);
        private static final UserRepository USER_REPOSITORY = Mockito.mock(UserRepository.class);
        private static final AuditLogService AUDIT_LOG_SERVICE = Mockito.mock(AuditLogService.class);

        @Bean
        DocumentRepository documentRepository() {
            return DOCUMENT_REPOSITORY;
        }

        @Bean
        DocumentVersionRepository documentVersionRepository() {
            return DOCUMENT_VERSION_REPOSITORY;
        }

        @Bean
        ApprovalRepository approvalRepository() {
            return APPROVAL_REPOSITORY;
        }

        @Bean
        UserRepository userRepository() {
            return USER_REPOSITORY;
        }

        @Bean
        AuditLogService auditLogService() {
            return AUDIT_LOG_SERVICE;
        }

        @Bean
        AuthenticatedUserService authenticatedUserService(UserRepository userRepository) {
            return new AuthenticatedUserService(userRepository);
        }

        @Bean
        DocumentService documentService(
                DocumentRepository documentRepository,
                DocumentVersionRepository documentVersionRepository,
                UserRepository userRepository,
                AuditLogService auditLogService
        ) {
            return new DocumentService(
                    documentRepository,
                    documentVersionRepository,
                    userRepository,
                    auditLogService
            );
        }

        @Bean
        DocumentVersionService documentVersionService(
                DocumentVersionRepository documentVersionRepository,
                DocumentRepository documentRepository,
                ApprovalRepository approvalRepository,
                UserRepository userRepository,
                AuditLogService auditLogService
        ) {
            return new DocumentVersionService(
                    documentVersionRepository,
                    documentRepository,
                    userRepository,
                    auditLogService
            );
        }

        @Bean
        ApprovalService approvalService(
                ApprovalRepository approvalRepository,
                DocumentVersionRepository documentVersionRepository,
                AuditLogService auditLogService,
                AuthenticatedUserService authenticatedUserService
        ) {
            return new ApprovalService(
                    approvalRepository,
                    documentVersionRepository,
                    auditLogService,
                    authenticatedUserService
            );
        }
    }

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentVersionService documentVersionService;

    @Autowired
    private ApprovalService approvalService;

    @BeforeEach
    void resetMocks() {
        reset(
                MethodSecurityTestConfig.DOCUMENT_REPOSITORY,
                MethodSecurityTestConfig.DOCUMENT_VERSION_REPOSITORY,
                MethodSecurityTestConfig.APPROVAL_REPOSITORY,
                MethodSecurityTestConfig.USER_REPOSITORY,
                MethodSecurityTestConfig.AUDIT_LOG_SERVICE
        );
    }

    @Test
    @WithMockUser(username = "author.local", roles = "AUTHOR")
    void createDocument_allowsAuthor() {
        DocumentRequestDto request = new DocumentRequestDto();
        request.setTitle("Spec");
        request.setDescription("Description");

        User author = createUser(1, "author.local", "AUTHOR");

        Document saved = new Document();
        saved.setId(1);
        saved.setTitle("Spec");
        saved.setDescription("Description");
        saved.setStatus(DocumentStatus.ACTIVE);
        saved.setOwner(author);

        when(MethodSecurityTestConfig.USER_REPOSITORY.findByUsername("author.local"))
                .thenReturn(Optional.of(author));
        when(MethodSecurityTestConfig.DOCUMENT_REPOSITORY.save(any(Document.class)))
                .thenReturn(saved);

        assertDoesNotThrow(() -> documentService.createDocument(request));
    }

    @Test
    @WithMockUser(roles = "REVIEWER")
    void createDocument_forbidsReviewer() {
        DocumentRequestDto request = new DocumentRequestDto();
        request.setTitle("Spec");
        request.setDescription("Description");

        assertThrows(AccessDeniedException.class, () -> documentService.createDocument(request));
    }

    @Test
    @WithMockUser(username = "author.local", roles = "AUTHOR")
    void createVersion_allowsAuthor() {
        User author = createUser(101, "author.local", "AUTHOR");

        Document document = new Document();
        document.setId(1);
        document.setTitle("Spec");
        document.setStatus(DocumentStatus.ACTIVE);
        document.setOwner(author);

        DocumentVersionRequestDto request = new DocumentVersionRequestDto();
        request.setContent("Content");
        request.setMessage("Initial");

        DocumentVersion savedVersion = new DocumentVersion();
        savedVersion.setId(10);
        savedVersion.setDocument(document);
        savedVersion.setVersionNumber(1);
        savedVersion.setContent("Content");
        savedVersion.setMessage("Initial");
        savedVersion.setStatus(VersionStatus.DRAFT);
        savedVersion.setCreatedBy(author);

        when(MethodSecurityTestConfig.USER_REPOSITORY.findByUsername("author.local"))
                .thenReturn(Optional.of(author));
        when(MethodSecurityTestConfig.DOCUMENT_REPOSITORY.findById(1))
                .thenReturn(Optional.of(document));
        when(MethodSecurityTestConfig.DOCUMENT_VERSION_REPOSITORY.findTopByDocumentOrderByVersionNumberDesc(document))
                .thenReturn(Optional.empty());
        when(MethodSecurityTestConfig.DOCUMENT_VERSION_REPOSITORY.save(any(DocumentVersion.class)))
                .thenReturn(savedVersion);

        DocumentVersionResponseDto response = documentVersionService.createVersion(1, request);

        ArgumentCaptor<DocumentVersion> captor = ArgumentCaptor.forClass(DocumentVersion.class);
        verify(MethodSecurityTestConfig.DOCUMENT_VERSION_REPOSITORY).save(captor.capture());

        DocumentVersion capturedVersion = captor.getValue();
        assertNotNull(capturedVersion);
        assertEquals(document, capturedVersion.getDocument());
        assertEquals(1, capturedVersion.getVersionNumber());
        assertEquals(author, capturedVersion.getCreatedBy());
        assertEquals("author.local", response.getCreatedByUsername());
    }

    @Test
    @WithMockUser(roles = "REVIEWER")
    void createVersion_forbidsReviewer() {
        DocumentVersionRequestDto request = new DocumentVersionRequestDto();
        request.setContent("Content");
        request.setMessage("Initial");

        assertThrows(AccessDeniedException.class, () -> documentVersionService.createVersion(1, request));
    }

    @Test
    @WithMockUser(username = "reviewer.local", roles = "REVIEWER")
    void approve_allowsReviewer() {
        DocumentVersion version = new DocumentVersion();
        version.setId(1);
        version.setStatus(VersionStatus.IN_REVIEW);
        version.setVersionNumber(1);

        Document document = new Document();
        document.setId(5);
        version.setDocument(document);

        User reviewer = createUser(100, "reviewer.local", "REVIEWER");

        Approval approval = new Approval();
        approval.setId(10);
        approval.setVersion(version);
        approval.setReviewer(reviewer);
        approval.setDecision(ApprovalDecision.APPROVED);

        when(MethodSecurityTestConfig.DOCUMENT_VERSION_REPOSITORY.findById(1))
                .thenReturn(Optional.of(version));
        when(MethodSecurityTestConfig.USER_REPOSITORY.findByUsername("reviewer.local"))
                .thenReturn(Optional.of(reviewer));
        when(MethodSecurityTestConfig.APPROVAL_REPOSITORY.findByVersionAndReviewer(version, reviewer))
                .thenReturn(Optional.empty());
        when(MethodSecurityTestConfig.APPROVAL_REPOSITORY.save(any(Approval.class)))
                .thenReturn(approval);

        assertDoesNotThrow(() -> approvalService.approve(1));
    }

    @Test
    @WithMockUser(roles = "AUTHOR")
    void approve_forbidsAuthor() {
        assertThrows(AccessDeniedException.class, () -> approvalService.approve(1));
    }

    @Test
    @WithMockUser(username = "admin.local", roles = "ADMIN")
    void reject_allowsAdmin() {
        DocumentVersion version = new DocumentVersion();
        version.setId(1);
        version.setStatus(VersionStatus.IN_REVIEW);
        version.setVersionNumber(1);

        Document document = new Document();
        document.setId(5);
        version.setDocument(document);

        User reviewer = createUser(100, "admin.local", "ADMIN");

        Approval approval = new Approval();
        approval.setId(11);
        approval.setVersion(version);
        approval.setReviewer(reviewer);
        approval.setDecision(ApprovalDecision.REJECTED);

        when(MethodSecurityTestConfig.DOCUMENT_VERSION_REPOSITORY.findById(1))
                .thenReturn(Optional.of(version));
        when(MethodSecurityTestConfig.USER_REPOSITORY.findByUsername("admin.local"))
                .thenReturn(Optional.of(reviewer));
        when(MethodSecurityTestConfig.APPROVAL_REPOSITORY.findByVersionAndReviewer(version, reviewer))
                .thenReturn(Optional.empty());
        when(MethodSecurityTestConfig.APPROVAL_REPOSITORY.save(any(Approval.class)))
                .thenReturn(approval);

        assertDoesNotThrow(() -> approvalService.reject(1));
    }

    @Test
    @WithMockUser(roles = "READER")
    void reject_forbidsReader() {
        assertThrows(AccessDeniedException.class, () -> approvalService.reject(1));
    }

    @Test
    @WithMockUser(username = "reviewer.local", roles = "REVIEWER")
    void publishVersion_allowsReviewer() {
        User reviewer = createUser(1, "reviewer.local", "REVIEWER");

        Document document = new Document();
        document.setId(1);
        document.setTitle("Spec");
        document.setStatus(DocumentStatus.ACTIVE);

        DocumentVersion version = new DocumentVersion();
        version.setId(10);
        version.setDocument(document);
        version.setVersionNumber(2);
        version.setStatus(VersionStatus.APPROVED);

        when(MethodSecurityTestConfig.USER_REPOSITORY.findByUsername("reviewer.local"))
                .thenReturn(Optional.of(reviewer));
        when(MethodSecurityTestConfig.DOCUMENT_VERSION_REPOSITORY.findById(10))
                .thenReturn(Optional.of(version));
        when(MethodSecurityTestConfig.DOCUMENT_VERSION_REPOSITORY.save(any(DocumentVersion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(MethodSecurityTestConfig.DOCUMENT_REPOSITORY.save(any(Document.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> documentVersionService.publishVersion(10));
    }

    @Test
    @WithMockUser(roles = "AUTHOR")
    void publishVersion_forbidsAuthor() {
        assertThrows(AccessDeniedException.class, () -> documentVersionService.publishVersion(10));
    }

    @Test
    @WithMockUser(username = "admin.local", roles = "ADMIN")
    void rollbackVersion_allowsAdmin() {
        User admin = createUser(1, "admin.local", "ADMIN");

        Document document = new Document();
        document.setId(1);
        document.setTitle("Spec");
        document.setStatus(DocumentStatus.ACTIVE);

        DocumentVersion version = new DocumentVersion();
        version.setId(10);
        version.setDocument(document);
        version.setVersionNumber(1);
        version.setStatus(VersionStatus.PUBLISHED);

        when(MethodSecurityTestConfig.USER_REPOSITORY.findByUsername("admin.local"))
                .thenReturn(Optional.of(admin));
        when(MethodSecurityTestConfig.DOCUMENT_VERSION_REPOSITORY.findById(10))
                .thenReturn(Optional.of(version));
        when(MethodSecurityTestConfig.DOCUMENT_VERSION_REPOSITORY.save(any(DocumentVersion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(MethodSecurityTestConfig.DOCUMENT_REPOSITORY.save(any(Document.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> documentVersionService.rollbackVersion(10));
    }

    @Test
    @WithMockUser(roles = "READER")
    void rollbackVersion_forbidsReader() {
        assertThrows(AccessDeniedException.class, () -> documentVersionService.rollbackVersion(10));
    }

    @Test
    @WithMockUser(roles = "REVIEWER")
    void updateDocument_forbidsReviewer() {
        UpdateDocumentMetadataRequestDto request = new UpdateDocumentMetadataRequestDto();
        request.setTitle("Updated title");
        request.setDescription("Updated description");

        assertThrows(AccessDeniedException.class, () -> documentService.updateDocument(1, request));
    }

    @Test
    @WithMockUser(username = "author.local", roles = "AUTHOR")
    void updateDocument_allowsAuthor() {
        User author = createUser(1, "author.local", "AUTHOR");

        Document existing = new Document();
        existing.setId(1);
        existing.setTitle("Old title");
        existing.setDescription("Old description");
        existing.setStatus(DocumentStatus.ACTIVE);
        existing.setOwner(author);

        UpdateDocumentMetadataRequestDto request = new UpdateDocumentMetadataRequestDto();
        request.setTitle("Updated title");
        request.setDescription("Updated description");

        when(MethodSecurityTestConfig.USER_REPOSITORY.findByUsername("author.local"))
                .thenReturn(Optional.of(author));
        when(MethodSecurityTestConfig.DOCUMENT_REPOSITORY.findById(1))
                .thenReturn(Optional.of(existing));
        when(MethodSecurityTestConfig.DOCUMENT_REPOSITORY.save(any(Document.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = documentService.updateDocument(1, request);

        assertEquals("Updated title", response.getTitle());
        assertEquals("Updated description", response.getDescription());
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
}