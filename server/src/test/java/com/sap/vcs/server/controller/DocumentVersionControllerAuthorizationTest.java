package com.sap.vcs.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.vcs.server.dto.DocumentVersionRequestDto;
import com.sap.vcs.server.dto.DocumentVersionResponseDto;
import com.sap.vcs.server.security.CustomUserDetailsService;
import com.sap.vcs.server.security.RestAccessDeniedHandler;
import com.sap.vcs.server.security.RestAuthenticationEntryPoint;
import com.sap.vcs.server.security.SecurityConfig;
import com.sap.vcs.server.security.jwt.JwtService;
import com.sap.vcs.server.service.DocumentVersionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DocumentVersionController.class)
@Import({
        SecurityConfig.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class
})
class DocumentVersionControllerAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DocumentVersionService documentVersionService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtService jwtService;

    @Test
    @WithMockUser(roles = "REVIEWER")
    void getVersions_allowsReviewer() throws Exception {
        when(documentVersionService.getVersions(1))
                .thenReturn(List.of(
                        new DocumentVersionResponseDto(10, 1, 1, "Content", "Initial", "author.local", LocalDateTime.now())
                ));

        mockMvc.perform(get("/documents/1/versions"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "READER")
    void getVersions_forbidsReader() throws Exception {
        mockMvc.perform(get("/documents/1/versions"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getVersions_requiresAuthentication() throws Exception {
        mockMvc.perform(get("/documents/1/versions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "AUTHOR")
    void createVersion_allowsAuthor() throws Exception {
        DocumentVersionRequestDto request = new DocumentVersionRequestDto();
        request.setContent("Updated content");
        request.setMessage("Revision");

        when(documentVersionService.createVersion(eq(1), any(DocumentVersionRequestDto.class)))
                .thenReturn(new DocumentVersionResponseDto(
                        11,
                        1,
                        2,
                        "Updated content",
                        "Revision",
                        "author.local",
                        LocalDateTime.now()
                ));

        mockMvc.perform(post("/documents/1/versions")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "REVIEWER")
    void createVersion_forbidsReviewer() throws Exception {
        DocumentVersionRequestDto request = new DocumentVersionRequestDto();
        request.setContent("Updated content");
        request.setMessage("Revision");

        mockMvc.perform(post("/documents/1/versions")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createVersion_requiresAuthentication() throws Exception {
        DocumentVersionRequestDto request = new DocumentVersionRequestDto();
        request.setContent("Updated content");
        request.setMessage("Revision");

        mockMvc.perform(post("/documents/1/versions")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
