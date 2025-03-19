/*package com.example.api_tierces.controller;

import com.example.api_tierces.service.UploadService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*; //définir les points d'entrée de l'API (GET, POST ...)
import org.springframework.web.multipart.MultipartFile; //gérer les fichiers envoyés via des requêtes HTTP
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.io.IOException;

@RestController
@RequestMapping("/api")
public class FileUploadController {

    @Autowired
    private UploadService apiService;

    @Operation(summary = "Upload un fichier swagger", description = "Permet d'uploader un fichier Swagger pour le traitement.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fichier téléchargé avec succès"),
            @ApiResponse(responseCode = "400", description = "Fichier vide")
    })
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
    }
}*/
package com.example.api_tierces.controller;

import com.example.api_tierces.service.UploadService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import com.example.api_tierces.service.SwaggerProcessingService;
/*@RestController
@RequestMapping("/api")
public class FileUploadController {

    @Autowired
    private UploadService apiService;

    @Operation(summary = "Télécharge un fichier Swagger depuis une URL", description = "Permet de télécharger et traiter un fichier Swagger à partir d'une URL.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fichier téléchargé et traité avec succès"),
            @ApiResponse(responseCode = "400", description = "URL invalide ou erreur lors du téléchargement")
    })
    @GetMapping("/process-swagger-url") // Changed to GET for receiving URL as parameter
    public ResponseEntity<String> processSwaggerFromUrl(@RequestParam("url") String swaggerUrl) {
        try {
            String result = apiService.parseSwaggerFileFromUrl(swaggerUrl);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors du traitement du fichier : " + e.getMessage());
        }
    }
}*/
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.net.URL;
import java.io.IOException;

@RestController
@RequestMapping("/api")
public class FileUploadController {

    @Autowired
    private SwaggerProcessingService swaggerProcessingService;

    @Autowired
    private UploadService apiService;

    // Tâche planifiée qui s'exécute toutes les 10 secondes
    @Scheduled(cron = "0 0 */6 * * *")
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
    @GetMapping("/process-swagger-url")
    public ResponseEntity<String> processSwaggerFromUrl(@RequestParam("url") String swaggerUrl) {
        try {
            String result = apiService.parseSwaggerFileFromUrl(swaggerUrl);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors du traitement du fichier : " + e.getMessage());
        }
    }
}