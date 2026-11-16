package com.lab.Uni_Management_System.repository;

import com.lab.Uni_Management_System.entity.Role;
import com.lab.Uni_Management_System.entity.Student;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import static org.assertj.core.api.Assertions.assertThat;

// Repository (DB) test: runs against the real MySQL database from application.properties.
// @DataJpaTest wraps each test in a transaction that is rolled back afterwards.
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class StudentRepositoryTest {

    @Autowired
    private StudentRepository underTest;

    @Test
    void itShouldCheckIfStudentExistsByStudentId() {
        // given
        Student student = new Student();
        student.setName("Suhita Islam");
        student.setEmail("repo.test@uni.com");
        student.setPassword("suhita5252");
        student.setRole(Role.STUDENT);
        student.setStudentId("REPO-TEST-001");
        student.setDepartment("CSE");
        student.setBatch("2K20");
        underTest.save(student);

        // when
        boolean exists = underTest.existsByStudentId("REPO-TEST-001");

        // then
        assertThat(exists).isTrue();
        assertThat(underTest.findByStudentId("REPO-TEST-001"))
                .isPresent()
                .get()
                .extracting(Student::getEmail)
                .isEqualTo("repo.test@uni.com");
    }
}
