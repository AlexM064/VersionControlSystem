package com.sap.vcs.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.vcs.server.dto.UpdateUserRoleRequestDto;
import com.sap.vcs.server.dto.auth.AuthResponseDto;
import com.sap.vcs.server.dto.auth.MeResponseDto;
import com.sap.vcs.server.dto.auth.RegisterRequestDto;
import com.sap.vcs.server.security.CustomUserDetailsService;
import com.sap.vcs.server.security.RestAccessDeniedHandler;
import com.sap.vcs.server.security.RestAuthenticationEntryPoint;
import com.sap.vcs.server.security.SecurityConfig;
import com.sap.vcs.server.security.jwt.JwtAuthenticationFilter;
import com.sap.vcs.server.security.jwt.JwtService;
import com.sap.vcs.server.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({
        SecurityConfig.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class
})
class UserControllerAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

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
    void register_isPublic() throws Exception {
        RegisterRequestDto request = new RegisterRequestDto();
        request.setUsername("new-user");
        request.setEmail("new-user@example.com");
        request.setPassword("password123");

        when(userService.register(any(RegisterRequestDto.class)))
                .thenReturn(new AuthResponseDto(
                        "jwt-token",
                        "new-user",
                        java.util.List.of("READER")
                ));

        mockMvc.perform(post("/api/v1/users")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.username").value("new-user"))
                .andExpect(jsonPath("$.roles[0]").value("READER"));
    }

    @Test
    void me_requiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "author-user", roles = "AUTHOR")
    void me_allowsAuthenticatedUser() throws Exception {
        when(userService.me(eq("author-user")))
                .thenReturn(new MeResponseDto(
                        "author-user",
                        "author-user@example.com",
                        java.util.List.of("AUTHOR")
                ));

        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("author-user"))
                .andExpect(jsonPath("$.email").value("author-user@example.com"))
                .andExpect(jsonPath("$.roles[0]").value("AUTHOR"));
    }

    @Test
    @WithMockUser(username = "admin-user", roles = "ADMIN")
    void updateRole_allowsAdmin() throws Exception {
        UpdateUserRoleRequestDto request = new UpdateUserRoleRequestDto();
        request.setRole("REVIEWER");

        doNothing().when(userService).updateUserRole(5, "REVIEWER");

        mockMvc.perform(patch("/api/v1/users/5/role")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "author-user", roles = "AUTHOR")
    void updateRole_forbidsNonAdmin() throws Exception {
        UpdateUserRoleRequestDto request = new UpdateUserRoleRequestDto();
        request.setRole("REVIEWER");

        mockMvc.perform(patch("/api/v1/users/5/role")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}