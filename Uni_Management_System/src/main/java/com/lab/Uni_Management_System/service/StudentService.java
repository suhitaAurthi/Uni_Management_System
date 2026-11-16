package com.lab.Uni_Management_System.service;

import com.lab.Uni_Management_System.dto.StudentCreateRequest;
import com.lab.Uni_Management_System.dto.StudentResponse;
import com.lab.Uni_Management_System.dto.StudentUpdateRequest;
import com.lab.Uni_Management_System.entity.Role;
import com.lab.Uni_Management_System.entity.Student;
import com.lab.Uni_Management_System.exception.DuplicateResourceException;
import com.lab.Uni_Management_System.exception.ResourceNotFoundException;
import com.lab.Uni_Management_System.repository.AppUserRepository;
import com.lab.Uni_Management_System.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {

    private final StudentRepository studentRepository;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    // Get all students
    public List<StudentResponse> findAll() {
        return studentRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Get student by ID
    public StudentResponse findById(Long id) {
        Student student = getStudentById(id);
        return toResponse(student);
    }

    // Create a new student
    @Transactional
    public StudentResponse create(StudentCreateRequest request) {

        if (appUserRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already exists");
        }

        if (studentRepository.existsByStudentId(request.studentId())) {
            throw new DuplicateResourceException("Student ID already exists");
        }

        Student student = new Student();

        student.setName(request.name());
        student.setEmail(request.email());
        student.setPassword(passwordEncoder.encode(request.password()));
        student.setRole(Role.STUDENT);

        student.setStudentId(request.studentId());
        student.setDepartment(request.department());
        student.setBatch(request.batch());

        Student savedStudent = studentRepository.save(student);

        return toResponse(savedStudent);
    }

    // Update an existing student
    @Transactional
    public StudentResponse update(Long id, StudentUpdateRequest request) {

        Student student = getStudentById(id);

        if (!student.getEmail().equalsIgnoreCase(request.email())
                && appUserRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already exists");
        }

        if (!student.getStudentId().equalsIgnoreCase(request.studentId())
                && studentRepository.existsByStudentId(request.studentId())) {
            throw new DuplicateResourceException("Student ID already exists");
        }

        student.setName(request.name());
        student.setEmail(request.email());
        student.setStudentId(request.studentId());
        student.setDepartment(request.department());
        student.setBatch(request.batch());

        // Update password only if a new password is provided
        if (request.password() != null && !request.password().isBlank()) {
            student.setPassword(passwordEncoder.encode(request.password()));
        }

        Student updatedStudent = studentRepository.save(student);

        return toResponse(updatedStudent);
    }

    // Delete a student
    @Transactional
    public void delete(Long id) {
        Student student = getStudentById(id);
        studentRepository.delete(student);
    }

    // Find student or throw 404 exception
    private Student getStudentById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Student not found with ID: " + id));
    }

    // Convert entity to response DTO
    private StudentResponse toResponse(Student student) {
        return new StudentResponse(
                student.getId(),
                student.getName(),
                student.getEmail(),
                student.getRole(),
                student.getStudentId(),
                student.getDepartment(),
                student.getBatch()
        );
    }
}