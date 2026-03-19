package com.sap.vcs.server.security;

import com.sap.vcs.server.dto.DocumentResponseDto;
import com.sap.vcs.server.controller.DocumentController;
import com.sap.vcs.server.entity.Role;
import com.sap.vcs.server.entity.User;
import com.sap.vcs.server.entity.enums.DocumentStatus;
import com.sap.vcs.server.repository.UserRepository;
import com.sap.vcs.server.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
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

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private DocumentService documentService;

    @Test
    void validCredentialsAuthenticateSuccessfully() throws Exception {
        User author = buildUser("author-user", "author-pass", true, "AUTHOR");

        Mockito.when(userRepository.findByUsername("author-user"))
                .thenReturn(Optional.of(author));
        Mockito.when(documentService.getAllDocuments(eq(null), eq(null), any()))
                .thenReturn(new PageImpl<>(
                        java.util.List.of(
                                new DocumentResponseDto(1, "Spec", "Description", DocumentStatus.DRAFT.name(), null)
                        ),
                        PageRequest.of(0, 10),
                        1
                ));

        mockMvc.perform(get("/documents").with(httpBasic("author-user", "author-pass")))
                .andExpect(status().isOk());
    }

    @Test
    void wrongPasswordIsRejected() throws Exception {
        User author = buildUser("author-user", "author-pass", true, "AUTHOR");

        Mockito.when(userRepository.findByUsername("author-user"))
                .thenReturn(Optional.of(author));

        mockMvc.perform(get("/documents").with(httpBasic("author-user", "wrong-pass")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void inactiveUserIsRejected() throws Exception {
        User inactiveAuthor = buildUser("inactive-author", "author-pass", false, "AUTHOR");

        Mockito.when(userRepository.findByUsername("inactive-author"))
                .thenReturn(Optional.of(inactiveAuthor));

        mockMvc.perform(get("/documents").with(httpBasic("inactive-author", "author-pass")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedUserWithoutRequiredRoleGetsForbidden() throws Exception {
        User reader = buildUser("reader-user", "reader-pass", true, "READER");

        Mockito.when(userRepository.findByUsername("reader-user"))
                .thenReturn(Optional.of(reader));

        mockMvc.perform(get("/documents").with(httpBasic("reader-user", "reader-pass")))
                .andExpect(status().isForbidden());
    }

    private User buildUser(String username, String rawPassword, boolean isActive, String roleName) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setIsActive(isActive);
        user.updateEncodedPassword(passwordEncoder.encode(rawPassword));

        Role role = new Role();
        role.setName(roleName);
        user.setRoles(Set.of(role));

        return user;
    }
}
