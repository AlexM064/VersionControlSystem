package com.sap.vcs.server.service;

import com.sap.vcs.server.entity.Document;
import com.sap.vcs.server.entity.Role;
import com.sap.vcs.server.entity.User;
import com.sap.vcs.server.entity.enums.DocumentStatus;
import com.sap.vcs.server.exception.ResourceNotFoundException;
import com.sap.vcs.server.repository.DocumentRepository;
import com.sap.vcs.server.repository.DocumentVersionRepository;
import com.sap.vcs.server.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentVersionRepository documentVersionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DocumentService documentService;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        SecurityContextHolder.clearContext();
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

        when(userRepository.findByUsername("author.local"))
                .thenReturn(Optional.of(author));
        when(documentRepository.findById(1))
                .thenReturn(Optional.of(document));
        when(documentRepository.save(document))
                .thenReturn(document);

        documentService.archiveDocument(1);

        ArgumentCaptor<Document> captor = ArgumentCaptor.forClass(Document.class);
        verify(documentRepository).save(captor.capture());

        Document saved = captor.getValue();
        assertEquals(DocumentStatus.ARCHIVED, saved.getStatus());
    }

    @Test
    void archiveDocument_shouldThrowWhenDocumentNotFound() {
        User author = createUser(1, "author.local", "AUTHOR");
        setAuthenticatedUser("author.local");

        when(userRepository.findByUsername("author.local"))
                .thenReturn(Optional.of(author));
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
}