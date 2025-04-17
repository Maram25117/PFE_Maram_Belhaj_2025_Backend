package com.example.api_tierces.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.example.api_tierces.model.Swagger;
import com.example.api_tierces.repository.SwaggerUrlRepository;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException; // (Vous l'avez probablement déjà)
// ... autres imports ...
import com.fasterxml.jackson.databind.JsonNode;         // <-- Import nécessaire
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

@Service
public class SwaggerProcessingService {

    @Autowired
    private UploadService apiService;

    private final RestTemplate restTemplate = new RestTemplate();

    /*public void processSwaggerDaily() {
        String swaggerUrl = "http://localhost:8084/v3/api-docs";
        try {

            String swaggerJson = restTemplate.getForObject(swaggerUrl, String.class);
            System.out.println("Swagger JSON téléchargé.");

            saveSwaggerFile(swaggerJson);

            apiService.parseSwaggerFile(swaggerJson);
        } catch (Exception e) {
            System.err.println("Erreur lors du traitement du fichier Swagger : " + e.getMessage());
        }
    }*/

    @Autowired
    private SwaggerUrlRepository swaggerUrlRepository;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public void processSwaggerDaily() {
        List<Swagger> urls = swaggerUrlRepository.findAll();

        for (Swagger swaggerUrl : urls) {
            try {
                String swaggerJson = restTemplate.getForObject(swaggerUrl.getUrl(), String.class);
                System.out.println("Swagger JSON téléchargé depuis : " + swaggerUrl.getUrl());

                saveSwaggerFile(swaggerJson);

                apiService.parseSwaggerFile(swaggerJson);

            } catch (Exception e) {
                System.err.println("Erreur avec l'URL " + swaggerUrl.getUrl() + " : " + e.getMessage());
            }
        }
    }

