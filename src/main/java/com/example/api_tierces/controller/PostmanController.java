package com.example.api_tierces.controller;

import com.example.api_tierces.service.PostmanProcessingService;
import com.fasterxml.jackson.core.JsonProcessingException;
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Tag(name = "A-Gestion de la Collection Postman")
@RestController
@RequestMapping("/api/postman")
public class PostmanController {

    private static final String POSTMAN_COLLECTION_FILENAME = "postman_collection.json";
    private static final Path POSTMAN_COLLECTION_PATH; //variable finale n’est pas encore initialisée


    /*static {
        // Récupérer le chemin du répertoire resources
        String resourcesPath = null;
        try {
            Resource resource = new ClassPathResource("application.properties");
            File resourcesDir = resource.getFile().getParentFile();
            resourcesPath = resourcesDir.getAbsolutePath();
        } catch (IOException e) {
            System.err.println("Erreur lors de la récupération du répertoire resources : " + e.getMessage());
            resourcesPath = System.getProperty("java.io.tmpdir");
        }
        POSTMAN_COLLECTION_PATH = Paths.get(resourcesPath, POSTMAN_COLLECTION_FILENAME);
    }*/
    static {
        Path path = null;
        try {
            // Récupère la racine du projet
            Path projectRoot = Paths.get(System.getProperty("user.dir"));

            // Chemin vers src/main/resources/postman/
            Path postmanDir = projectRoot.resolve("src/main/resources/postman");

            // Crée le dossier s’il n’existe pas
            if (!Files.exists(postmanDir)) {
                Files.createDirectories(postmanDir);
            }

            // Chemin complet vers le fichier .json
            path = postmanDir.resolve(POSTMAN_COLLECTION_FILENAME);

        } catch (IOException e) {
            System.err.println("Erreur lors de l’accès à src/main/resources/postman : " + e.getMessage());
            // Si tu ne veux pas de fallback : laisse path à null ou gère l’erreur selon ton besoin
        }

        POSTMAN_COLLECTION_PATH = path;
    }


    @Autowired
    private PostmanProcessingService postmanProcessingService;
    // importer une collection postman prete a l'emploi
    @Operation(summary = "Importer une collection Postman Prete a l'emploi", description = "Permet d'importer une collection Postman prete a l'emploi et de la stocker dans le répertoire resources sous le nom collection_postman.json ")
    /*@PostMapping(value = "/upload-collection", consumes = "multipart/form-data")
    public ResponseEntity<String> uploadPostmanCollection(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Le fichier est vide.");
        }
        try (InputStream inputStream = file.getInputStream()) {
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
            Files.copy(inputStream, POSTMAN_COLLECTION_PATH, StandardCopyOption.REPLACE_EXISTING);

            return ResponseEntity.ok("Collection Postman téléchargée et stockée avec succès dans : " + POSTMAN_COLLECTION_PATH.toString());
        } catch (IOException e) {
            System.err.println("Erreur lors du téléchargement ou de la sauvegarde du fichier : " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors du téléchargement ou de la sauvegarde du fichier : " + e.getMessage());
        }
    }*/
    /*ne vérifie pas collection postman*/
    /*@PostMapping(value = "/upload-collection", consumes = "multipart/form-data")
    public ResponseEntity<String> uploadPostmanCollection(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Le fichier est vide.");
        }

        try (InputStream inputStream = file.getInputStream()) {
            // Lire le contenu JSON
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(inputStream);

            // Vérifier la présence de scripts de test
            if (!root.has("item") || !containsTestScript(root.get("item"))) {
                return ResponseEntity.badRequest().body("Erreur : La collection ne contient aucun script de test.");
            }

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

            try (InputStream inputStreamForSave = file.getInputStream()) {
                Files.copy(inputStreamForSave, POSTMAN_COLLECTION_PATH, StandardCopyOption.REPLACE_EXISTING);
            }

            return ResponseEntity.ok("Collection Postman avec scripts de test téléchargée et stockée avec succès dans : "
                    + POSTMAN_COLLECTION_PATH.toString());

        } catch (IOException e) {
            System.err.println("Erreur lors du traitement du fichier : " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors du traitement du fichier : " + e.getMessage());
        }
    }*/
    @PostMapping(value = "/upload-collection", consumes = "multipart/form-data")
    public ResponseEntity<String> uploadPostmanCollection(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Le fichier est vide.");
        }

        try (InputStream inputStream = file.getInputStream()) {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(inputStream);

            // Vérifier que c'est une collection Postman valide
            JsonNode infoNode = root.path("info");
            String schema = infoNode.path("schema").asText();

            if (schema == null || !schema.contains("postman.com/json/collection")) {
                return ResponseEntity.badRequest().body("Erreur : Le fichier fourni n'est pas une collection Postman valide.");
            }

            if (!root.has("item") || !containsTestScript(root.get("item"))) {
                return ResponseEntity.badRequest().body("Erreur : La collection ne contient aucun script de test.");
            }

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

            try (InputStream inputStreamForSave = file.getInputStream()) {
                Files.copy(inputStreamForSave, POSTMAN_COLLECTION_PATH, StandardCopyOption.REPLACE_EXISTING);
            }

            return ResponseEntity.ok("Collection Postman avec scripts de test stockée avec succès : "
                    + POSTMAN_COLLECTION_PATH.toString());

        } catch (IOException e) {
            System.err.println("Erreur lors du traitement du fichier : " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors du traitement du fichier : " + e.getMessage());
        }
    }


