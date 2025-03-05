package com.example.api_tierces.repository;

import com.example.api_tierces.model.ApiResponse; // Import correct
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiResponseRepository extends JpaRepository<ApiResponse, Long> {
}