    // Méthode pour sauvegarder le fichier Swagger dans un dossier de ressources
    private void saveSwaggerFile(String swaggerJson) throws IOException {
        // Définir le chemin du fichier dans les ressources
        String filePath = "src/main/resources/swagger-doc.json";

        // Créer un fichier et écrire le contenu JSON en lui
        File file = new File(filePath);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(swaggerJson);
            System.out.println("Fichier Swagger sauvegardé sous : " + filePath);
        } catch (IOException e) {
            throw new IOException("Erreur lors de la sauvegarde du fichier Swagger : " + e.getMessage());
        }
    }

    public void extractSwaggerData(String url) throws Exception {
        try {
            // Télécharge le Swagger JSON depuis l'URL
            String swaggerJson = restTemplate.getForObject(url, String.class);
            System.out.println("Swagger JSON téléchargé depuis : " + url);

            saveSwaggerFile(swaggerJson);

            apiService.parseSwaggerFile(swaggerJson);
        } catch (Exception e) {
            throw new Exception("Erreur lors de l'extraction des données Swagger : " + e.getMessage());
        }
    }

    /*public void downloadSwaggerFileToDownloads(String fileUrl, String fileName) throws IOException {
        // Détection du dossier "Téléchargements" selon l'OS
        String userHome = System.getProperty("user.home");
        Path downloadsDir;

        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            downloadsDir = Paths.get(userHome, "Downloads");
        } else {
            downloadsDir = Paths.get(userHome, "Téléchargements"); // Pour Linux/macOS (français)
            if (!Files.exists(downloadsDir)) {
                downloadsDir = Paths.get(userHome, "Downloads"); // fallback en anglais
            }
        }

        // Créer le dossier s'il n'existe pas
        if (!Files.exists(downloadsDir)) {
            Files.createDirectories(downloadsDir);
        }

        // Télécharger le fichier depuis l'URL
        URL url = new URL(fileUrl);
        try (InputStream in = url.openStream()) {
            Path filePath = downloadsDir.resolve(fileName);
            Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Fichier enregistré dans : " + filePath.toAbsolutePath());
        }
    }*/
    /*public class SwaggerDownloaderUtil { // Vous pouvez mettre cette méthode dans une classe utilitaire

        // Créer une instance d'ObjectMapper (peut être statique ou créée à chaque appel)
        private static final ObjectMapper objectMapper = new ObjectMapper();

        /**
         * Télécharge un fichier depuis une URL, le formate en JSON (pretty-print)
         * et l'enregistre dans le dossier Téléchargements de l'utilisateur.
         * Si le contenu n'est pas du JSON valide, enregistre le fichier original.
         *
         * @param fileUrl  L'URL du fichier à télécharger.
         * @param fileName Le nom à donner au fichier sauvegardé.
         * @throws IOException En cas d'erreur de réseau, de fichier ou de parsing JSON.
         */
        /*public void downloadAndPrettyPrintSwaggerFile(String fileUrl, String fileName) throws IOException {
            // --- Détection du dossier Téléchargements (logique existante améliorée) ---
            String userHome = System.getProperty("user.home");
            Path downloadsDir;
            String osName = System.getProperty("os.name").toLowerCase();

            if (osName.contains("win")) {
                downloadsDir = Paths.get(userHome, "Downloads");
            } else if (osName.contains("mac")) { // Ajout pour macOS
                downloadsDir = Paths.get(userHome, "Downloads"); // Le défaut est "Downloads" sur Mac
            } else { // Assume Linux/other Unix-like
                downloadsDir = Paths.get(userHome, "Téléchargements"); // Défaut français
                if (!Files.exists(downloadsDir)) {
                    downloadsDir = Paths.get(userHome, "Downloads"); // Fallback anglais
                }
            }

            // Créer le dossier s'il n'existe pas
            if (!Files.exists(downloadsDir)) {
                Files.createDirectories(downloadsDir);
            }
            Path filePath = downloadsDir.resolve(fileName); // Chemin complet du fichier final

            // --- Télécharger et lire le contenu en mémoire ---
            URL url = new URL(fileUrl);
            String originalContent;
            try (InputStream in = url.openStream()) {
                // Lire tous les octets du flux et les convertir en String UTF-8
                originalContent = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                System.err.println("Erreur lors du téléchargement depuis " + fileUrl + ": " + e.getMessage());
                throw e; // Propager l'erreur
            }

            // --- Parser, Formater et Écrire le JSON ---
            try {
                // Parser la chaîne originale en structure JsonNode (ou Object/Map)
                JsonNode rootNode = objectMapper.readTree(originalContent);

                // Ré-écrire le JsonNode en chaîne formatée (pretty-printed)
                String prettyJsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);

                // Écrire la chaîne formatée dans le fichier
                Files.write(filePath, prettyJsonString.getBytes(StandardCharsets.UTF_8),
                        StandardOpenOption.CREATE,           // Crée le fichier s'il n'existe pas
                        StandardOpenOption.TRUNCATE_EXISTING, // Écrase le contenu s'il existe
                        StandardOpenOption.WRITE);           // Ouvre pour écriture

                System.out.println("Fichier Swagger formaté enregistré dans : " + filePath.toAbsolutePath());

            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                // Si le contenu n'est PAS du JSON valide, on log une erreur et on sauvegarde l'original
                System.err.println("AVERTISSEMENT: Le contenu téléchargé depuis " + fileUrl
                        + " n'est pas un JSON valide. Le fichier original (non formaté) sera sauvegardé.");
                System.err.println("Erreur de parsing JSON: " + e.getMessage());

                // Sauvegarde du contenu original non formaté
                Files.write(filePath, originalContent.getBytes(StandardCharsets.UTF_8),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.WRITE);

                System.out.println("Fichier original (non formaté) enregistré dans : " + filePath.toAbsolutePath());
                // Optionnel : propager une exception si le formatage est crucial
                // throw new IOException("Le contenu téléchargé n'était pas du JSON valide.", e);

            } catch (IOException e) {
                // Gérer les erreurs lors de l'écriture du fichier final
                System.err.println("Erreur lors de l'écriture du fichier formaté " + filePath + ": " + e.getMessage());
                throw e;
            }
        }


    }*/

    public void downloadSwaggerFileToDownloads(String fileUrl, String fileName) throws IOException {
        // --- Détection du dossier "Téléchargements" selon l'OS (version améliorée) ---
        String userHome = System.getProperty("user.home");
        Path downloadsDir;
        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.contains("win")) {
            downloadsDir = Paths.get(userHome, "Downloads");
        } else if (osName.contains("mac")) { // Ajout pour macOS
            downloadsDir = Paths.get(userHome, "Downloads"); // Le défaut est "Downloads" sur Mac
        } else { // Assume Linux/other Unix-like
            downloadsDir = Paths.get(userHome, "Téléchargements"); // Défaut français
            if (!Files.exists(downloadsDir)) {
                downloadsDir = Paths.get(userHome, "Downloads"); // Fallback anglais
            }
        }

        // Créer le dossier s'il n'existe pas
        if (!Files.exists(downloadsDir)) {
            Files.createDirectories(downloadsDir);
        }
        Path filePath = downloadsDir.resolve(fileName); // Chemin complet du fichier final

        // --- Télécharger et lire le contenu en mémoire ---
        URL url = new URL(fileUrl);
        String originalContent;
        try (InputStream in = url.openStream()) {
            // Lire tous les octets du flux et les convertir en String UTF-8
            originalContent = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Erreur lors du téléchargement depuis " + fileUrl + ": " + e.getMessage());
            throw e; // Propager l'erreur
        }

        // --- Tenter de Parser, Formater et Écrire le JSON ---
        try {
            // Parser la chaîne originale en structure JsonNode
            JsonNode rootNode = objectMapper.readTree(originalContent);

            // Ré-écrire le JsonNode en chaîne formatée (pretty-printed)
            String prettyJsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);

            // Écrire la chaîne formatée dans le fichier (remplace Files.copy)
            Files.write(filePath, prettyJsonString.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE,           // Crée le fichier s'il n'existe pas
                    StandardOpenOption.TRUNCATE_EXISTING, // Écrase le contenu s'il existe
                    StandardOpenOption.WRITE);           // Ouvre pour écriture

            System.out.println("Fichier Swagger formaté enregistré dans : " + filePath.toAbsolutePath());

        } catch (JsonProcessingException e) {
            // --- Si ce n'est PAS du JSON valide ---
            // Logguer une erreur/avertissement et sauvegarder le contenu original
            System.err.println("AVERTISSEMENT: Le contenu téléchargé depuis " + fileUrl
                    + " n'est pas un JSON valide. Le fichier original (non formaté) sera sauvegardé.");
            System.err.println("Erreur de parsing JSON: " + e.getMessage());

            // Sauvegarde du contenu original non formaté (comme l'aurait fait Files.copy)
            Files.write(filePath, originalContent.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);

            System.out.println("Fichier original (non formaté) enregistré dans : " + filePath.toAbsolutePath());
            // Optionnel : Vous pourriez vouloir lancer une exception ici si le formatage est obligatoire
            // throw new IOException("Le contenu téléchargé n'était pas du JSON valide.", e);

        } catch (IOException e) {
            // Gérer les erreurs lors de l'écriture du fichier final (formaté ou original)
            System.err.println("Erreur lors de l'écriture du fichier " + filePath + ": " + e.getMessage());
            throw e;
        }
    }


}