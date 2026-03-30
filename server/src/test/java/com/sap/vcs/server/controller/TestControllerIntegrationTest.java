package com.sap.vcs.server.controller;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TestController.class)
@Import({
        SecurityConfig.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class
})
public class TestControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentService documentService;

    @MockBean
    private DocumentVersionService documentVersionService;

    @MockBean
    private com.sap.vcs.server.service.ApprovalService approvalService;

    @MockBean
    private com.sap.vcs.server.security.CustomUserDetailsService customUserDetailsService;

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
    @WithMockUser
    void testEndpointReturnsRunningMessage() throws Exception {
        mockMvc.perform(get("/test"))
                .andExpect(status().isOk())
                .andExpect(content().string("Project is running"));
    }
}
