package com.sap.vcs.server.service;

import com.sap.vcs.server.dto.auth.AuthResponseDto;
import com.sap.vcs.server.dto.auth.MeResponseDto;
import com.sap.vcs.server.dto.auth.RegisterRequestDto;
import com.sap.vcs.server.entity.Role;
import com.sap.vcs.server.entity.User;
import com.sap.vcs.server.exception.ResourceNotFoundException;
import com.sap.vcs.server.repository.RoleRepository;
import com.sap.vcs.server.repository.UserRepository;
import com.sap.vcs.server.security.SecurityUserDetails;
import com.sap.vcs.server.security.jwt.JwtService;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponseDto register(RegisterRequestDto request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        Role readerRole = roleRepository.findByName("READER")
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: READER"));

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

    @Transactional
    public User createUser(
            String username,
            String email,
            String rawPassword,
            Boolean isActive,
            Set<Role> roles
    ) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setIsActive(isActive != null ? isActive : Boolean.TRUE);
        user.setRoles(roles != null ? new HashSet<>(roles) : new HashSet<>());
        user.updateEncodedPassword(passwordEncoder.encode(rawPassword));

        return userRepository.save(user);
    }

    public MeResponseDto me(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        return new MeResponseDto(
                user.getUsername(),
                user.getEmail(),
                user.getRoles().stream().map(Role::getName).toList()
        );
    }

    @Transactional
    public User changePassword(Integer userId, String rawPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with id: " + userId));

        user.updateEncodedPassword(passwordEncoder.encode(rawPassword));
        return userRepository.save(user);
    }

    @Transactional
    public void updateUserRole(Integer userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with id: " + userId));

        Role role = roleRepository.findByName(roleName.toUpperCase())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Role not found: " + roleName));

        user.setRoles(Set.of(role));
        userRepository.save(user);
    }
}