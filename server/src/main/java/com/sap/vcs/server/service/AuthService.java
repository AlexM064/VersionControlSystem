package com.sap.vcs.server.service;

import com.sap.vcs.server.dto.auth.AuthResponseDto;
import com.sap.vcs.server.dto.auth.LoginRequestDto;
import com.sap.vcs.server.entity.Role;
import com.sap.vcs.server.entity.User;
import com.sap.vcs.server.repository.UserRepository;
import com.sap.vcs.server.security.SecurityUserDetails;
import com.sap.vcs.server.security.jwt.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public AuthResponseDto login(LoginRequestDto request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow();

        String token = jwtService.generateToken(new SecurityUserDetails(user));

        return new AuthResponseDto(
                token,
                user.getUsername(),
                user.getRoles().stream().map(Role::getName).toList()
        );
    }
}