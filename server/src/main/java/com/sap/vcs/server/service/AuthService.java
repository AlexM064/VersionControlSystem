package com.sap.vcs.server.service;

import com.sap.vcs.server.dto.auth.*;
import com.sap.vcs.server.entity.Role;
import com.sap.vcs.server.entity.User;
import com.sap.vcs.server.repository.RoleRepository;
import com.sap.vcs.server.repository.UserRepository;
import com.sap.vcs.server.security.SecurityUserDetails;
import com.sap.vcs.server.security.jwt.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public AuthResponseDto register(RegisterRequestDto request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        Role readerRole = roleRepository.findByName("READER")
                .orElseThrow();

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setIsActive(true);
        user.setRoles(Set.of(readerRole));
        user.updateEncodedPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

        String token = jwtService.generateToken(new SecurityUserDetails(user));

        return new AuthResponseDto(
                token,
                user.getUsername(),
                List.of("READER")
        );
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

    public MeResponseDto me(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();

        return new MeResponseDto(
                user.getUsername(),
                user.getEmail(),
                user.getRoles().stream().map(Role::getName).toList()
        );
    }
}