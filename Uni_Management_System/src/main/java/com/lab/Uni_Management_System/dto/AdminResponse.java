package com.lab.Uni_Management_System.dto;

import com.lab.Uni_Management_System.entity.Role;

public record AdminResponse(
        Long id,
        String name,
        String email,
        Role role,
        String designation
) {}