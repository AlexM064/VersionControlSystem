package com.sap.vcs.server.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final RestAccessDeniedHandler restAccessDeniedHandler;

    public SecurityConfig(
            CustomUserDetailsService customUserDetailsService,
            RestAuthenticationEntryPoint restAuthenticationEntryPoint,
            RestAccessDeniedHandler restAccessDeniedHandler
    ) {
        this.customUserDetailsService = customUserDetailsService;
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
        this.restAccessDeniedHandler = restAccessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(Customizer.withDefaults())
                .userDetailsService(customUserDetailsService)
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(HttpMethod.GET, "/test").permitAll()

                        // Document endpoints
                        .requestMatchers(HttpMethod.GET, "/documents")
                        .hasAnyRole("AUTHOR", "REVIEWER", "ADMIN")

                        .requestMatchers(HttpMethod.GET, "/documents/*")
                        .hasAnyRole("AUTHOR", "REVIEWER", "ADMIN")

                        .requestMatchers(HttpMethod.GET, "/documents/*/history")
                        .hasAnyRole("AUTHOR", "REVIEWER", "ADMIN")

                        .requestMatchers(HttpMethod.GET, "/documents/*/versions")
                        .hasAnyRole("AUTHOR", "REVIEWER", "ADMIN")

                        .requestMatchers(HttpMethod.GET, "/documents/*/published-version")
                        .hasAnyRole("READER", "AUTHOR", "REVIEWER", "ADMIN")

                        .requestMatchers(HttpMethod.POST, "/documents")
                        .hasAnyRole("AUTHOR", "ADMIN")

                        // Document version endpoints
                        .requestMatchers(HttpMethod.GET, "/documents/*/versions")
                        .hasAnyRole("AUTHOR", "REVIEWER", "ADMIN")

                        .requestMatchers(HttpMethod.POST, "/documents/*/versions")
                        .hasAnyRole("AUTHOR", "ADMIN")

                        // Workflow endpoints
                        .requestMatchers(HttpMethod.POST, "/versions/*/approve")
                        .hasAnyRole("REVIEWER", "ADMIN")

                        .requestMatchers(HttpMethod.POST, "/versions/*/reject")
                        .hasAnyRole("REVIEWER", "ADMIN")

                        .requestMatchers(HttpMethod.POST, "/versions/*/publish")
                        .hasAnyRole("REVIEWER", "ADMIN")

                        .requestMatchers(HttpMethod.POST, "/versions/*/rollback")
                        .hasAnyRole("REVIEWER", "ADMIN")

                        .anyRequest().hasRole("ADMIN")
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
