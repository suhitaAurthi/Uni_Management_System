package com.lab.Uni_Management_System.dto;

import com.lab.Uni_Management_System.entity.Role;

public record TeacherResponse(
        Long id,
        String name,
        String email,
        Role role,
        String department,
        String designation
) {}