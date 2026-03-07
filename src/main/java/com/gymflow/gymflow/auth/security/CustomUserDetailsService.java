package com.gymflow.gymflow.auth.security;






import com.gymflow.gymflow.auth.entity.GymOwner;
import com.gymflow.gymflow.auth.repository.GymOwnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final GymOwnerRepository repository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        GymOwner owner = repository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Owner not found with email: " + email));

        return new CustomUserDetails(
                owner.getEmail(),
                owner.getPassword(),
                owner.getRole().name() // Hardcode hatakar Enum name use karein
        );
    }
}