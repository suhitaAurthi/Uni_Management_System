package com.lab.Uni_Management_System.controller;

import com.lab.Uni_Management_System.dto.TeacherCreateRequest;
import com.lab.Uni_Management_System.dto.TeacherUpdateRequest;
import com.lab.Uni_Management_System.dto.TeacherResponse;
import com.lab.Uni_Management_System.service.TeacherService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    @GetMapping
    public ResponseEntity<List<TeacherResponse>> findAll() {
        return ResponseEntity.ok(teacherService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeacherResponse> findById(
            @PathVariable Long id) {

        return ResponseEntity.ok(teacherService.findById(id));
    }

    @PostMapping
    public ResponseEntity<TeacherResponse> create(
            @Valid @RequestBody TeacherCreateRequest request) {

        TeacherResponse response = teacherService.create(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TeacherResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody TeacherUpdateRequest request) {

        return ResponseEntity.ok(teacherService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id) {

        teacherService.delete(id);

        return ResponseEntity.noContent().build();
    }
}