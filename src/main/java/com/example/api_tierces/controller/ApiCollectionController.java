/*package com.example.api_tierces.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/collections")
public class ApiCollectionController {

    @Autowired
    private ApiCollectionRepository apiCollectionRepository;

    private static final String POSTMAN_COLLECTION_PATH = "src/main/resources/postman_collection.json";

    // 📌 **Récupérer la liste de toutes les collections**
    @GetMapping
    public ResponseEntity<List<ApiCollection>> getAllCollections() {
        List<ApiCollection> collections = apiCollectionRepository.findAll();
        return new ResponseEntity<>(collections, HttpStatus.OK);
    }

    // 📌 **Récupérer une collection par ID**
    @GetMapping("/{id}")
    public ResponseEntity<?> getCollectionById(@PathVariable Long id) {
        Optional<ApiCollection> collection = apiCollectionRepository.findById(id);
        if (collection.isPresent()) {
            return new ResponseEntity<>(collection.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Collection not found", HttpStatus.NOT_FOUND);
        }
    }

    // 📌 **Télécharger le fichier JSON de la collection par ID**
    /*@GetMapping("/download/{id}")
    public ResponseEntity<?> downloadCollection(@PathVariable Long id) {
        Optional<ApiCollection> apiCollection = apiCollectionRepository.findById(id);

        if (!apiCollection.isPresent()) {
            return new ResponseEntity<>("Collection not found", HttpStatus.NOT_FOUND);
        }

        String fileName = apiCollection.get().getCollection();
        Path filePath = Paths.get(POSTMAN_COLLECTION_PATH);

        Resource resource;
        try {
            resource = new UrlResource(filePath.toUri());
        } catch (MalformedURLException e) {
            return new ResponseEntity<>("File not found", HttpStatus.NOT_FOUND);
        }

        if (resource.exists() && resource.isReadable()) {
            String contentType = "application/json"; // Définir le Content-Type approprié
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } else {
            return new ResponseEntity<>("Could not read file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }*/
   /* @GetMapping("/download/{id}")
    public ResponseEntity<?> downloadCollection(@PathVariable Long id) {
        Optional<ApiCollection> apiCollection = apiCollectionRepository.findById(id);

        if (!apiCollection.isPresent()) {
            return new ResponseEntity<>("Collection not found", HttpStatus.NOT_FOUND);
        }

        String fileName = apiCollection.get().getCollection();  // Récupérer le nom du fichier
        String collectionData = apiCollection.get().getCollection(); // Récupérer le contenu JSON

        // Définir le Content-Type et le Content-Disposition
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setContentDispositionFormData("attachment", fileName); // Utiliser le nom du fichier de la base

        return new ResponseEntity<>(collectionData, headers, HttpStatus.OK);
    }

    // 📌 **Télécharger le fichier JSON actuel de la collection**
    @GetMapping("/download")
    public ResponseEntity<?> downloadCurrentCollection() {

        Path filePath = Paths.get(POSTMAN_COLLECTION_PATH);

        Resource resource;
        try {
            resource = new UrlResource(filePath.toUri());
        } catch (MalformedURLException e) {
            return new ResponseEntity<>("File not found", HttpStatus.NOT_FOUND);
        }

        if (resource.exists() && resource.isReadable()) {
            String contentType = "application/json"; // Définir le Content-Type approprié
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } else {
            return new ResponseEntity<>("Could not read file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}*/
