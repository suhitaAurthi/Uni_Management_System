package com.lab.Uni_Management_System.integration;

import com.lab.Uni_Management_System.dto.StudentCreateRequest;
import com.lab.Uni_Management_System.dto.StudentResponse;
import com.lab.Uni_Management_System.dto.StudentUpdateRequest;
import com.lab.Uni_Management_System.entity.Student;
import com.lab.Uni_Management_System.repository.StudentRepository;
import com.lab.Uni_Management_System.service.StudentService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

// Integration test: real StudentService + real repository + real MySQL database.
// @Transactional rolls the data back after every test.
@SpringBootTest
@Transactional
class StudentServiceIntegrationTest {

    @Autowired
    private StudentService studentService;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void itShouldInsertStudentAndHashPassword() {
        // given
        StudentCreateRequest request = new StudentCreateRequest(
                "Zaina Rahman", "zaina.service@uni.com", "plainPassword123", "SVC-TEST-102", "CSE", "2K20");

        // when
        studentService.create(request);

        // then
        Student saved = studentRepository.findByStudentId("SVC-TEST-102").orElseThrow();
        assertThat(saved.getEmail()).isEqualTo("zaina.service@uni.com");
        assertThat(saved.getPassword()).isNotEqualTo("plainPassword123");
        assertThat(saved.getPassword()).startsWith("$2a$");
    }

    @Test
    void itShouldUpdateStudentInDatabase() {
        // given
        StudentResponse created = studentService.create(new StudentCreateRequest(
                "Old Name", "update.user@uni.com", "oldPassword", "SVC-TEST-301", "CSE", "2K20"));

        Long id = created.id();
        String oldHash = studentRepository.findById(id).orElseThrow().getPassword();

        StudentUpdateRequest changes = new StudentUpdateRequest(
                "New Name", "update.user2@uni.com", "newPassword", "SVC-TEST-302", "EEE", "2K21");

        // when
        studentService.update(id, changes);
        entityManager.flush();
        entityManager.clear();

        // then
        Student updated = studentRepository.findById(id).orElseThrow();
        assertThat(updated.getName()).isEqualTo("New Name");
        assertThat(updated.getEmail()).isEqualTo("update.user2@uni.com");
        assertThat(updated.getStudentId()).isEqualTo("SVC-TEST-302");
        assertThat(updated.getDepartment()).isEqualTo("EEE");
        assertThat(updated.getBatch()).isEqualTo("2K21");
        assertThat(updated.getPassword()).isNotEqualTo(oldHash);
        assertThat(updated.getPassword()).startsWith("$2a$");
    }
}
