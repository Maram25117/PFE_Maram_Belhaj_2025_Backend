package com.example.api_tierces.service;

import com.example.api_tierces.model.Api;
import com.example.api_tierces.repository.ApiRepository;
import com.example.api_tierces.service.ApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ApiService {

    @Autowired
    private ApiRepository apiRepository;


    public List<Api> getAllApis() {
        return apiRepository.findAll();
    }


    public Optional<Api> getApiById(Long id) {
        return apiRepository.findById(id);
    }


    public Optional<Api> getApiByPath(String path) {
        return apiRepository.findByPath(path);
    }
}


