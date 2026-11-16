package com.lab.Uni_Management_System.service;

import com.lab.Uni_Management_System.dto.StudentCreateRequest;
import com.lab.Uni_Management_System.dto.StudentResponse;
import com.lab.Uni_Management_System.dto.StudentUpdateRequest;
import com.lab.Uni_Management_System.entity.Role;
import com.lab.Uni_Management_System.entity.Student;
import com.lab.Uni_Management_System.exception.ResourceNotFoundException;
import com.lab.Uni_Management_System.repository.AppUserRepository;
import com.lab.Uni_Management_System.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

// Unit tests: the repositories and the password encoder are mocked, so no database is used.
@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;
    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private StudentService underTest;

    @BeforeEach
    void setUp() {
        underTest = new StudentService(studentRepository, appUserRepository, passwordEncoder);
    }

    // Builds a student that already "exists" in the database
    private Student existingStudent(Long id) {
        Student student = new Student();
        student.setId(id);
        student.setName("Zaina");
        student.setEmail("zaina@uni.com");
        student.setPassword("encodedOldPassword");
        student.setRole(Role.STUDENT);
        student.setStudentId("2007101");
        student.setDepartment("CSE");
        student.setBatch("2K20");
        return student;
    }

    @Test
    void getStudents() {
        // when
        underTest.findAll();

        // then
        verify(studentRepository).findAll();
    }

    @Test
    void getStudentById() {
        // given
        Long id = 1L;
        given(studentRepository.findById(id)).willReturn(Optional.of(existingStudent(id)));

        // when
        StudentResponse found = underTest.findById(id);

        // then
        verify(studentRepository).findById(id);
        assertThat(found.id()).isEqualTo(id);
        assertThat(found.name()).isEqualTo("Zaina");
        assertThat(found.email()).isEqualTo("zaina@uni.com");
        assertThat(found.studentId()).isEqualTo("2007101");
    }

    @Test
    void canInsertStudent() {
        // given
        StudentCreateRequest request = new StudentCreateRequest(
                "Suhita", "suhita@uni.com", "suhita5252", "2007052", "CSE", "2K20");

        given(passwordEncoder.encode("suhita5252")).willReturn("encodedPassword52");
        given(studentRepository.save(any(Student.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        underTest.create(request);

        // then
        ArgumentCaptor<Student> studentArgumentCaptor = ArgumentCaptor.forClass(Student.class);
        verify(studentRepository).save(studentArgumentCaptor.capture());

        Student captured = studentArgumentCaptor.getValue();
        assertThat(captured.getEmail()).isEqualTo("suhita@uni.com");
        assertThat(captured.getPassword()).isEqualTo("encodedPassword52");
        assertThat(captured.getRole()).isEqualTo(Role.STUDENT);
        assertThat(captured.getStudentId()).isEqualTo("2007052");
        assertThat(captured.getId()).isNull();
    }

    @Test
    void canUpdateStudent() {
        // given
        Long id = 1L;
        Student existing = existingStudent(id);

        StudentUpdateRequest changes = new StudentUpdateRequest(
                "Zaina Rahman", "zaina.rahman@uni.com", "newPassword22", "2007102", "EEE", "2K21");

        given(studentRepository.findById(id)).willReturn(Optional.of(existing));
        given(appUserRepository.existsByEmail("zaina.rahman@uni.com")).willReturn(false);
        given(studentRepository.existsByStudentId("2007102")).willReturn(false);
        given(passwordEncoder.encode("newPassword22")).willReturn("encodedNewPassword");
        given(studentRepository.save(existing)).willReturn(existing);

        // when
        underTest.update(id, changes);

        // then
        verify(studentRepository).save(existing);

        assertThat(existing.getName()).isEqualTo("Zaina Rahman");
        assertThat(existing.getEmail()).isEqualTo("zaina.rahman@uni.com");
        assertThat(existing.getStudentId()).isEqualTo("2007102");
        assertThat(existing.getDepartment()).isEqualTo("EEE");
        assertThat(existing.getBatch()).isEqualTo("2K21");
        assertThat(existing.getPassword()).isEqualTo("encodedNewPassword");
    }

    @Test
    void willDeleteStudent() {
        // given
        Long id = 1L;
        Student existing = existingStudent(id);
        given(studentRepository.findById(id)).willReturn(Optional.of(existing));

        // when
        underTest.delete(id);

        // then
        verify(studentRepository).delete(existing);
    }

    @Test
    void willThrowWhenStudentNotFoundOnDelete() {
        // given
        Long id = 1L;
        given(studentRepository.findById(id)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> underTest.delete(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Student not found with ID: " + id);

        verify(studentRepository, never()).delete(any());
    }
}
