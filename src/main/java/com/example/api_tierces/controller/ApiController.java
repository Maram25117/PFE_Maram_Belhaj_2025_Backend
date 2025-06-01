package com.example.api_tierces.controller;

import com.example.api_tierces.model.Api;
import com.example.api_tierces.service.ApiService;
import com.example.api_tierces.service.UploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Api")
@RestController
@RequestMapping("/api/apis")
public class ApiController {

    @Autowired
    private ApiService apiService;

    @Operation(summary = "Récupérer toutes les API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Opération réussie",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Api.class)))
    })
    @GetMapping
    public List<Api> getAllApis() {
        return apiService.getAllApis();
    }

    @Operation(summary = "Récupérer une API par son ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "API trouvée",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Api.class))),
            @ApiResponse(responseCode = "404", description = "API non trouvée")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Api> getApiById(
            @Parameter(description = "L'ID de l'API à récupérer", required = true) @PathVariable Long id) {
        return apiService.getApiById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Récupérer une API par son chemin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "API trouvée",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Api.class))),
            @ApiResponse(responseCode = "404", description = "API non trouvée")
    })
    @GetMapping("/path/{path}")
    public ResponseEntity<Api> getApiByPath(
            @Parameter(description = "Le chemin de l'API à récupérer", required = true) @PathVariable String path) {
        return apiService.getApiByPath(path)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

