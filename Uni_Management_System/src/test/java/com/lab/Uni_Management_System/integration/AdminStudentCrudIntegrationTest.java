package com.lab.Uni_Management_System.integration;

import com.lab.Uni_Management_System.entity.Student;
import com.lab.Uni_Management_System.repository.StudentRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Integration test: HTTP request -> security -> controller -> service -> MySQL, logged in as an ADMIN.
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "admin@university.com", roles = {"ADMIN"})
class AdminStudentCrudIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void adminCanCreateReadUpdateDeleteStudent() throws Exception {

        // CREATE
        String newStudentJson = """
                {
                  "name": "Crud Student",
                  "email": "crud.student@uni.com",
                  "password": "crudPassword",
                  "studentId": "CRUD-TEST-501",
                  "department": "CSE",
                  "batch": "2K20"
                }
                """;

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newStudentJson))
                .andExpect(status().isCreated());

        entityManager.flush();
        entityManager.clear();

        Student created = studentRepository.findByStudentId("CRUD-TEST-501").orElseThrow();
        Long id = created.getId();
        assertThat(created.getName()).isEqualTo("Crud Student");
        assertThat(created.getPassword()).startsWith("$2a$");

        // READ
        mockMvc.perform(get("/api/students/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Crud Student"))
                .andExpect(jsonPath("$.email").value("crud.student@uni.com"))
                .andExpect(jsonPath("$.studentId").value("CRUD-TEST-501"))
                .andExpect(jsonPath("$.role").value("STUDENT"))
                .andExpect(jsonPath("$.password").doesNotExist());

        // UPDATE (no password -> the old password is kept)
        String updateJson = """
                {
                  "name": "Updated Student",
                  "email": "crud.student@uni.com",
                  "studentId": "CRUD-TEST-502",
                  "department": "EEE",
                  "batch": "2K21"
                }
                """;

        mockMvc.perform(put("/api/students/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Student"));

        entityManager.flush();
        entityManager.clear();

        Student updated = studentRepository.findById(id).orElseThrow();
        assertThat(updated.getName()).isEqualTo("Updated Student");
        assertThat(updated.getStudentId()).isEqualTo("CRUD-TEST-502");
        assertThat(updated.getPassword()).isEqualTo(created.getPassword());

        // DELETE
        mockMvc.perform(delete("/api/students/" + id))
                .andExpect(status().isNoContent());

        entityManager.flush();
        entityManager.clear();

        assertThat(studentRepository.findById(id)).isEmpty();

        mockMvc.perform(get("/api/students/" + id))
                .andExpect(status().isNotFound());
    }
}
