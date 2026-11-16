package com.lab.Uni_Management_System.security;

import com.lab.Uni_Management_System.entity.AppUser;
import com.lab.Uni_Management_System.repository.AppUserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        AppUser appUser = appUserRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found: " + email));

        return User.builder()
                .username(appUser.getEmail())
                .password(appUser.getPassword())
                .authorities("ROLE_" + appUser.getRole().name())
                .build();
    }
}