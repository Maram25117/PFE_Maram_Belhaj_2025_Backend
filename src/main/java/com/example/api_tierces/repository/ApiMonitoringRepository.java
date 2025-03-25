package com.example.api_tierces.repository;

import com.example.api_tierces.model.ApiMonitoring;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface ApiMonitoringRepository extends JpaRepository<ApiMonitoring, Long> {
    Optional<ApiMonitoring> findByMetadata(@Param("metadata") String metadata);
}

