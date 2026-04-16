package com.sap.vcs.server.security;

import com.sap.vcs.server.security.jwt.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final String API_V1_PREFIX = "/api/v1";

    private final CustomUserDetailsService customUserDetailsService;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final RestAccessDeniedHandler restAccessDeniedHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            CustomUserDetailsService customUserDetailsService,
            RestAuthenticationEntryPoint restAuthenticationEntryPoint,
            RestAccessDeniedHandler restAccessDeniedHandler,
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) {
        this.customUserDetailsService = customUserDetailsService;
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
        this.restAccessDeniedHandler = restAccessDeniedHandler;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // =========================
                        // PUBLIC ENDPOINTS
                        // =========================

                        // Legacy endpoints (frontend compatibility)
                        .requestMatchers("/auth/login", "/auth/register", "/api/auth/login", "/api/auth/register").permitAll()

                        // Versioned API endpoints
                        .requestMatchers(HttpMethod.POST, API_V1_PREFIX + "/tokens").permitAll()
                        .requestMatchers(HttpMethod.POST, API_V1_PREFIX + "/users").permitAll()

                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/documents/published")
                        .hasAnyRole("READER", "AUTHOR", "REVIEWER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/documents/*/published-only")
                        .hasAnyRole("READER", "AUTHOR", "REVIEWER", "ADMIN")
                        // =========================
                        // AUTHENTICATED PROFILE
                        // =========================

                        .requestMatchers(HttpMethod.GET, "/auth/me", "/api/auth/me", API_V1_PREFIX + "/users/me")
                        .authenticated()

                        // =========================
                        // DOCUMENTS - READ
                        // =========================

                        .requestMatchers(HttpMethod.GET, "/documents", API_V1_PREFIX + "/documents")
                        .hasAnyRole("AUTHOR", "REVIEWER", "ADMIN")

                        .requestMatchers(HttpMethod.GET, "/documents/*", API_V1_PREFIX + "/documents/*")
                        .hasAnyRole("AUTHOR", "REVIEWER", "ADMIN")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/documents/*/history",
                                API_V1_PREFIX + "/documents/*/history"
                        )
                        .hasAnyRole("AUTHOR", "REVIEWER", "ADMIN")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/documents/*/versions",
                                API_V1_PREFIX + "/documents/*/versions"
                        )
                        .hasAnyRole("AUTHOR", "REVIEWER", "ADMIN")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/documents/compare",
                                API_V1_PREFIX + "/documents/compare"
                        )
                        .hasAnyRole("AUTHOR", "REVIEWER", "ADMIN")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/documents/*/published-version",
                                API_V1_PREFIX + "/documents/*/published-version"
                        )
                        .hasAnyRole("READER", "AUTHOR", "REVIEWER", "ADMIN")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/documents/*/published-version/pdf",
                                API_V1_PREFIX + "/documents/*/published-version/pdf"
                        )
                        .hasAnyRole("READER", "AUTHOR", "REVIEWER", "ADMIN")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/documents/*/versions/*/pdf",
                                API_V1_PREFIX + "/documents/*/versions/*/pdf"
                        )
                        .hasAnyRole("AUTHOR", "REVIEWER", "ADMIN")

                        // =========================
                        // DOCUMENTS - WRITE
                        // =========================

                        .requestMatchers(HttpMethod.POST, "/documents", API_V1_PREFIX + "/documents")
                        .hasAnyRole("AUTHOR", "ADMIN")

                        .requestMatchers(HttpMethod.PUT, "/documents/*", API_V1_PREFIX + "/documents/*")
                        .hasAnyRole("AUTHOR", "ADMIN")

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/documents/*/archive",
                                API_V1_PREFIX + "/documents/*/archive"
                        )
                        .hasAnyRole("AUTHOR", "ADMIN")

                        .requestMatchers(HttpMethod.DELETE, "/documents/*", API_V1_PREFIX + "/documents/*")
                        .hasRole("ADMIN")

                        // =========================
                        // VERSIONS
                        // =========================

                        .requestMatchers(
                                HttpMethod.POST,
                                "/documents/*/versions",
                                API_V1_PREFIX + "/documents/*/versions"
                        )
                        .hasAnyRole("AUTHOR", "ADMIN")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/documents/*/versions/*/submit",
                                API_V1_PREFIX + "/documents/*/versions/*/submit"
                        )
                        .hasAnyRole("AUTHOR", "ADMIN")

                        // =========================
                        // APPROVAL WORKFLOW
                        // =========================

                        .requestMatchers(
                                HttpMethod.POST,
                                "/versions/*/approve",
                                API_V1_PREFIX + "/versions/*/approve"
                        )
                        .hasAnyRole("REVIEWER", "ADMIN")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/versions/*/reject",
                                API_V1_PREFIX + "/versions/*/reject"
                        )
                        .hasAnyRole("REVIEWER", "ADMIN")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/versions/*/publish",
                                API_V1_PREFIX + "/versions/*/publish"
                        )
                        .hasAnyRole("REVIEWER", "ADMIN")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/versions/*/rollback",
                                API_V1_PREFIX + "/versions/*/rollback"
                        )
                        .hasAnyRole("REVIEWER", "ADMIN")

                        // =========================
                        // USER MANAGEMENT
                        // =========================

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/users/*/role",
                                API_V1_PREFIX + "/users/*/role"
                        )
                        .hasRole("ADMIN")

                        .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:3001",
                "http://localhost:3002"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}