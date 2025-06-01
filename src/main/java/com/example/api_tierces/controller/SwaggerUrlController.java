package com.example.api_tierces.controller;

import com.example.api_tierces.model.Swagger; // Ajustez le chemin
import com.example.api_tierces.repository.SwaggerUrlRepository; // Ajustez le chemin
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.example.api_tierces.service.FileUploadService;
import com.example.api_tierces.service.SwaggerProcessingService;

import java.net.URL;
import java.util.List;
import java.util.Optional;

@Tag(name = "Swagger URL")
@RestController
@RequestMapping("/api/swagger-urls")
public class SwaggerUrlController {

    @Autowired
    private SwaggerUrlRepository swaggerUrlRepository;

    @Autowired
    private SwaggerUrlRepository repository;

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private SwaggerProcessingService swaggerProcessingService;

    public SwaggerUrlController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    // Enregistrement du swagger url dans la base
    @Operation(summary = "Enregistrement de l'URL Swagger dans la base")
    @PostMapping
    public ResponseEntity<Swagger> createSwaggerUrl(@RequestBody Swagger newSwaggerUrl) {
        if (newSwaggerUrl.getId() == null || newSwaggerUrl.getId().trim().isEmpty() ||
                newSwaggerUrl.getUrl() == null || newSwaggerUrl.getUrl().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        if (swaggerUrlRepository.existsById(newSwaggerUrl.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Un enregistrement avec l'ID '" + newSwaggerUrl.getId() + "' existe déjà.");
        }
        Swagger savedSwaggerUrl = swaggerUrlRepository.save(newSwaggerUrl);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedSwaggerUrl);
    }

    // Modification du swagger url dans la base et enregistrement des données dans la base et enregistrement du swagger file de l'url en local
    @Operation(summary = "Modification de l'URL Swagger")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateSwaggerUrl(@PathVariable String id, @RequestBody Swagger updatedSwagger) {
        if (!isValidUrl(updatedSwagger.getUrl())) {
            return ResponseEntity.badRequest().body(new ErrorResponse("L'URL fournie est invalide."));
        }

        try {
            swaggerProcessingService.extractSwaggerData(updatedSwagger.getUrl());
            System.out.println("Données extraites avec succès.");

            String fileName = "swagger_" + id + ".json";
            swaggerProcessingService.downloadSwaggerFileToDownloads(updatedSwagger.getUrl(), fileName);

        } catch (Exception e) {
            System.out.println("Erreur : " + e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse("Erreur lors de l'extraction ou du téléchargement du fichier Swagger."));
        }

        return repository.findById(id)
                .map(swagger -> {
                    swagger.setUrl(updatedSwagger.getUrl());
                    Swagger saved = repository.save(swagger);
                    return ResponseEntity.ok(new SuccessResponse("URL enregistrée, données extraites et fichier Swagger téléchargé dans Téléchargements."));
                })
                .orElse(ResponseEntity.notFound().build());
    }


    // Classe de réponse pour succès
    public static class SuccessResponse {
        private String message;

        public SuccessResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    // Classe de réponse pour erreur
    public static class ErrorResponse {
        private String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }
    }


    // Méthode pour vérifier la validité de l'URL
    private boolean isValidUrl(String url) {
        try {
            new URL(url);
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    @Operation(summary = "Récupérer l'url swagger par id ")
    @GetMapping("/{id}")
    public ResponseEntity<Swagger> getSwaggerUrlById(@PathVariable String id) {
        Optional<Swagger> swaggerUrlOptional = swaggerUrlRepository.findById(id);

        if (swaggerUrlOptional.isPresent()) {
            return ResponseEntity.ok(swaggerUrlOptional.get());
        } else {
            return ResponseEntity.notFound().build();
        }

    }

    @Operation(summary = "Récupérer URL Swagger")
    @GetMapping
    public ResponseEntity<List<Swagger>> getAllSwaggerUrls() {
        List<Swagger> urls = swaggerUrlRepository.findAll();
        return ResponseEntity.ok(urls);
    }

    @Operation(summary = "Supprimer URL Swagger")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSwaggerUrl(@PathVariable String id) {
        if (!swaggerUrlRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        swaggerUrlRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
