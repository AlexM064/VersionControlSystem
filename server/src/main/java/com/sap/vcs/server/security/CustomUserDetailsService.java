package com.sap.vcs.server.security;

import com.sap.vcs.server.entity.User;
import com.sap.vcs.server.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("LOGIN ATTEMPT: " + username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with username: " + username));

        System.out.println("FOUND USER: " + user.getUsername());
        System.out.println("PASSWORD FROM DB: " + user.getPasswordHash());
        System.out.println("ACTIVE: " + user.getIsActive());
        System.out.println("ROLES COUNT: " + user.getRoles().size());

        return new SecurityUserDetails(user);
    }
}