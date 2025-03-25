package com.example.api_tierces.repository;

import com.example.api_tierces.model.ApiChange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiChangeRepository extends JpaRepository<ApiChange, Long> {
}

