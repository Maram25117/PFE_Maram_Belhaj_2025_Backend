package com.example.api_tierces.controller;

import com.example.api_tierces.service.ApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*; //définir les points d'entrée de l'API (GET, POST ...)
import org.springframework.web.multipart.MultipartFile; //gérer les fichiers envoyés via des requêtes HTTP

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private ApiService apiService;

    @PostMapping("/upload_lib_api")
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
}
