package com.example.api_tierces.controller;

import com.example.api_tierces.model.ApiParameters;
import com.example.api_tierces.service.ApiParametersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Tag(name = "Api Parameters")
@RestController
@RequestMapping("/api/parameters")
public class ApiParametersController {

    @Autowired
    private ApiParametersService apiParametersService;

    @Operation(summary = "Récupérer tous les paramétres")
    @GetMapping
    public List<ApiParameters> getAllParameters() {
        return apiParametersService.getAllParameters();
    }

    @Operation(summary = "Récupérer un paramétre par son id")
    @GetMapping("/{id}")
    public ResponseEntity<ApiParameters> getParameterById(@PathVariable Long id) {
        Optional<ApiParameters> parameter = apiParametersService.getParameterById(id);
        return parameter.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Récupérer un paramétre par son Api id")
    @GetMapping("/api/{apiId}")
    public List<ApiParameters> getParametersByApiId(@PathVariable Long apiId) {
        return apiParametersService.getParametersByApiId(apiId);
    }
}

