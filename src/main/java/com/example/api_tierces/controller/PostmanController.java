package com.example.api_tierces.controller;

import com.example.api_tierces.service.PostmanProcessingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;

@Tag(name = "A-Téléchargement de la Collection Postman")
@RestController
@RequestMapping("/api/postman")
public class PostmanController {

    @Autowired
    private PostmanProcessingService postmanProcessingService;

    /*@PostMapping(value = "/process-file", consumes ="multipart/form-data")
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
    }*/

    @Operation(summary = "Téléchargement de la collection créer en local", description = "Permet le téléchargement de la collection créer et stocker dans la partie ressource en local.")
    @GetMapping("/download-file")
    public ResponseEntity<byte[]> downloadFile() {
        try {
            // Spécifiez le chemin du fichier dans `resources`
            Resource resource = new ClassPathResource("postman_collection.json");

            // Lire le contenu du fichier
            byte[] fileContent = Files.readAllBytes(resource.getFile().toPath());

            // Définir les en-têtes pour le téléchargement
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setContentDispositionFormData("attachment", "postman_collection.json");


            return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Erreur lors du téléchargement du fichier : " + e.getMessage()).getBytes());
        }
    }
}