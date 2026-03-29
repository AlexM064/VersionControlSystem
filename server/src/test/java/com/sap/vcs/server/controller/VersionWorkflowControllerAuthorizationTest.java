package com.sap.vcs.server.controller;

import com.sap.vcs.server.dto.PublishDocumentResponseDto;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VersionWorkflowController.class)
@Import({
        SecurityConfig.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class
})
class VersionWorkflowControllerAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentVersionService documentVersionService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtService jwtService;

    @Test
    @WithMockUser(roles = "REVIEWER")
    void publish_allowsReviewer() throws Exception {
        when(documentVersionService.publishVersion(1))
                .thenReturn(new PublishDocumentResponseDto(
                        1,
                        "Spec",
                        "PUBLISHED",
                        10,
                        2
                ));

        mockMvc.perform(post("/versions/1/publish"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "AUTHOR")
    void publish_forbidsAuthor() throws Exception {
        mockMvc.perform(post("/versions/1/publish"))
                .andExpect(status().isForbidden());
    }

    @Test
    void publish_requiresAuthentication() throws Exception {
        mockMvc.perform(post("/versions/1/publish"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rollback_allowsAdmin() throws Exception {
        when(documentVersionService.rollbackVersion(1))
                .thenReturn(new PublishDocumentResponseDto(
                        1,
                        "Spec",
                        "PUBLISHED",
                        9,
                        1
                ));

        mockMvc.perform(post("/versions/1/rollback"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "READER")
    void rollback_forbidsReader() throws Exception {
        mockMvc.perform(post("/versions/1/rollback"))
                .andExpect(status().isForbidden());
    }

    @Test
    void rollback_requiresAuthentication() throws Exception {
        mockMvc.perform(post("/versions/1/rollback"))
                .andExpect(status().isUnauthorized());
    }
}
