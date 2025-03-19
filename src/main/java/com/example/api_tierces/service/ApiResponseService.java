package com.example.api_tierces.service;

import com.example.api_tierces.model.ApiResponse;
import com.example.api_tierces.repository.ApiResponseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ApiResponseService {

    @Autowired
    private ApiResponseRepository apiResponseRepository;


    public List<ApiResponse> getAllResponses() {
        return apiResponseRepository.findAll();
    }


    public Optional<ApiResponse> getResponseById(Long id) {
        return apiResponseRepository.findById(id);
    }


    public List<ApiResponse> getResponsesByApiId(Long apiId) {
        return apiResponseRepository.findByApiId(apiId);
    }
}


