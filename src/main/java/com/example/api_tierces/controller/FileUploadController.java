package com.example.api_tierces.controller;

import com.example.api_tierces.service.UploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import com.example.api_tierces.service.SwaggerProcessingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Tag(name = "A-FileUpload , Téléchargement d'un fichier swagger")
@RestController
@RequestMapping("/api")
public class FileUploadController {

    @Autowired
    private SwaggerProcessingService swaggerProcessingService;

    @Autowired
    private UploadService apiService;

    // Tâche planifiée qui s'exécute toutes les 10 secondes
    @Scheduled(cron = "0 0 * * * *")
    public void scheduleSwaggerProcessing() {
        try {
            // Appelez directement la méthode du service pour traiter Swagger
            swaggerProcessingService.processSwaggerDaily();
            System.out.println("Swagger processed successfully.");
        } catch (Exception e) {
            System.out.println("Erreur lors du traitement du fichier Swagger: " + e.getMessage());
        }
    }

    // Méthode pour appeler manuellement l'URL si nécessaire (en option)
   /* @Operation(summary = "Upload un fichier swagger via url", description = "Permet d'uploader un fichier Swagger pour le traitement.")
    @GetMapping("/process-swagger-url")
    public ResponseEntity<String> processSwaggerFromUrl(@RequestParam("url") String swaggerUrl) {
        try {
            String result = apiService.parseSwaggerFileFromUrl(swaggerUrl);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors du traitement du fichier : " + e.getMessage());
        }
    }

    @Operation(summary = "Upload un fichier swagger", description = "Permet d'uploader un fichier Swagger pour le traitement.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fichier téléchargé avec succès"),
            @ApiResponse(responseCode = "400", description = "Fichier vide")
    })
    // option : télécharger un fichier swagger
    @PostMapping(value = "/upload-file", consumes = "multipart/form-data")
    public String uploadSwaggerFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return "Le fichier est vide.";
        }

        try {
            String fileContent = new String(file.getBytes()); //getBytes :c'est une méthode de la classe MultipartFile,convertir le contenu du fichier en un tableau de bytes
            return apiService.parseSwaggerFile(fileContent);
        } catch (IOException e) {
            return "Erreur lors du traitement du fichier : " + e.getMessage();
        }
    }*/
}
