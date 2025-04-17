/*package com.example.api_tierces.controller;

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

    @PostMapping(value = "/process-file", consumes ="multipart/form-data")
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

    @Operation(summary = "Téléchargement de la collection créer en local", description = "Permet le téléchargement de la collection créer et stocker dans la partie ressource en local.")
    @GetMapping("/download-file")
    public ResponseEntity<byte[]> downloadFile() {
        try {
            // Spécifiez le chemin du fichier dans `resources`
            Resource resource = new ClassPathResource("p_c.json");//esm l fichier li fi ressource

            // Lire le contenu du fichier
            byte[] fileContent = Files.readAllBytes(resource.getFile().toPath());

            // Définir les en-têtes pour le téléchargement
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setContentDispositionFormData("attachment", "p_c.json");


            return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Erreur lors du téléchargement du fichier : " + e.getMessage()).getBytes());
        }
    }
}*/
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
import com.example.api_tierces.service.PostmanProcessingService;

import java.io.IOException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.charset.StandardCharsets;
import org.json.JSONArray;
import org.json.JSONObject;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.oas.models.OpenAPI;
import java.nio.charset.StandardCharsets;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;


@Tag(name = "A-Téléchargement de la Collection Postman")
@RestController
@RequestMapping("/api/postman")
public class PostmanController {

    private static final String POSTMAN_COLLECTION_FILENAME = "postman_collection.json"; // Nom du fichier changé
    private static final Path POSTMAN_COLLECTION_PATH;

    static {
        // Récupérer le chemin du répertoire resources
        String resourcesPath = null;
        try {
            Resource resource = new ClassPathResource("application.properties"); // Un fichier quelconque dans resources
            File resourcesDir = resource.getFile().getParentFile();
            resourcesPath = resourcesDir.getAbsolutePath();
        } catch (IOException e) {
            // Gérer l'erreur si le répertoire resources ne peut pas être déterminé
            System.err.println("Erreur lors de la récupération du répertoire resources : " + e.getMessage());
            // Définir un chemin par défaut ou lancer une exception, selon votre besoin
            resourcesPath = System.getProperty("java.io.tmpdir"); // Chemin temporaire par défaut
        }

        POSTMAN_COLLECTION_PATH = Paths.get(resourcesPath, POSTMAN_COLLECTION_FILENAME);
    }


    @Autowired
    private PostmanProcessingService postmanProcessingService;

    @Operation(summary = "Importer une collection Postman et la stocker", description = "Permet d'importer une collection Postman et de la stocker dans le répertoire resources sous le nom collection_postman.json ")
    @PostMapping(value = "/upload-collection", consumes = "multipart/form-data")
    public ResponseEntity<String> uploadPostmanCollection(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Le fichier est vide.");
        }

        try (InputStream inputStream = file.getInputStream()) {
            // Supprimer l'ancien fichier s'il existe
            if (Files.exists(POSTMAN_COLLECTION_PATH)) {
                try {
                    Files.delete(POSTMAN_COLLECTION_PATH);
                    System.out.println("Ancien fichier " + POSTMAN_COLLECTION_FILENAME + " supprimé.");
                } catch (IOException e) {
                    System.err.println("Erreur lors de la suppression de l'ancien fichier : " + e.getMessage());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Erreur lors de la suppression de l'ancien fichier : " + e.getMessage());
                }
            }
            // Copier le contenu du fichier vers le chemin spécifié
            Files.copy(inputStream, POSTMAN_COLLECTION_PATH, StandardCopyOption.REPLACE_EXISTING);


            return ResponseEntity.ok("Collection Postman téléchargée et stockée avec succès dans : " + POSTMAN_COLLECTION_PATH.toString());
        } catch (IOException e) {
            System.err.println("Erreur lors du téléchargement ou de la sauvegarde du fichier : " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors du téléchargement ou de la sauvegarde du fichier : " + e.getMessage());
        }
    }

    @PostMapping(value = "/process-file", consumes ="multipart/form-data")
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

    @Operation(summary = "Téléchargement de la collection créer en local", description = "Permet le téléchargement de la collection créer et stocker dans la partie ressource en local.")
    @GetMapping("/download-file")
    public ResponseEntity<byte[]> downloadFile() {
        try {
            // Spécifiez le chemin du fichier dans `resources`
            Resource resource = new ClassPathResource(POSTMAN_COLLECTION_FILENAME);

            // Lire le contenu du fichier
            byte[] fileContent = Files.readAllBytes(resource.getFile().toPath());

            // Définir les en-têtes pour le téléchargement
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setContentDispositionFormData("attachment", POSTMAN_COLLECTION_FILENAME);


            return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Erreur lors du téléchargement du fichier : " + e.getMessage()).getBytes());
        }
    }



        /*@PostMapping(value = "/convert",  consumes ="multipart/form-data")
        public ResponseEntity<Resource> convertSwaggerToPostman(@RequestParam("file") MultipartFile file) {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(null);
            }

            try {
                // Lire le Swagger JSON
                String swaggerJson = new String(file.getBytes(), StandardCharsets.UTF_8);

                // Parser le Swagger
                OpenAPI openAPI = new OpenAPIV3Parser().readContents(swaggerJson, null, null).getOpenAPI();

                if (openAPI == null) {
                    return ResponseEntity.badRequest().body(null);
                }

                // Créer la collection Postman
                JSONObject postmanCollection = new JSONObject();
                postmanCollection.put("info", new JSONObject()
                        .put("name", openAPI.getInfo().getTitle())
                        .put("schema", "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"));

                JSONArray items = new JSONArray();

                openAPI.getPaths().forEach((path, pathItem) -> {
                    if (pathItem.readOperationsMap() != null) {
                        pathItem.readOperationsMap().forEach((method, operation) -> {
                            JSONObject request = new JSONObject();
                            request.put("method", method.name().toUpperCase());

                            JSONObject url = new JSONObject();
                            url.put("raw", "{{base_url}}" + path);
                            url.put("host", new JSONArray().put("{{base_url}}"));
                            url.put("path", new JSONArray(List.of(path.replaceFirst("/", "").split("/"))));

                            request.put("url", url);
                            request.put("header", new JSONArray());
                            request.put("body", new JSONObject());
                            request.put("description", operation.getSummary());

                            JSONObject item = new JSONObject();
                            item.put("name", operation.getSummary() != null ? operation.getSummary() : method.name() + " " + path);
                            item.put("request", request);

                            items.put(item);
                        });
                    }
                });

                postmanCollection.put("item", items);

                // Retourner le JSON sous forme de fichier téléchargeable
                byte[] jsonBytes = postmanCollection.toString(2).getBytes(StandardCharsets.UTF_8);
                ByteArrayResource resource = new ByteArrayResource(jsonBytes);

                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=collection_postman.json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(resource);

            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }
        }*/




}