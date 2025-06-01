package com.example.api_tierces.controller;

import com.example.api_tierces.model.ApiResponse;
import com.example.api_tierces.service.ApiResponseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Tag(name = "Api Response")
@RestController
@RequestMapping("/api/responses")
public class ApiResponseController {

    @Autowired
    private ApiResponseService apiResponseService;

    @Operation(summary = "Récupérer toutes les réponses")
    @GetMapping
    public List<ApiResponse> getAll() {
        return apiResponseService.getAllResponses();
    }

    @Operation(summary = "Récupérer une réponse par son id")
    @GetMapping("/{id}")
    public Optional<ApiResponse> getById(@PathVariable Long id) {
        return apiResponseService.getResponseById(id);
    }

    @Operation(summary = "Récupérer une réponse par son Api id")
    @GetMapping("/api/{apiId}")
    public List<ApiResponse> getByApiId(@PathVariable Long apiId) {
        return apiResponseService.getResponsesByApiId(apiId);
    }
}


