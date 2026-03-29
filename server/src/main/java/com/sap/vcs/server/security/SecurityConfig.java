package com.sap.vcs.server.security;

import com.sap.vcs.server.security.jwt.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

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
                        .requestMatchers(HttpMethod.GET, "/test").permitAll()
                        .requestMatchers("/auth/**").permitAll()

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

                        .requestMatchers(HttpMethod.PUT, "/documents/*")
                        .hasAnyRole("AUTHOR", "ADMIN")

                        .requestMatchers(HttpMethod.POST, "/documents/*/versions")
                        .hasAnyRole("AUTHOR", "ADMIN")

                        .requestMatchers(HttpMethod.POST, "/versions/*/approve")
                        .hasAnyRole("REVIEWER", "ADMIN")

                        .requestMatchers(HttpMethod.POST, "/versions/*/reject")
                        .hasAnyRole("REVIEWER", "ADMIN")

                        .requestMatchers(HttpMethod.POST, "/versions/*/publish")
                        .hasAnyRole("REVIEWER", "ADMIN")

                        .requestMatchers(HttpMethod.POST, "/versions/*/rollback")
                        .hasAnyRole("REVIEWER", "ADMIN")

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
}