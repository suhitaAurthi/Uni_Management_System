package com.lab.Uni_Management_System.repository;

import com.lab.Uni_Management_System.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
}