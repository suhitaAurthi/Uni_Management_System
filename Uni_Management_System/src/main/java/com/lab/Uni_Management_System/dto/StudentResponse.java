package com.lab.Uni_Management_System.dto;

import com.lab.Uni_Management_System.entity.Role;

public record StudentResponse(
        Long id,
        String name,
        String email,
        Role role,
        String studentId,
        String department,
        String batch
) {}