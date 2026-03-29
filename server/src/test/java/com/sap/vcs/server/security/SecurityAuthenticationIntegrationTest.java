package com.sap.vcs.server.security;

import com.sap.vcs.server.dto.DocumentResponseDto;
import com.sap.vcs.server.controller.DocumentController;
import com.sap.vcs.server.entity.Role;
import com.sap.vcs.server.entity.User;
import com.sap.vcs.server.entity.enums.DocumentStatus;
import com.sap.vcs.server.repository.UserRepository;
import com.sap.vcs.server.security.jwt.JwtService;
import com.sap.vcs.server.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DocumentController.class)
@Import({
        SecurityConfig.class,
        CustomUserDetailsService.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class
})
class SecurityAuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private DocumentService documentService;

    @MockBean
    private JwtService jwtService;

    @Test
    void validCredentialsAuthenticateSuccessfully() throws Exception {
        User author = buildUser("author-user", true, "AUTHOR");

        when(jwtService.extractUsername("valid-token"))
                .thenReturn("author-user");
        when(userRepository.findByUsername("author-user"))
                .thenReturn(Optional.of(author));
        when(jwtService.isValid(eq("valid-token"), any()))
                .thenReturn(true);
        when(documentService.getAllDocuments(eq(null), eq(null), any()))
                .thenReturn(new PageImpl<>(
                        java.util.List.of(
                                new DocumentResponseDto(1,
                                        "Spec",
                                        "Description",
                                        "DRAFT",
                                        null,
                                        "author.local",
                                        LocalDateTime.now(),
                                        LocalDateTime.now())
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
        User inactiveAuthor = buildUser("inactive-author", false, "AUTHOR");

        when(jwtService.extractUsername("inactive-token"))
                .thenReturn("inactive-author");
        when(userRepository.findByUsername("inactive-author"))
                .thenReturn(Optional.of(inactiveAuthor));
        when(jwtService.isValid(eq("inactive-token"), any()))
                .thenReturn(true);

        mockMvc.perform(get("/documents").header("Authorization", "Bearer inactive-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedUserWithoutRequiredRoleGetsForbidden() throws Exception {
        User reader = buildUser("reader-user", true, "READER");

        when(jwtService.extractUsername("reader-token"))
                .thenReturn("reader-user");
        when(userRepository.findByUsername("reader-user"))
                .thenReturn(Optional.of(reader));
        when(jwtService.isValid(eq("reader-token"), any()))
                .thenReturn(true);

        mockMvc.perform(get("/documents").header("Authorization", "Bearer reader-token"))
                .andExpect(status().isForbidden());
    }

    private User buildUser(String username, boolean isActive, String roleName) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setIsActive(isActive);
        user.updateEncodedPassword("encoded-password");

        Role role = new Role();
        role.setName(roleName);
        user.setRoles(Set.of(role));

        return user;
    }
}
