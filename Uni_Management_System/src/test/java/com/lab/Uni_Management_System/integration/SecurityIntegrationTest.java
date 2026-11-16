package com.lab.Uni_Management_System.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Integration test for the role rules in SecurityConfig (/api/students = ADMIN or STUDENT).
@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void unauthenticatedUserShouldGetUnauthorized() throws Exception {
        mockMvc.perform(get("/api/students").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized()) // 401
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    @WithMockUser(username = "admin@university.com", roles = {"ADMIN"})
    void adminUserShouldGetOk() throws Exception {
        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk()); // 200
    }

    @Test
    @WithMockUser(username = "teacher@university.com", roles = {"TEACHER"})
    void teacherUserShouldGetForbidden() throws Exception {
        mockMvc.perform(get("/api/students"))
                .andExpect(status().isForbidden()) // 403
                .andExpect(jsonPath("$.message").value("You do not have permission to access this resource"));
    }
}
