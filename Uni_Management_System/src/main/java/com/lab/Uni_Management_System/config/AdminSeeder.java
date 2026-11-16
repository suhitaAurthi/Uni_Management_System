package com.lab.Uni_Management_System.config;

import com.lab.Uni_Management_System.entity.Admin;
import com.lab.Uni_Management_System.entity.Role;
import com.lab.Uni_Management_System.repository.AdminRepository;
import com.lab.Uni_Management_System.repository.AppUserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminSeeder {

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Bean
    public CommandLineRunner seedAdmin(
            AdminRepository adminRepository,
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            if (!appUserRepository.existsByEmail(adminEmail)) {

                Admin admin = new Admin();

                admin.setName("System Administrator");
                admin.setEmail(adminEmail);
                admin.setPassword(passwordEncoder.encode(adminPassword));
                admin.setRole(Role.ADMIN);
                admin.setDesignation("Administrator");

                adminRepository.save(admin);

                System.out.println("Initial admin account created.");
            } else {
                System.out.println("Admin account already exists.");
            }
        };
    }
}