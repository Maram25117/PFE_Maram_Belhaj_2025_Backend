/*package com.example.api_tierces.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SwaggerProcessingService {

    @Autowired
    private UploadService apiService;

    private final RestTemplate restTemplate = new RestTemplate();

    public void processSwaggerDaily() {
        // URL de l'API Swagger
        String swaggerUrl = "http://localhost:8084/v3/api-docs";
        try {
            // Téléchargement du fichier Swagger
            String swaggerJson = restTemplate.getForObject(swaggerUrl, String.class);
            System.out.println("Swagger JSON: " + swaggerJson);  // Afficher le contenu du Swagger pour déboguer

            // Traitement du fichier Swagger avec apiService
            apiService.parseSwaggerFileFromUrl(swaggerJson);
        } catch (Exception e) {
            System.err.println("Erreur lors du traitement du fichier Swagger : " + e.getMessage());
        }
    }
}*/
package com.example.api_tierces.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@Service
public class SwaggerProcessingService {

    @Autowired
    private UploadService apiService;

    private final RestTemplate restTemplate = new RestTemplate();

    public void processSwaggerDaily() {
        // URL de l'API Swagger
        String swaggerUrl = "http://localhost:8084/v3/api-docs";
        try {
            // Téléchargement du fichier Swagger
            String swaggerJson = restTemplate.getForObject(swaggerUrl, String.class);
            System.out.println("Swagger JSON téléchargé.");

            // Sauvegarde du fichier Swagger dans le dossier resources
            saveSwaggerFile(swaggerJson);

            // Traitement du fichier Swagger avec apiService
            apiService.parseSwaggerFileFromUrl(swaggerJson);
        } catch (Exception e) {
            System.err.println("Erreur lors du traitement du fichier Swagger : " + e.getMessage());
        }
    }

    // Méthode pour sauvegarder le fichier Swagger dans un dossier de ressources
    private void saveSwaggerFile(String swaggerJson) throws IOException {
        // Définir le chemin du fichier dans les ressources
        String filePath = "src/main/resources/swagger-doc.json";

        // Créer un fichier et écrire le contenu JSON dedans
        File file = new File(filePath);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(swaggerJson);
            System.out.println("Fichier Swagger sauvegardé sous : " + filePath);
        } catch (IOException e) {
            throw new IOException("Erreur lors de la sauvegarde du fichier Swagger : " + e.getMessage());
        }
    }
}

