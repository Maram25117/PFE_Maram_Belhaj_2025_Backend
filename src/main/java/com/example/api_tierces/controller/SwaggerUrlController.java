package com.example.api_tierces.controller;


import com.example.api_tierces.model.Swagger; // Ajustez le chemin
import com.example.api_tierces.repository.SwaggerUrlRepository; // Ajustez le chemin
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.example.api_tierces.service.FileUploadService;
import com.example.api_tierces.service.SwaggerProcessingService;

import java.net.URL;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/swagger-urls") // Préfixe pour tous les endpoints de ce contrôleur
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
    // --- Endpoint POST pour ajouter une nouvelle URL Swagger ---
    @PostMapping
    public ResponseEntity<Swagger> createSwaggerUrl(@RequestBody Swagger newSwaggerUrl) {
        // Validation simple (peut être améliorée avec @Valid et des annotations sur l'entité/DTO)
        if (newSwaggerUrl.getId() == null || newSwaggerUrl.getId().trim().isEmpty() ||
                newSwaggerUrl.getUrl() == null || newSwaggerUrl.getUrl().trim().isEmpty()) {
            return ResponseEntity.badRequest().build(); // Mauvaise requête si ID ou URL manquant
        }
        // Vérifier si l'ID existe déjà (optionnel, dépend de votre logique métier)
        if (swaggerUrlRepository.existsById(newSwaggerUrl.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Un enregistrement avec l'ID '" + newSwaggerUrl.getId() + "' existe déjà.");
            // Alternativement: return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Swagger savedSwaggerUrl = swaggerUrlRepository.save(newSwaggerUrl);
        // Retourne l'entité sauvegardée avec le statut 201 Created
        return ResponseEntity.status(HttpStatus.CREATED).body(savedSwaggerUrl);
    }

    /*@PutMapping("/{id}")
    public ResponseEntity<?> updateSwaggerUrl(@PathVariable String id, @RequestBody Swagger updatedSwagger) {
        return repository.findById(id)
                .map(swagger -> {
                    swagger.setUrl(updatedSwagger.getUrl());
                    Swagger saved = repository.save(swagger);

                    // Appel explicite de la méthode pour traiter le fichier Swagger
                    try {
                        swaggerProcessingService.processSwaggerDaily();
                        System.out.println("Swagger processed successfully.");
                    } catch (Exception e) {
                        System.out.println("Erreur lors du traitement du fichier Swagger: " + e.getMessage());
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Erreur lors du traitement du fichier Swagger: " + e.getMessage());
                    }

                    return ResponseEntity.ok(saved);
                })
                .orElse(ResponseEntity.notFound().build());
    }*/
    /*@PutMapping("/{id}")
    public ResponseEntity<?> updateSwaggerUrl(@PathVariable String id, @RequestBody Swagger updatedSwagger) {
        // Vérification de la validité de l'URL
        if (!isValidUrl(updatedSwagger.getUrl())) {
            return ResponseEntity.badRequest().body("L'URL fournie est invalide.");
        }

        return repository.findById(id)
                .map(swagger -> {
                    swagger.setUrl(updatedSwagger.getUrl());
                    Swagger saved = repository.save(swagger);

                    // Appel explicite de la méthode pour traiter le fichier Swagger
                    try {
                        swaggerProcessingService.processSwaggerDaily();
                        System.out.println("Swagger processed successfully.");
                    } catch (Exception e) {
                        System.out.println("Erreur lors du traitement du fichier Swagger: " + e.getMessage());
                        // Retourne un message d'erreur spécifique au lieu de 500
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body("Impossible d'extraire les données du fichier Swagger.");
                    }

                    return ResponseEntity.ok(saved);
                })
                .orElse(ResponseEntity.notFound().build());
    }*/
    /*@PutMapping("/{id}")
    public ResponseEntity<?> updateSwaggerUrl(@PathVariable String id, @RequestBody Swagger updatedSwagger) {
        // Vérification de la validité de l'URL
        if (!isValidUrl(updatedSwagger.getUrl())) {
            return ResponseEntity.badRequest().body(new ErrorResponse("L'URL fournie est invalide."));
        }

        // Essayer d'extraire les données de l'URL avant d'enregistrer
        try {
            // Utiliser la méthode extractSwaggerData pour traiter les données de l'URL
            swaggerProcessingService.extractSwaggerData(updatedSwagger.getUrl());
            System.out.println("Données extraites avec succès.");
        } catch (Exception e) {
            System.out.println("Erreur lors de l'extraction des données Swagger: " + e.getMessage());
            // Retourne une réponse BAD_REQUEST si l'extraction échoue
            return ResponseEntity.badRequest().body(new ErrorResponse("Impossible d'extraire les données du fichier Swagger."));
        }

        return repository.findById(id)
                .map(swagger -> {
                    // Enregistrer l'URL après validation et extraction des données
                    swagger.setUrl(updatedSwagger.getUrl());
                    Swagger saved = repository.save(swagger);

                    // Réponse de succès après enregistrement en base
                    return ResponseEntity.ok(new SuccessResponse("URL enregistrée et données extraites avec succès."));
                })
                .orElse(ResponseEntity.notFound().build());
    }*/
    @PutMapping("/{id}")
    public ResponseEntity<?> updateSwaggerUrl(@PathVariable String id, @RequestBody Swagger updatedSwagger) {
        if (!isValidUrl(updatedSwagger.getUrl())) {
            return ResponseEntity.badRequest().body(new ErrorResponse("L'URL fournie est invalide."));
        }

        try {
            swaggerProcessingService.extractSwaggerData(updatedSwagger.getUrl());
            System.out.println("Données extraites avec succès.");

            // Nom du fichier à sauvegarder (tu peux le personnaliser)
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
            new URL(url); // Essaie de créer un objet URL à partir de la chaîne
            return true;
        } catch (Exception e) {
            return false; // Si une exception est levée, l'URL n'est pas valide
        }
    }



    // --- Endpoint GET pour récupérer une URL par son ID ---
    @GetMapping("/{id}")
    public ResponseEntity<Swagger> getSwaggerUrlById(@PathVariable String id) {
        Optional<Swagger> swaggerUrlOptional = swaggerUrlRepository.findById(id);

        if (swaggerUrlOptional.isPresent()) {
            return ResponseEntity.ok(swaggerUrlOptional.get());
        } else {
            // Retourne 404 Not Found si l'ID n'existe pas
            return ResponseEntity.notFound().build();
        }
        // Alternative plus concise avec orElseThrow:
        // SwaggerUrl swaggerUrl = swaggerUrlRepository.findById(id)
        //       .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "URL Swagger non trouvée avec l'ID: " + id));
        // return ResponseEntity.ok(swaggerUrl);
    }

    // --- Endpoint GET pour récupérer toutes les URLs (optionnel) ---
    @GetMapping
    public ResponseEntity<List<Swagger>> getAllSwaggerUrls() {
        List<Swagger> urls = swaggerUrlRepository.findAll();
        return ResponseEntity.ok(urls);
    }

    // --- Endpoint DELETE pour supprimer une URL par ID (optionnel) ---
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSwaggerUrl(@PathVariable String id) {
        if (!swaggerUrlRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        swaggerUrlRepository.deleteById(id);
        return ResponseEntity.noContent().build(); // Statut 204 No Content
    }


    /*@PutMapping("/{id}")
    public ResponseEntity<Swagger> updateSwaggerUrl(@PathVariable String id, @RequestBody Swagger updatedSwagger) {
        return repository.findById(id)
                .map(swagger -> {
                    swagger.setUrl(updatedSwagger.getUrl());
                    Swagger saved = repository.save(swagger);
                    return ResponseEntity.ok(saved);
                })
                .orElse(ResponseEntity.notFound().build());
    }*/



}
