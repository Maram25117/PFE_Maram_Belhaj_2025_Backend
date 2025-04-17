package com.example.api_tierces.repository;

import com.example.api_tierces.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    Admin findByUsername(String username);
    Optional<Admin> findByEmail(String email);
    Optional<Admin> findByResetToken(String resetToken);
}
