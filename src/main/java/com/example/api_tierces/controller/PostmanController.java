package com.example.api_tierces.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.api_tierces.service.PostmanProcessingService;

import java.io.IOException;

@RestController
@RequestMapping("/api/postman")
public class PostmanController {

    @Autowired
    private PostmanProcessingService postmanProcessingService;

    @PostMapping(value = "/process-file", consumes = "multipart/form-data")
    public String processPostmanCollection(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return "Le fichier est vide.";
        }

        try {
            // Lire le contenu du fichier JSON en tant que chaîne de caractères
            String postmanCollectionJson = new String(file.getBytes());
            return postmanProcessingService.processPostmanCollection(postmanCollectionJson);
        } catch (IOException e) {
            return "Erreur lors du traitement du fichier : " + e.getMessage();
        }
    }
}