    /*private boolean containsTestScript(JsonNode items) {
        for (JsonNode item : items) {
            // Vérifie s’il y a un événement "test"
            if (item.has("event")) {
                for (JsonNode event : item.get("event")) {
                    if ("test".equals(event.path("listen").asText())
                            && event.has("script")
                            && event.get("script").has("exec")
                            && event.get("script").get("exec").isArray()
                            && event.get("script").get("exec").size() > 0) {
                        return true;
                    }
                }
            }

            // Vérifie les sous-items récursivement
            if (item.has("item")) {
                if (containsTestScript(item.get("item"))) {
                    return true;
                }
            }
        }
        return false;
    }*/
    private boolean containsTestScript(JsonNode items) {
        for (JsonNode item : items) {
            if (item.has("event")) {
                for (JsonNode event : item.get("event")) {
                    if ("test".equals(event.path("listen").asText())
                            && event.has("script")
                            && event.get("script").has("exec")) {

                        JsonNode execNode = event.get("script").get("exec");

                        // Vérifie si "exec" est un tableau non vide
                        if (execNode.isArray() && execNode.size() > 0) {
                            return true;
                        }

                        // OU s'il s'agit d'une chaîne non vide
                        if (execNode.isTextual() && !execNode.asText().trim().isEmpty()) {
                            return true;
                        }
                    }
                }
            }
            if (item.has("item")) {
                if (containsTestScript(item.get("item"))) {
                    return true;
                }
            }
        }
        return false;
    }




    // import d'une collection postman pour ajouter les scripts de tests
    /*@Operation(summary = "Importer une Collection Postman afin de leur ajouter des scripts des tests", description = "Permet d'importer une collection Postman afin de leur ajouter des scripts des tests et de la stocker dans le répertoire resources sous le nom collection_postman.json ")
    @PostMapping(value = "/process-file", consumes ="multipart/form-data")
    public String processPostmanCollection(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return "Le fichier est vide.";
        }
        try {
            String postmanCollectionJson = new String(file.getBytes());
            return postmanProcessingService.processPostmanCollection(postmanCollectionJson);
        } catch (IOException e) {
            return "Erreur lors du traitement du fichier : " + e.getMessage();
        }
    }*/

    /*ne vérifie pas une collection postman*/
    /*@Operation(summary = "Importer une Collection Postman afin de leur ajouter des scripts des tests",
            description = "Permet d'importer une collection Postman afin de leur ajouter des scripts des tests et de la stocker dans le répertoire resources sous le nom collection_postman.json ")
    @PostMapping(value = "/process-file", consumes ="multipart/form-data")
    public String processPostmanCollection(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return "Le fichier est vide.";
        }

        try {
            String postmanCollectionJson = new String(file.getBytes());

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode collectionNode = objectMapper.readTree(postmanCollectionJson);

            boolean containsTests = containsTestScript(collectionNode.path("item"));

            if (containsTests) {
                return "La collection contient déjà des scripts de tests.";
            } else {
                return postmanProcessingService.processPostmanCollection(postmanCollectionJson);
            }

        } catch (IOException e) {
            return "Erreur lors du traitement du fichier : " + e.getMessage();
        }
    }*/

    @Operation(
            summary = "Importer une Collection Postman afin de leur ajouter des scripts de tests",
            description = "Permet d'importer une collection Postman afin de leur ajouter des scripts de tests et de la stocker dans le répertoire resources sous le nom collection_postman.json"
    )
    @PostMapping(value = "/process-file", consumes = "multipart/form-data")
    public String processPostmanCollection(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return "Le fichier est vide.";
        }

        try {
            String postmanCollectionJson = new String(file.getBytes());
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode collectionNode = objectMapper.readTree(postmanCollectionJson);

            // Vérification que le fichier est bien une collection Postman
            JsonNode infoNode = collectionNode.path("info");
            String schemaUrl = infoNode.path("schema").asText();
            if (!schemaUrl.contains("postman.com/json/collection")) {
                return "Le fichier fourni n'est pas une collection Postman valide.";
            }

            // Vérifie la présence de scripts de test
            boolean containsTests = containsTestScript(collectionNode.path("item"));
            if (containsTests) {
                return "La collection contient déjà des scripts de tests.";
            }

            // Traitement du fichier via le service
            return postmanProcessingService.processPostmanCollection(postmanCollectionJson);

        } catch (IOException e) {
            return "Erreur lors du traitement du fichier : " + e.getMessage();
        }
    }



    // téléchargement du fichier disponible dans la partie ressource en local du pc
    @Operation(summary = "Téléchargement de la Collection Postman en local", description = "Permet le téléchargement de la collection postman disponible dans la partie ressource du systéme  et stocker dans la partie téléchargement en local.")
    @GetMapping("/download-file")
    public ResponseEntity<byte[]> downloadFile() {
        try {
            Resource resource = new ClassPathResource(POSTMAN_COLLECTION_FILENAME);

            byte[] fileContent = Files.readAllBytes(resource.getFile().toPath());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setContentDispositionFormData("attachment", POSTMAN_COLLECTION_FILENAME);

            return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Erreur lors du téléchargement du fichier : " + e.getMessage()).getBytes());
        }
    }




}