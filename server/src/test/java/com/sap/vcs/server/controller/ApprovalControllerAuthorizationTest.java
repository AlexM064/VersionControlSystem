package com.sap.vcs.server.controller;

import com.sap.vcs.server.dto.ApprovalResponseDto;
import com.sap.vcs.server.security.CustomUserDetailsService;
import com.sap.vcs.server.security.RestAccessDeniedHandler;
import com.sap.vcs.server.security.RestAuthenticationEntryPoint;
import com.sap.vcs.server.security.SecurityConfig;
import com.sap.vcs.server.security.jwt.JwtService;
import com.sap.vcs.server.service.ApprovalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApprovalController.class)
@Import({
        SecurityConfig.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class
})
class ApprovalControllerAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApprovalService approvalService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtService jwtService;

    @Test
    @WithMockUser(roles = "REVIEWER")
    void approve_allowsReviewer() throws Exception {
        when(approvalService.approve(1))
                .thenReturn(new ApprovalResponseDto(
                        10,
                        1,
                        100,
                        "APPROVED",
                        null,
                        LocalDateTime.now()
                ));

        mockMvc.perform(post("/versions/1/approve"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "AUTHOR")
    void approve_forbidsAuthor() throws Exception {
        mockMvc.perform(post("/versions/1/approve"))
                .andExpect(status().isForbidden());
    }

    @Test
    void approve_requiresAuthentication() throws Exception {
        mockMvc.perform(post("/versions/1/approve"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void reject_allowsAdmin() throws Exception {
        when(approvalService.reject(1))
                .thenReturn(new ApprovalResponseDto(
                        11,
                        1,
                        100,
                        "REJECTED",
                        null,
                        LocalDateTime.now()
                ));

        mockMvc.perform(post("/versions/1/reject"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "READER")
    void reject_forbidsReader() throws Exception {
        mockMvc.perform(post("/versions/1/reject"))
                .andExpect(status().isForbidden());
    }

    @Test
    void reject_requiresAuthentication() throws Exception {
        mockMvc.perform(post("/versions/1/reject"))
                .andExpect(status().isUnauthorized());
    }
}