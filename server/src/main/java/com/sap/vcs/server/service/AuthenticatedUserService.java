package com.sap.vcs.server.service;

import com.sap.vcs.server.entity.User;
import com.sap.vcs.server.exception.ResourceNotFoundException;
import com.sap.vcs.server.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticatedUserService {

    private final UserRepository userRepository;

    public AuthenticatedUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new IllegalStateException("No authenticated user available");
        }

        return authentication.getName();
    }

    public User getCurrentUser() {
        String username = getCurrentUsername();

        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Authenticated user not found: " + username));
    }
}