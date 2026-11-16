package com.lab.Uni_Management_System.repository;

import com.lab.Uni_Management_System.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, Long> {
}