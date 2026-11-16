package com.lab.Uni_Management_System.service;

import com.lab.Uni_Management_System.dto.AdminCreateRequest;
import com.lab.Uni_Management_System.dto.AdminResponse;
import com.lab.Uni_Management_System.dto.AdminUpdateRequest;
import com.lab.Uni_Management_System.entity.Admin;
import com.lab.Uni_Management_System.entity.Role;
import com.lab.Uni_Management_System.exception.DuplicateResourceException;
import com.lab.Uni_Management_System.exception.ResourceNotFoundException;
import com.lab.Uni_Management_System.repository.AdminRepository;
import com.lab.Uni_Management_System.repository.AppUserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final AdminRepository adminRepository;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    // Find all admins
    public List<AdminResponse> findAll() {
        return adminRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Find admin by ID
    public AdminResponse findById(Long id) {
        Admin admin = getAdminById(id);
        return toResponse(admin);
    }

    // Create admin
    @Transactional
    public AdminResponse create(AdminCreateRequest request) {

        if (appUserRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already exists");
        }

        Admin admin = new Admin();

        admin.setName(request.name());
        admin.setEmail(request.email());
        admin.setPassword(passwordEncoder.encode(request.password()));
        admin.setRole(Role.ADMIN);
        admin.setDesignation(request.designation());

        Admin savedAdmin = adminRepository.save(admin);

        return toResponse(savedAdmin);
    }

    // Update admin
    @Transactional
    public AdminResponse update(Long id, AdminUpdateRequest request) {

        Admin admin = getAdminById(id);

        // Check email only if it has changed
        if (!admin.getEmail().equalsIgnoreCase(request.email())
                && appUserRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already exists");
        }

        admin.setName(request.name());
        admin.setEmail(request.email());
        admin.setDesignation(request.designation());

        // Change password only if a new password was provided
        if (request.password() != null && !request.password().isBlank()) {
            admin.setPassword(passwordEncoder.encode(request.password()));
        }

        Admin updatedAdmin = adminRepository.save(admin);

        return toResponse(updatedAdmin);
    }

    // Delete admin
    @Transactional
    public void delete(Long id) {
        Admin admin = getAdminById(id);
        adminRepository.delete(admin);
    }

    // Find admin or throw exception
    private Admin getAdminById(Long id) {
        return adminRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Admin not found with ID: " + id
                        ));
    }

    // Convert Admin entity into response DTO
    private AdminResponse toResponse(Admin admin) {
        return new AdminResponse(
                admin.getId(),
                admin.getName(),
                admin.getEmail(),
                admin.getRole(),
                admin.getDesignation()
        );
    }
}