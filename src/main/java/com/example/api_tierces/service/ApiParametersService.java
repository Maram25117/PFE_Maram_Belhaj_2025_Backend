package com.example.api_tierces.service;

import com.example.api_tierces.model.ApiParameters;
import com.example.api_tierces.repository.ApiParametersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ApiParametersService {

    @Autowired
    private ApiParametersRepository apiParametersRepository;


    public List<ApiParameters> getAllParameters() {
        return apiParametersRepository.findAll();
    }


    public Optional<ApiParameters> getParameterById(Long id) {
        return apiParametersRepository.findById(id);
    }


    public List<ApiParameters> getParametersByApiId(Long apiId) {
        return apiParametersRepository.findByApiId(apiId);
    }
}

