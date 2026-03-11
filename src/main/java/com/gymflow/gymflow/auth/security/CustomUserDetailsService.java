package com.gymflow.gymflow.auth.security;

import com.gymflow.gymflow.auth.entity.GymOwner;
import com.gymflow.gymflow.auth.repository.GymOwnerRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Loads user details from the database for authentication.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final GymOwnerRepository repository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("Loading user details for email={}", email);

        GymOwner owner = repository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Owner not found with email={}", email);
                    return new UsernameNotFoundException("Owner not found with email: " + email);
                });

        return new CustomUserDetails(
                owner.getEmail(),
                owner.getPassword(),
                owner.getRole().name()
        );
    }
}