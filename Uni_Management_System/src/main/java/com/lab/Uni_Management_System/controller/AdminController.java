package com.lab.Uni_Management_System.controller;

import com.lab.Uni_Management_System.dto.AdminCreateRequest;
import com.lab.Uni_Management_System.dto.AdminUpdateRequest;
import com.lab.Uni_Management_System.dto.AdminResponse;
import com.lab.Uni_Management_System.service.AdminService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/admins")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // GET all admins
    @GetMapping
    public ResponseEntity<List<AdminResponse>> findAll() {
        return ResponseEntity.ok(adminService.findAll());
    }

    // GET admin by ID
    @GetMapping("/{id}")
    public ResponseEntity<AdminResponse> findById(
            @PathVariable Long id) {

        return ResponseEntity.ok(adminService.findById(id));
    }

    // CREATE admin
    @PostMapping
    public ResponseEntity<AdminResponse> create(
            @Valid @RequestBody AdminCreateRequest request) {

        AdminResponse response = adminService.create(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    // UPDATE admin
    @PutMapping("/{id}")
    public ResponseEntity<AdminResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody AdminUpdateRequest request) {

        return ResponseEntity.ok(adminService.update(id, request));
    }

    // DELETE admin
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id) {

        adminService.delete(id);

        return ResponseEntity.noContent().build();
    }
}