package com.lab.Uni_Management_System.service;

import com.lab.Uni_Management_System.dto.TeacherCreateRequest;
import com.lab.Uni_Management_System.dto.TeacherResponse;
import com.lab.Uni_Management_System.dto.TeacherUpdateRequest;
import com.lab.Uni_Management_System.entity.Role;
import com.lab.Uni_Management_System.entity.Teacher;
import com.lab.Uni_Management_System.exception.DuplicateResourceException;
import com.lab.Uni_Management_System.exception.ResourceNotFoundException;
import com.lab.Uni_Management_System.repository.AppUserRepository;
import com.lab.Uni_Management_System.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    // Get all teachers
    public List<TeacherResponse> findAll() {
        return teacherRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Get teacher by ID
    public TeacherResponse findById(Long id) {
        Teacher teacher = getTeacherById(id);
        return toResponse(teacher);
    }

    // Create a new teacher
    @Transactional
    public TeacherResponse create(TeacherCreateRequest request) {

        if (appUserRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already exists");
        }

        Teacher teacher = new Teacher();

        teacher.setName(request.name());
        teacher.setEmail(request.email());
        teacher.setPassword(passwordEncoder.encode(request.password()));
        teacher.setRole(Role.TEACHER);

        teacher.setDepartment(request.department());
        teacher.setDesignation(request.designation());

        Teacher savedTeacher = teacherRepository.save(teacher);

        return toResponse(savedTeacher);
    }

    // Update an existing teacher
    @Transactional
    public TeacherResponse update(Long id, TeacherUpdateRequest request) {

        Teacher teacher = getTeacherById(id);

        if (!teacher.getEmail().equalsIgnoreCase(request.email())
                && appUserRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already exists");
        }

        teacher.setName(request.name());
        teacher.setEmail(request.email());
        teacher.setDepartment(request.department());
        teacher.setDesignation(request.designation());

        // Update password only if a new password is provided
        if (request.password() != null && !request.password().isBlank()) {
            teacher.setPassword(passwordEncoder.encode(request.password()));
        }

        Teacher updatedTeacher = teacherRepository.save(teacher);

        return toResponse(updatedTeacher);
    }

    // Delete a teacher
    @Transactional
    public void delete(Long id) {
        Teacher teacher = getTeacherById(id);
        teacherRepository.delete(teacher);
    }

    // Find teacher or throw 404 exception
    private Teacher getTeacherById(Long id) {
        return teacherRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Teacher not found with ID: " + id));
    }

    // Convert entity to response DTO
    private TeacherResponse toResponse(Teacher teacher) {
        return new TeacherResponse(
                teacher.getId(),
                teacher.getName(),
                teacher.getEmail(),
                teacher.getRole(),
                teacher.getDepartment(),
                teacher.getDesignation()
        );
    }
}