package com.example.api_tierces.controller;

import com.example.api_tierces.service.UploadService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.scheduling.annotation.Scheduled;
import com.example.api_tierces.service.SwaggerProcessingService;
import org.springframework.web.bind.annotation.RestController;


// extracte les données automatiquement a partir de l'url définit (controle automatique pour détecter les changements)
@Tag(name = "A-FileUpload , Téléchargement d'un fichier swagger")
@RestController
@RequestMapping("/api")
public class FileUploadController {

    @Autowired
    private SwaggerProcessingService swaggerProcessingService;

    @Autowired
    private UploadService apiService;

    // Tâche planifiée qui s'exécute chaque une heure
    @Scheduled(cron = "0 0 * * * *") /*0 0 chaque heure*/
    public void scheduleSwaggerProcessing() {
        try {
            swaggerProcessingService.processSwaggerDaily();
            System.out.println("Swagger processed successfully.");
        } catch (Exception e) {
            System.out.println("Erreur lors du traitement du fichier Swagger: " + e.getMessage());
        }
    }

}
