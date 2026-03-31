package com.sap.vcs.server.service;

import com.sap.vcs.server.dto.DocumentRequestDto;
import com.sap.vcs.server.dto.DocumentVersionRequestDto;
import com.sap.vcs.server.dto.DocumentVersionResponseDto;
import com.sap.vcs.server.entity.Approval;
import com.sap.vcs.server.entity.Document;
import com.sap.vcs.server.entity.DocumentVersion;
import com.sap.vcs.server.entity.User;
import com.sap.vcs.server.entity.enums.ApprovalDecision;
import com.sap.vcs.server.entity.enums.DocumentStatus;
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
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

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
        DocumentService documentService(
                DocumentRepository documentRepository,
                DocumentVersionRepository documentVersionRepository,
                UserRepository userRepository

        ) {
            return new DocumentService(
                    documentRepository,
                    documentVersionRepository,
                    userRepository
            );
        }

        @Bean
        DocumentVersionService documentVersionService(
                DocumentVersionRepository documentVersionRepository,
                DocumentRepository documentRepository,
                ApprovalRepository approvalRepository,
                UserRepository userRepository
        ) {
            return new DocumentVersionService(
                    documentVersionRepository,
                    documentRepository,
                    approvalRepository,
                    userRepository
            );
        }

    @Bean
        ApprovalService approvalService(
                ApprovalRepository approvalRepository,
                DocumentVersionRepository documentVersionRepository,
                UserRepository userRepository
        ) {
            return new ApprovalService(approvalRepository, documentVersionRepository, userRepository);
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
                MethodSecurityTestConfig.USER_REPOSITORY
        );
    }

    @Test
    @WithMockUser(roles = "AUTHOR")
    void createDocument_allowsAuthor() {
        DocumentRequestDto request = new DocumentRequestDto();
        request.setTitle("Spec");
        request.setDescription("Description");

        Document saved = new Document();
        saved.setId(1);
        saved.setTitle("Spec");
        saved.setDescription("Description");
        saved.setStatus(DocumentStatus.ACTIVE);
        saved.setOwner(author);

        when(MethodSecurityTestConfig.DOCUMENT_REPOSITORY.save(any(Document.class))).thenReturn(saved);

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
    @WithMockUser(roles = "AUTHOR")
    void createVersion_allowsAuthor() {
        Document document = new Document();
        document.setId(1);
        document.setTitle("Spec");

        User author = new User();
        author.setId(101);
        author.setUsername("user");

        DocumentVersionRequestDto request = new DocumentVersionRequestDto();
        request.setContent("Content");
        request.setMessage("Initial");

        DocumentVersion savedVersion = new DocumentVersion();
        savedVersion.setId(10);
        savedVersion.setDocument(document);
        savedVersion.setVersionNumber(1);
        savedVersion.setContent("Content");
        savedVersion.setMessage("Initial");
        savedVersion.setCreatedBy(author);

        when(MethodSecurityTestConfig.DOCUMENT_REPOSITORY.findById(1)).thenReturn(Optional.of(document));
        when(MethodSecurityTestConfig.USER_REPOSITORY.findByUsername("user")).thenReturn(Optional.of(author));
        when(MethodSecurityTestConfig.DOCUMENT_VERSION_REPOSITORY.findTopByDocumentOrderByVersionNumberDesc(document))
                .thenReturn(Optional.empty());
        when(MethodSecurityTestConfig.DOCUMENT_VERSION_REPOSITORY.save(any(DocumentVersion.class))).thenReturn(savedVersion);

        DocumentVersionResponseDto response = documentVersionService.createVersion(1, request);

        ArgumentCaptor<DocumentVersion> captor = ArgumentCaptor.forClass(DocumentVersion.class);
        verify(MethodSecurityTestConfig.DOCUMENT_VERSION_REPOSITORY).save(captor.capture());

        DocumentVersion capturedVersion = captor.getValue();
        assertNotNull(capturedVersion);
        assertEquals(document, capturedVersion.getDocument());
        assertEquals(1, capturedVersion.getVersionNumber());
        assertEquals(author, capturedVersion.getCreatedBy());
        assertEquals("user", response.getCreatedByUsername());
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
    @WithMockUser(roles = "REVIEWER")
    void approve_allowsReviewer() {
        DocumentVersion version = new DocumentVersion();
        version.setId(1);

        User reviewer = new User();
        reviewer.setId(100);
        reviewer.setUsername("reviewer.local");

        Approval approval = new Approval();
        approval.setId(10);
        approval.setVersion(version);
        approval.setReviewer(reviewer);
        approval.setDecision(ApprovalDecision.APPROVED);

        when(MethodSecurityTestConfig.DOCUMENT_VERSION_REPOSITORY.findById(1)).thenReturn(Optional.of(version));
        when(MethodSecurityTestConfig.USER_REPOSITORY.findById(100)).thenReturn(Optional.of(reviewer));
        when(MethodSecurityTestConfig.APPROVAL_REPOSITORY.findByVersionAndReviewer(version, reviewer)).thenReturn(Optional.empty());
        when(MethodSecurityTestConfig.APPROVAL_REPOSITORY.save(any(Approval.class))).thenReturn(approval);

        assertDoesNotThrow(() -> approvalService.approve(1));
    }

    @Test
    @WithMockUser(roles = "AUTHOR")
    void approve_forbidsAuthor() {
        assertThrows(AccessDeniedException.class, () -> approvalService.approve(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void reject_allowsAdmin() {
        DocumentVersion version = new DocumentVersion();
        version.setId(1);

        User reviewer = new User();
        reviewer.setId(100);
        reviewer.setUsername("reviewer.local");

        Approval approval = new Approval();
        approval.setId(11);
        approval.setVersion(version);
        approval.setReviewer(reviewer);
        approval.setDecision(ApprovalDecision.REJECTED);

        when(MethodSecurityTestConfig.DOCUMENT_VERSION_REPOSITORY.findById(1)).thenReturn(Optional.of(version));
        when(MethodSecurityTestConfig.USER_REPOSITORY.findById(100)).thenReturn(Optional.of(reviewer));
        when(MethodSecurityTestConfig.APPROVAL_REPOSITORY.findByVersionAndReviewer(version, reviewer)).thenReturn(Optional.empty());
        when(MethodSecurityTestConfig.APPROVAL_REPOSITORY.save(any(Approval.class))).thenReturn(approval);

        assertDoesNotThrow(() -> approvalService.reject(1));
    }

    @Test
    @WithMockUser(roles = "READER")
    void reject_forbidsReader() {
        assertThrows(AccessDeniedException.class, () -> approvalService.reject(1));
    }

    @Test
    @WithMockUser(roles = "REVIEWER")
    void publishVersion_allowsReviewer() {
        Document document = new Document();
        document.setId(1);
        document.setTitle("Spec");
        document.setStatus(DocumentStatus.ACTIVE);

        DocumentVersion version = new DocumentVersion();
        version.setId(10);
        version.setDocument(document);
        version.setVersionNumber(2);

        when(MethodSecurityTestConfig.DOCUMENT_VERSION_REPOSITORY.findById(10)).thenReturn(Optional.of(version));
        when(MethodSecurityTestConfig.APPROVAL_REPOSITORY.existsByVersionAndDecision(version, ApprovalDecision.APPROVED)).thenReturn(true);
        when(MethodSecurityTestConfig.APPROVAL_REPOSITORY.existsByVersionAndDecision(version, ApprovalDecision.REJECTED)).thenReturn(false);
        when(MethodSecurityTestConfig.DOCUMENT_REPOSITORY.save(any(Document.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> documentVersionService.publishVersion(10));
    }

    @Test
    @WithMockUser(roles = "AUTHOR")
    void publishVersion_forbidsAuthor() {
        assertThrows(AccessDeniedException.class, () -> documentVersionService.publishVersion(10));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rollbackVersion_allowsAdmin() {
        Document document = new Document();
        document.setId(1);
        document.setTitle("Spec");
        document.setStatus(DocumentStatus.ACTIVE);

        DocumentVersion version = new DocumentVersion();
        version.setId(10);
        version.setDocument(document);
        version.setVersionNumber(1);
        version.setStatus(VersionStatus.APPROVED);

        when(MethodSecurityTestConfig.DOCUMENT_VERSION_REPOSITORY.findById(10)).thenReturn(Optional.of(version));
        when(MethodSecurityTestConfig.DOCUMENT_REPOSITORY.save(any(Document.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> documentVersionService.rollbackVersion(10));
    }

    @Test
    @WithMockUser(roles = "READER")
    void rollbackVersion_forbidsReader() {
        assertThrows(AccessDeniedException.class, () -> documentVersionService.rollbackVersion(10));
    }
}
