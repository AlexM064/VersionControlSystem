package com.sap.vcs.server.service;

import com.sap.vcs.server.dto.CompareVersionsResponseDto;
import com.sap.vcs.server.entity.Document;
import com.sap.vcs.server.entity.DocumentVersion;
import com.sap.vcs.server.exception.BusinessRuleViolationException;
import com.sap.vcs.server.repository.ApprovalRepository;
import com.sap.vcs.server.repository.DocumentRepository;
import com.sap.vcs.server.repository.DocumentVersionRepository;
import com.sap.vcs.server.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentVersionServiceTest {

    @Mock
    private DocumentVersionRepository documentVersionRepository;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private ApprovalRepository approvalRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DocumentVersionService documentVersionService;

    @Test
    void compareVersions_returnsExpectedPayloadForSameDocument() {
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

        when(documentVersionRepository.findById(10)).thenReturn(Optional.of(leftVersion));
        when(documentVersionRepository.findById(11)).thenReturn(Optional.of(rightVersion));

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
}
