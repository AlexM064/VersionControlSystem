package com.sap.vcs.server.security;

import com.sap.vcs.server.controller.DocumentController;
import com.sap.vcs.server.dto.DocumentResponseDto;
import com.sap.vcs.server.security.jwt.JwtAuthenticationFilter;
import com.sap.vcs.server.security.jwt.JwtService;
import com.sap.vcs.server.service.DocumentService;
import com.sap.vcs.server.service.DocumentVersionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DocumentController.class)
@Import({
        SecurityConfig.class,
        CustomUserDetailsService.class,
        JwtAuthenticationFilter.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class
})
class SecurityAuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private com.sap.vcs.server.repository.UserRepository userRepository;

    @MockBean
    private DocumentService documentService;

    @MockBean
    private DocumentVersionService documentVersionService;

    @MockBean
    private JwtService jwtService;

    @Test
    void validCredentialsAuthenticateSuccessfully() throws Exception {
        when(jwtService.extractUsername("valid-token"))
                .thenReturn("author-user");
        when(jwtService.extractRoles("valid-token"))
                .thenReturn(List.of("AUTHOR"));
        when(jwtService.extractEnabled("valid-token"))
                .thenReturn(true);
        when(jwtService.isValid("valid-token"))
                .thenReturn(true);

        when(documentService.getAllDocuments(eq(null), eq(null), eq(null)))
                .thenReturn(new PageImpl<>(
                        List.of(
                                new DocumentResponseDto(
                                        1,
                                        "Spec",
                                        "Description",
                                        "DRAFT",
                                        null,
                                        "author.local",
                                        LocalDateTime.now(),
                                        LocalDateTime.now()
                                )
                        ),
                        PageRequest.of(0, 10),
                        1
                ));

        mockMvc.perform(get("/documents").header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk());
    }

    @Test
    void invalidTokenIsRejected() throws Exception {
        when(jwtService.extractUsername("invalid-token"))
                .thenThrow(new RuntimeException("Invalid token"));

        mockMvc.perform(get("/documents").header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void inactiveUserIsRejected() throws Exception {
        when(jwtService.extractUsername("inactive-token"))
                .thenReturn("inactive-author");
        when(jwtService.extractRoles("inactive-token"))
                .thenReturn(List.of("AUTHOR"));
        when(jwtService.extractEnabled("inactive-token"))
                .thenReturn(false);
        when(jwtService.isValid("inactive-token"))
                .thenReturn(true);

        mockMvc.perform(get("/documents").header("Authorization", "Bearer inactive-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedUserWithoutRequiredRoleGetsForbidden() throws Exception {
        when(jwtService.extractUsername("reader-token"))
                .thenReturn("reader-user");
        when(jwtService.extractRoles("reader-token"))
                .thenReturn(List.of("READER"));
        when(jwtService.extractEnabled("reader-token"))
                .thenReturn(true);
        when(jwtService.isValid("reader-token"))
                .thenReturn(true);

        mockMvc.perform(get("/documents").header("Authorization", "Bearer reader-token"))
                .andExpect(status().isForbidden());
    }
}