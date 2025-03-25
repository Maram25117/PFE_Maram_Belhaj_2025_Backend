package com.example.api_tierces.controller;

import com.example.api_tierces.model.ApiParameters;
import com.example.api_tierces.service.ApiParametersService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Tag(name = "Table Api-Parameters")
@RestController
@RequestMapping("/api/parameters")
public class ApiParametersController {

    @Autowired
    private ApiParametersService apiParametersService;

    // Récupérer tous les paramètres
    @GetMapping
    public List<ApiParameters> getAllParameters() {
        return apiParametersService.getAllParameters();
    }

    // Récupérer un paramètre par son ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiParameters> getParameterById(@PathVariable Long id) {
        Optional<ApiParameters> parameter = apiParametersService.getParameterById(id);
        return parameter.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Récupérer les paramètres d'un API donné
    @GetMapping("/api/{apiId}")
    public List<ApiParameters> getParametersByApiId(@PathVariable Long apiId) {
        return apiParametersService.getParametersByApiId(apiId);
    }
}

