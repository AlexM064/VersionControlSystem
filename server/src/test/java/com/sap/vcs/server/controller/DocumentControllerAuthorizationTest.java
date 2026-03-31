package com.sap.vcs.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.vcs.server.dto.*;
import com.sap.vcs.server.exception.BusinessRuleViolationException;
import com.sap.vcs.server.exception.ResourceNotFoundException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import com.sap.vcs.server.entity.enums.VersionStatus;
import com.sap.vcs.server.security.CustomUserDetailsService;
import com.sap.vcs.server.security.RestAccessDeniedHandler;
import com.sap.vcs.server.security.RestAuthenticationEntryPoint;
import com.sap.vcs.server.security.SecurityConfig;
import com.sap.vcs.server.security.jwt.JwtAuthenticationFilter;
import com.sap.vcs.server.security.jwt.JwtService;
import com.sap.vcs.server.service.DocumentService;
import com.sap.vcs.server.service.DocumentVersionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@WebMvcTest(DocumentController.class)
@Import({
        SecurityConfig.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class
})
class DocumentControllerAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DocumentService documentService;

    @MockBean
    private DocumentVersionService documentVersionService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void letJwtFilterPassThrough() throws Exception {
        doAnswer(invocation -> {
            ServletRequest request = invocation.getArgument(0);
            ServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    @Test
    @WithMockUser(roles = "AUTHOR")
    void getDocuments_allowsAuthor() throws Exception {
        when(documentService.getAllDocuments(eq(null), eq(null), any()))
                .thenReturn(new PageImpl<>(
                        List.of(new DocumentResponseDto(1, "Spec", "Description", "DRAFT", null)),
                        PageRequest.of(0, 10),
                        1
                ));

        mockMvc.perform(get("/documents"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "READER")
    void getDocuments_forbidsReader() throws Exception {
        mockMvc.perform(get("/documents"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "AUTHOR")
    void compareVersions_allowsAuthor() throws Exception {
        CompareVersionsResponseDto response = new CompareVersionsResponseDto(
                5,
                3,
                1,
                "Това е първата тестова версия за новия документ.",
                5,
                2,
                "vtora test versiq za compare",
                false
        );

        when(documentVersionService.compareVersions(3, 5)).thenReturn(response);

        mockMvc.perform(get("/documents/compare")
                        .param("leftVersionId", "3")
                        .param("rightVersionId", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentId").value(5))
                .andExpect(jsonPath("$.leftVersionId").value(3))
                .andExpect(jsonPath("$.leftVersionNumber").value(1))
                .andExpect(jsonPath("$.rightVersionId").value(5))
                .andExpect(jsonPath("$.rightVersionNumber").value(2))
                .andExpect(jsonPath("$.identical").value(false));
    }

    @Test
    @WithMockUser(roles = "AUTHOR")
    void compareVersions_returnsNotFoundWhenVersionMissing() throws Exception {
        when(documentVersionService.compareVersions(3, 99))
                .thenThrow(new ResourceNotFoundException("Version not found with id: 99"));

        mockMvc.perform(get("/documents/compare")
                        .param("leftVersionId", "3")
                        .param("rightVersionId", "99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "AUTHOR")
    void compareVersions_returnsConflictForDifferentDocuments() throws Exception {
        when(documentVersionService.compareVersions(3, 8))
                .thenThrow(new BusinessRuleViolationException(
                        "Versions can be compared only if they belong to the same document"));

        mockMvc.perform(get("/documents/compare")
                        .param("leftVersionId", "3")
                        .param("rightVersionId", "8"))
                .andExpect(status().isConflict());
    }

    @Test
    void getDocuments_requiresAuthentication() throws Exception {
        mockMvc.perform(get("/documents"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "REVIEWER")
    void getDocumentById_allowsReviewer() throws Exception {
        when(documentService.getDocumentById(1))
                .thenReturn(new DocumentResponseDto(1, "Spec", "Description", "DRAFT", null));

        mockMvc.perform(get("/documents/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "READER")
    void getDocumentById_forbidsReader() throws Exception {
        mockMvc.perform(get("/documents/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getDocumentById_requiresAuthentication() throws Exception {
        mockMvc.perform(get("/documents/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "REVIEWER")
    void getDocumentHistory_allowsReviewer() throws Exception {
        when(documentService.getDocumentHistory(1))
                .thenReturn(List.of(
                        new DocumentHistoryResponseDto(11, 1, "Initial version", LocalDateTime.now(), false)
                ));

        mockMvc.perform(get("/documents/1/history"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "READER")
    void getDocumentHistory_forbidsReader() throws Exception {
        mockMvc.perform(get("/documents/1/history"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getDocumentHistory_requiresAuthentication() throws Exception {
        mockMvc.perform(get("/documents/1/history"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "READER")
    void getPublishedVersion_allowsReader() throws Exception {
        when(documentService.getPublishedVersion(1))
                .thenReturn(new DocumentVersionResponseDto(
                        10,
                        1,
                        2,
                        "Published content",
                        "Published version",
                        VersionStatus.PUBLISHED,
                        "author.local",
                        LocalDateTime.now()
                ));

        mockMvc.perform(get("/documents/1/published-version"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "GUEST")
    void getPublishedVersion_forbidsUnknownRole() throws Exception {
        mockMvc.perform(get("/documents/1/published-version"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getPublishedVersion_requiresAuthentication() throws Exception {
        mockMvc.perform(get("/documents/1/published-version"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "AUTHOR")
    void createDocument_allowsAuthor() throws Exception {
        DocumentRequestDto request = new DocumentRequestDto();
        request.setTitle("New document");
        request.setDescription("Description");

        when(documentService.createDocument(any(DocumentRequestDto.class)))
                .thenReturn(new DocumentResponseDto(1, "New document", "Description", "DRAFT", null,"author.local",LocalDateTime.now(),LocalDateTime.now() ));

        mockMvc.perform(post("/documents")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "REVIEWER")
    void createDocument_forbidsReviewer() throws Exception {
        DocumentRequestDto request = new DocumentRequestDto();
        request.setTitle("New document");
        request.setDescription("Description");

        mockMvc.perform(post("/documents")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createDocument_requiresAuthentication() throws Exception {
        DocumentRequestDto request = new DocumentRequestDto();
        request.setTitle("New document");
        request.setDescription("Description");

        mockMvc.perform(post("/documents")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
