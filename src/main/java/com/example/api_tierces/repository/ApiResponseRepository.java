package com.example.api_tierces.repository;

import com.example.api_tierces.model.ApiResponse; // Import correct
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.api_tierces.model.Api;

import java.util.List;

@Repository
public interface ApiResponseRepository extends JpaRepository<ApiResponse, Long> {
    List<ApiResponse> findByApiId(Long apiId);
    List<ApiResponse> findByApi(Api api);
    ApiResponse findByApiAndStatus(Api api, String status);
    List<ApiResponse> findByApiPath(String path);

}
