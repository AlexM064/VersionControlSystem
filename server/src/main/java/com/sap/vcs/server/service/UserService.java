package com.sap.vcs.server.service;

import com.sap.vcs.server.entity.Role;
import com.sap.vcs.server.entity.User;
import com.sap.vcs.server.exception.ResourceNotFoundException;
import com.sap.vcs.server.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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

    @Transactional
    public User changePassword(Integer userId, String rawPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with id: " + userId));

        user.updateEncodedPassword(passwordEncoder.encode(rawPassword));
        return userRepository.save(user);
    }
}
