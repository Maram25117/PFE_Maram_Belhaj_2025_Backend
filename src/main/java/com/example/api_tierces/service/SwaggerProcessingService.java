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


@Service
public class SwaggerProcessingService {

    @Autowired
    private UploadService apiService;

    private final RestTemplate restTemplate = new RestTemplate();


    @Autowired
    private SwaggerUrlRepository swaggerUrlRepository;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // ceci extracte les données de l'url et les stockes dans la base de données
    public void processSwaggerDaily() {
        List<Swagger> urls = swaggerUrlRepository.findAll();

        for (Swagger swaggerUrl : urls) {
            try {
                String swaggerJson = restTemplate.getForObject(swaggerUrl.getUrl(), String.class);
                System.out.println("Swagger JSON téléchargé depuis : " + swaggerUrl.getUrl());

                /*saveSwaggerFile(swaggerJson);*/

                apiService.parseSwaggerFile(swaggerJson);

            } catch (Exception e) {
                System.err.println("Erreur avec l'URL " + swaggerUrl.getUrl() + " : " + e.getMessage());
            }
        }
    }

    // enregistrement du swagger file dans la partie ressource du systéme
    /*private void saveSwaggerFile(String swaggerJson) throws IOException {
        String filePath = "src/main/resources/swagger-doc.json";

        File file = new File(filePath);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(swaggerJson);
            System.out.println("Fichier Swagger sauvegardé sous : " + filePath);
        } catch (IOException e) {
            throw new IOException("Erreur lors de la sauvegarde du fichier Swagger : " + e.getMessage());
        }
    }*/

    //enregistrement des données dans la base de données a partir de l'url definit
    public void extractSwaggerData(String url) throws Exception {
        try {

            String swaggerJson = restTemplate.getForObject(url, String.class);
            System.out.println("Swagger JSON téléchargé depuis : " + url);

            /*saveSwaggerFile(swaggerJson);*/

            apiService.parseSwaggerFile(swaggerJson);
        } catch (Exception e) {
            throw new Exception("Erreur lors de l'extraction des données Swagger : " + e.getMessage());
        }
    }


    // enregistrement du swagger dans la partie downloads du pc
    public void downloadSwaggerFileToDownloads(String fileUrl, String fileName) throws IOException {
        String userHome = System.getProperty("user.home");
        Path downloadsDir;
        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.contains("win")) {
            downloadsDir = Paths.get(userHome, "Downloads");
        } else if (osName.contains("mac")) { // Ajout pour macOS
            downloadsDir = Paths.get(userHome, "Downloads");
        } else {
            downloadsDir = Paths.get(userHome, "Téléchargements");
            if (!Files.exists(downloadsDir)) {
                downloadsDir = Paths.get(userHome, "Downloads");
            }
        }


        if (!Files.exists(downloadsDir)) {
            Files.createDirectories(downloadsDir);
        }
        Path filePath = downloadsDir.resolve(fileName);

        URL url = new URL(fileUrl);
        String originalContent;
        try (InputStream in = url.openStream()) {
            originalContent = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Erreur lors du téléchargement depuis " + fileUrl + ": " + e.getMessage());
            throw e;
        }


        try {

            JsonNode rootNode = objectMapper.readTree(originalContent);

            String prettyJsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);

            Files.write(filePath, prettyJsonString.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);

            System.out.println("Fichier Swagger formaté enregistré dans : " + filePath.toAbsolutePath());

        } catch (JsonProcessingException e) {
            System.err.println("AVERTISSEMENT: Le contenu téléchargé depuis " + fileUrl
                    + " n'est pas un JSON valide. Le fichier original (non formaté) sera sauvegardé.");
            System.err.println("Erreur de parsing JSON: " + e.getMessage());
            Files.write(filePath, originalContent.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);

            System.out.println("Fichier original (non formaté) enregistré dans : " + filePath.toAbsolutePath());


        } catch (IOException e) {

            System.err.println("Erreur lors de l'écriture du fichier " + filePath + ": " + e.getMessage());
            throw e;
        }
    }


}