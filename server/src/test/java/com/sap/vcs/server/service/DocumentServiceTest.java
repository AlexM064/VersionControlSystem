package com.sap.vcs.server.service;

import com.sap.vcs.server.entity.Document;
import com.sap.vcs.server.entity.enums.DocumentStatus;
import com.sap.vcs.server.exception.ResourceNotFoundException;
import com.sap.vcs.server.repository.DocumentRepository;
import com.sap.vcs.server.repository.DocumentVersionRepository;
import com.sap.vcs.server.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentVersionRepository documentVersionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DocumentService documentService;

    @Test
    void archiveDocument_shouldSetStatusToArchived() {
        Document document = new Document();
        document.setId(1);
        document.setTitle("Test document");
        document.setStatus(DocumentStatus.ACTIVE);

        when(documentRepository.findById(1)).thenReturn(Optional.of(document));
        when(documentRepository.save(any(Document.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        documentService.archiveDocument(1);

        assertEquals(DocumentStatus.ARCHIVED, document.getStatus());
        verify(documentRepository).save(document);
    }

    @Test
    void archiveDocument_shouldThrowWhenDocumentNotFound() {
        when(documentRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> documentService.archiveDocument(1));

        verify(documentRepository, never()).save(any());
    }
}
