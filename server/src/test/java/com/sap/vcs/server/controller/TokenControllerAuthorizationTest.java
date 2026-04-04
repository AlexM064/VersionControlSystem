package com.sap.vcs.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.vcs.server.dto.auth.AuthResponseDto;
import com.sap.vcs.server.dto.auth.LoginRequestDto;
import com.sap.vcs.server.security.CustomUserDetailsService;
import com.sap.vcs.server.security.RestAccessDeniedHandler;
import com.sap.vcs.server.security.RestAuthenticationEntryPoint;
import com.sap.vcs.server.security.SecurityConfig;
import com.sap.vcs.server.security.jwt.JwtAuthenticationFilter;
import com.sap.vcs.server.security.jwt.JwtService;
import com.sap.vcs.server.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TokenController.class)
@Import({
        SecurityConfig.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class
})
class TokenControllerAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

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
    void issueToken_isPublic() throws Exception {
        LoginRequestDto request = new LoginRequestDto();
        request.setUsername("author-user");
        request.setPassword("password123");

        when(authService.login(any(LoginRequestDto.class)))
                .thenReturn(new AuthResponseDto(
                        "jwt-token",
                        "author-user",
                        java.util.List.of("AUTHOR")
                ));

        mockMvc.perform(post("/api/v1/tokens")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.username").value("author-user"))
                .andExpect(jsonPath("$.roles[0]").value("AUTHOR"));
    }
}