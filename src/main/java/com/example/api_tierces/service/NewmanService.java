/*package com.example.api_tierces.service;

import com.example.api_tierces.model.ApiMonitoring;
import com.example.api_tierces.repository.ApiMonitoringRepository;
import com.fasterxml.jackson.databind.ObjectMapper; //Gère la conversion JSON en Java.
import org.springframework.scheduling.annotation.Scheduled; //Annotation pour exécuter automatiquement les tests à intervalles réguliers.
import org.springframework.stereotype.Service; //Indique que cette classe est un service Spring.

import java.io.File;
import java.util.regex.Matcher; //Utilisé pour extraire des informations d'une chaîne de texte avec des expressions régulières.
import java.util.regex.Pattern; //Utilisé pour extraire des informations d'une chaîne de texte avec des expressions régulières.
import java.io.BufferedReader; //Permette de lire la sortie d’un processus externe (Newman).
import java.io.InputStreamReader; //Permette de lire la sortie d’un processus externe (Newman).
import java.time.LocalDateTime; //Stocke l’heure d’exécution des tests.


import java.net.URI; //Pour extraire le chemin d’une URL.
import com.fasterxml.jackson.databind.node.ObjectNode; //Gère la conversion JSON en Java

import java.util.Optional; //Pour gérer des valeurs nulles sans NullPointerException.


@Service
public class NewmanService {

    private final ApiMonitoringRepository resultRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NewmanService(ApiMonitoringRepository resultRepository) {
        this.resultRepository = resultRepository;
    }
    //Exécution automatique avec @Scheduled
    @Scheduled(cron = "0 * * * * *") // Exécution toutes les minutes
    public void runNewmanTests() {
       /* try {
            ProcessBuilder processBuilder = new ProcessBuilder( //Utilisation de ProcessBuilder pour exécuter Newman via la ligne de commande.
                    "C:\\Users\\LENOVO\\AppData\\Roaming\\npm\\newman.cmd", // Chemin vers Newman.
                    "run", "" //Exécute la collection de tests Postman Reqres_API.
            );*/
        /*try {
            ProcessBuilder processBuilder = new ProcessBuilder( //Utilisation de ProcessBuilder pour exécuter Newman via la ligne de commande.
                    "C:\\Users\\LENOVO\\AppData\\Roaming\\npm\\newman.cmd", // Chemin vers Newman.
                    "run", "" //Exécute la collection de tests Postman Reqres_API.
            );*/
          /*try {
               ProcessBuilder processBuilder = new ProcessBuilder( //Utilisation de ProcessBuilder pour exécuter Newman via la ligne de commande.
                       "C:\\Users\\LENOVO\\AppData\\Roaming\\npm\\newman.cmd", // Chemin vers Newman.
                       "run", "postman_collection.json" //Exécute la collection de tests Postman Reqres_API.
               );*/
        /*try {
            // Récupérer le chemin absolu du fichier postman_collection.json dans le dossier resources
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("postman_collection.json").toURI());

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "C:\\Users\\LENOVO\\AppData\\Roaming\\npm\\newman.cmd",
                    "run", file.getAbsolutePath() // Utilisation du chemin absolu du fichier
            );

            processBuilder.redirectErrorStream(true); //les erreurs seront redirigées vers la sortie standard.
            Process process = processBuilder.start(); //Renvoie un objet Process qui permet d'interagir avec le processus

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream())); //BufferedReader lit la sortie générée par Newman ligne par ligne.
            // process.getInputStream() : pour accéder a la sortie
            String line;
            String currentPath = null;
            String httpMethod = null; // Nouvelle variable pour stocker la méthode HTTP
            boolean success = false;
            int statusCode = 0;
            long responseTime = 0;
            String errorMessage = null;
            boolean requestStarted = false;

            //Traitement des lignes de sortie
            while ((line = reader.readLine()) != null) {
                System.out.println("Ligne lue : " + line);

                if (line.startsWith("→ ")) { //Si la ligne commence par → , cela signifie le début d’une requête API.
                    if (currentPath != null && requestStarted) {
                        saveTestResult(currentPath, httpMethod, success, statusCode, responseTime, errorMessage); //On sauvegarde les résultats du test précédent avant d’analyser la nouvelle requête.
                    }

                    String requestName = line.substring(2).trim(); // .trim() : Cette méthode supprime les espaces blancs
                    // line.substring(2) : Cette méthode extrait une sous-chaîne de line, en commençant à l’indice 2
                    System.out.println("Début de la requête : " + requestName);
                    //Réinitialisation des variables avant le test suivant.
                    currentPath = null;
                    httpMethod = null; // Réinitialiser la méthode HTTP
                    success = false;
                    statusCode = 0;
                    responseTime = 0;
                    errorMessage = null;
                    requestStarted = true;


                    String requestLine = reader.readLine();
                    System.out.println("Ligne de requête : " + requestLine);


                    if (requestLine != null && requestLine.contains(" ")) { //vérification que la ligne n'est pas null et contient un espace
                        // Extraction de l'URL et des informations de statut
                        Pattern pattern = Pattern.compile("^\\s*(GET|POST|PUT|DELETE)\\s+(https?://[^\\s]+)\\s+\\[(\\d{3})\\s+[^,]+,\\s+([^,]+),\\s+([0-9]+)ms]");
                        // ^\\s* → Ignore les espaces en début de ligne.
                        // (GET|POST|PUT|DELETE) → Capture la méthode HTTP.
                        // \\s+ → Un ou plusieurs espaces.
                        // (https?://[^\\s]+) → Capture l’URL complète.
                        // \\s+ → Un ou plusieurs espaces.
                        //  \\[(\\d{3}) → Capture le code HTTP (ex: 200, 404, 500).
                        //  [^,]+,\\s+([^,]+), → Ignore des caractères jusqu'à une autre information (probablement un type de réponse).
                        //  ([0-9]+)ms] → Capture le temps de réponse en millisecondes.
                        Matcher matcher = pattern.matcher(requestLine); // matcher permet d’appliquer la regex sur requestLine pour trouver les correspondances.
                        // chercher les correspondances
                        if (matcher.find()) {
                            httpMethod = matcher.group(1); // Extraction de la méthode HTTP
                            String fullUrl = matcher.group(2); // URL complète
                            String status = matcher.group(3);   // Code de statut HTTP
                            String time = matcher.group(5);     // Temps de réponse en ms

                            try {
                                URI uri = new URI(fullUrl); // Tente de convertir l’URL en un objet URI.
                                //Un URI représente une ressource (page web, fichier...).
                                //On peut le manipuler pour extraire ses parties (host, path, query...).
                                currentPath = uri.getPath(); // Extraire uniquement le chemin (sans url de base)
                                System.out.println("Chemin extrait : " + currentPath);
                            } catch (Exception e) {
                                System.err.println("Erreur lors de l'extraction du path depuis l'URL : " + fullUrl);
                                currentPath = "Unknown Path";
                            }
                            try {
                                statusCode = Integer.parseInt(status); //convertit une chaine de caractére en int
                                success = (statusCode >= 200 && statusCode < 300);
                            } catch (NumberFormatException e) {
                                System.err.println("Impossible d'analyser le code de statut : " + status);
                                statusCode = 0;
                                success = false;
                            }
                            try {
                                responseTime = Long.parseLong(time);
                            } catch (NumberFormatException e) {
                                System.err.println("Impossible d'analyser le temps de réponse : " + time);
                                responseTime = 0;
                            }


                        } else {
                            System.out.println("Aucune correspondance trouvée pour l'URL : " + requestLine);
                            currentPath = "Unknown Path";
                        }
                    }
                }


                if (line.toLowerCase().contains("error")) {
                    errorMessage = line; //Si une ligne contient "error", elle est stockée dans errorMessage.
                }
            }

            // Enregistrement du dernier résultat si la boucle se termine
            if (currentPath != null && requestStarted) {
                saveTestResult(currentPath, httpMethod, success, statusCode, responseTime, errorMessage); // Envoyer la méthode HTTP
            }

            int exitCode = process.waitFor();
            System.out.println(exitCode == 0 ? "Tous les tests ont été exécutés avec succès !" : "Certains tests ont échoué. Code de sortie : " + exitCode);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Une erreur s'est produite lors de l'exécution des tests Newman.");
        }
    }

    private void saveTestResult(String path, String method, boolean success, int statusCode, long responseTime, String errorMessage) {
        System.out.println("Tentative d'enregistrement pour : " + path + " (" + method + ")");
        try {
            String level = success ? "INFO" : "ERROR";

            // Créez un objet JSON pour stocker le path et la méthode
            ObjectNode metadata = objectMapper.createObjectNode();
            metadata.put("path", path);
            metadata.put("method", method);

            // Recherchez une entrée existante avec le même path et la même méthode
            //Recherche d'un test existant dans la base de données par path et method.
            Optional<ApiMonitoring> existingResult = resultRepository.findByMetadata(metadata.toString()); //Recherche par metadata

            ApiMonitoring result; //objet ApiMonitoring

            if (existingResult.isPresent()) {
                // Mettre à jour l'entrée existante
                result = existingResult.get();
                result.setTemps(LocalDateTime.now());
                result.setResponseTime(responseTime);
                result.setStatusCode(statusCode);
                result.setErrorMessage(errorMessage);
                result.setLevel(level);
                System.out.println("Mise à jour de l'enregistrement existant pour : " + path + " (" + method + ")");
            } else {
                // Créer une nouvelle entrée
                result = new ApiMonitoring();
                result.setPath(path);
                result.setTemps(LocalDateTime.now());
                result.setResponseTime(responseTime);
                result.setStatusCode(statusCode);
                result.setErrorMessage(errorMessage);
                result.setLevel(level);
                result.setMetadata(metadata.toString()); // Stocker le JSON dans la colonne metadata
                System.out.println("Création d'un nouvel enregistrement pour : " + path + " (" + method + ")");
            }

            resultRepository.save(result);
            System.out.println("Enregistrement réussi pour : " + path + " (" + method + ")");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur lors de l'enregistrement pour : " + path + " (" + method + ")");
        }
    }
}*/
package com.example.api_tierces.service;

import com.example.api_tierces.model.ApiMonitoring;
import com.example.api_tierces.repository.ApiMonitoringRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.net.URI;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Optional;


/*@Service
public class NewmanService {

    private final ApiMonitoringRepository resultRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NewmanService(ApiMonitoringRepository resultRepository) {
        this.resultRepository = resultRepository;
        System.out.println("NewmanService créé avec le repository : " + resultRepository);
    }

    @Scheduled(cron = "0 * * * * *") // Exécution toutes les minutes
    public void runNewmanTests() {
        System.out.println("Début de l'exécution des tests Newman.");

        try {
            // Récupérer le chemin absolu du fichier postman_collection.json dans le dossier resources
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("postman_collection.json").toURI());

            System.out.println("Chemin du fichier de collection Postman : " + file.getAbsolutePath());

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "C:\\Users\\LENOVO\\AppData\\Roaming\\npm\\newman.cmd",
                    "run", file.getAbsolutePath() // Utilisation du chemin absolu du fichier
            );

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            String currentPath = null;
            String httpMethod = null;
            boolean success = false;
            int statusCode = 0;
            long responseTime = 0;
            String errorMessage = null;
            boolean requestStarted = false;

            while ((line = reader.readLine()) != null) {
                System.out.println("Ligne lue : " + line);

                // Check if a new request is starting based on the "□" character
                if (line.startsWith("□ ")) {
                    // If there was a previous request, save its results
                    if (currentPath != null && requestStarted) {
                        saveTestResult(currentPath, httpMethod, success, statusCode, responseTime, errorMessage);
                    } else {
                        System.out.println("saveTestResult non appelé (début de requête). currentPath est null: " + (currentPath == null) + ", requestStarted est false: " + (!requestStarted));
                    }

                    // Reset variables for the new request
                    currentPath = null;
                    httpMethod = null;
                    success = false;
                    statusCode = 0;
                    responseTime = 0;
                    errorMessage = null;
                    requestStarted = true;

                    // Extract the request name
                    String requestName = line.substring(2).trim();
                    System.out.println("Début de la requête : " + requestName);

                    // Read the lines containing the request details (URL, status, etc.)
                    String requestLine1 = reader.readLine(); // Line containing "└"
                    System.out.println("Ligne de requête 1 : " + requestLine1);

                    String requestLine2 = reader.readLine(); // Line containing request details
                    System.out.println("Ligne de requête 2 : " + requestLine2);

                    // Extract request details using regex
                    if (requestLine2 != null && requestLine2.contains(" ")) {
                        Pattern pattern = Pattern.compile("^\\s*(GET|POST|PUT|DELETE)\\s+(https?://[^\\s]+)\\s+\\[(\\d{3})\\s+[^,]+,\\s+([^,]+),\\s+([0-9]+)ms]");
                        Matcher matcher = pattern.matcher(requestLine2);

                        if (matcher.find()) {
                            httpMethod = matcher.group(1);
                            String fullUrl = matcher.group(2);
                            String status = matcher.group(3);
                            String time = matcher.group(5);


                            try {
                                URI uri = new URI(fullUrl);
                                currentPath = uri.getPath();
                                System.out.println("Chemin extrait : " + currentPath);
                            } catch (Exception e) {
                                System.err.println("Erreur lors de l'extraction du path depuis l'URL : " + fullUrl);
                                currentPath = "Unknown Path";
                            }
                            try {
                                statusCode = Integer.parseInt(status);
                                success = (statusCode >= 200 && statusCode < 300);
                            } catch (NumberFormatException e) {
                                System.err.println("Impossible d'analyser le code de statut : " + status);
                                statusCode = 0;
                                success = false;
                            }
                            try {
                                responseTime = Long.parseLong(time);
                            } catch (NumberFormatException e) {
                                System.err.println("Impossible d'analyser le temps de réponse : " + time);
                                responseTime = 0;
                            }
                        } else {
                            System.out.println("Aucune correspondance trouvée pour l'URL : " + requestLine2);
                            currentPath = "Unknown Path";
                        }
                    }
                }

                if (line.toLowerCase().contains("error")) {
                    errorMessage = line;
                }
            }

            // Save the results of the last request if any
            if (currentPath != null && requestStarted) {
                saveTestResult(currentPath, httpMethod, success, statusCode, responseTime, errorMessage);
            } else {
                System.out.println("saveTestResult non appelé (fin de la boucle). currentPath est null: " + (currentPath == null) + ", requestStarted est false: " + (!requestStarted));
            }

            int exitCode = process.waitFor();
            System.out.println(exitCode == 0 ? "Tous les tests ont été exécutés avec succès !" : "Certains tests ont échoué. Code de sortie : " + exitCode);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Une erreur s'est produite lors de l'exécution des tests Newman: " + e.getMessage());
        }
        System.out.println("Fin de l'exécution des tests Newman.");
    }

    @Transactional
    protected void saveTestResult(String path, String method, boolean success, int statusCode, long responseTime, String errorMessage) {
        try {
            String level = (statusCode == 500) ? "ERROR" : "INFO";

            // Créez un objet JSON pour stocker le path et la méthode
            ObjectNode metadata = objectMapper.createObjectNode();
            metadata.put("path", path);
            metadata.put("method", method);

            // Recherchez une entrée existante avec le même path et la même méthode
            // Recherche d'un test existant dans la base de données par path et method.
            String metadataString = metadata.toString();

            Optional<ApiMonitoring> existingResult = resultRepository.findByMetadata(metadataString);

            ApiMonitoring result;
            if (existingResult.isPresent()) {

                // Mettre à jour l'entrée existante
                result = existingResult.get();
                result.setTemps(LocalDateTime.now());
                result.setResponseTime(responseTime);
                result.setStatusCode(statusCode);
                result.setErrorMessage(errorMessage);
                result.setLevel(level);

            } else {

                // Créer une nouvelle entrée
                result = new ApiMonitoring();
                result.setPath(path);
                result.setTemps(LocalDateTime.now());
                result.setResponseTime(responseTime);
                result.setStatusCode(statusCode);
                result.setErrorMessage(errorMessage);
                result.setLevel(level);
                result.setMetadata(metadataString); // Stocker le JSON dans la colonne metadata

            }

            resultRepository.save(result);
            System.out.println("Enregistrement réussi pour : " + path + " (" + method + ")");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur lors de l'enregistrement pour : " + path + " (" + method + "): " + e.getMessage());
        }
    }
}*/
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*@Service
public class NewmanService {

    private final ApiMonitoringRepository resultRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NewmanService(ApiMonitoringRepository resultRepository) {
        this.resultRepository = resultRepository;
        System.out.println("NewmanService créé avec le repository : " + resultRepository);
    }

    @Scheduled(cron = "0 * * * * *") // Exécution toutes les minutes
    public void runNewmanTests() {
        System.out.println("Début de l'exécution des tests Newman.");

        try {
            // Récupérer le chemin absolu du fichier postman_collection.json dans le dossier resources
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("postman_collection.json").toURI());

            System.out.println("Chemin du fichier de collection Postman : " + file.getAbsolutePath());

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "C:\\Users\\LENOVO\\AppData\\Roaming\\npm\\newman.cmd",
                    "run", file.getAbsolutePath() // Utilisation du chemin absolu du fichier
            );

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            String currentPath = null;
            String httpMethod = null;
            boolean success = false;
            int statusCode = 0;
            long responseTime = 0;
            String errorMessage = null;
            boolean requestStarted = false;

            while ((line = reader.readLine()) != null) {
                System.out.println("Ligne lue : " + line);

                // Check if a new request is starting based on the "□" character
                if (line.startsWith("□ ")) {
                    // If there was a previous request, save its results
                    if (currentPath != null && requestStarted && !"Unknown Path".equals(currentPath)) {
                        saveTestResult(currentPath, httpMethod, success, statusCode, responseTime, errorMessage);
                    } else {
                        System.out.println("saveTestResult non appelé (début de requête). currentPath est null: " + (currentPath == null) + ", requestStarted est false: " + (!requestStarted) + " or currentPath is Unknown Path");
                    }

                    // Reset variables for the new request
                    currentPath = null;
                    httpMethod = null;
                    success = false;
                    statusCode = 0;
                    responseTime = 0;
                    errorMessage = null;
                    requestStarted = true;

                    // Extract the request name
                    String requestName = line.substring(2).trim();
                    System.out.println("Début de la requête : " + requestName);

                    // Read the lines containing the request details (URL, status, etc.)
                    String requestLine1 = reader.readLine(); // Line containing "└"
                    System.out.println("Ligne de requête 1 : " + requestLine1);

                    String requestLine2 = reader.readLine(); // Line containing request details
                    System.out.println("Ligne de requête 2 : " + requestLine2);

                    // Extract request details using regex
                    if (requestLine2 != null && requestLine2.contains(" ")) {
                        Pattern pattern = Pattern.compile("^\\s*(GET|POST|PUT|DELETE)\\s+(https?://[^\\s]+)\\s+\\[(\\d{3})\\s+[^,]+,\\s+([^,]+),\\s+([0-9]+)ms]");
                        Matcher matcher = pattern.matcher(requestLine2);

                        if (matcher.find()) {
                            httpMethod = matcher.group(1);
                            String fullUrl = matcher.group(2);
                            String status = matcher.group(3);
                            String time = matcher.group(5);


                            try {
                                URI uri = new URI(fullUrl);
                                currentPath = uri.getPath();
                                System.out.println("Chemin extrait : " + currentPath);
                            } catch (Exception e) {
                                System.err.println("Erreur lors de l'extraction du path depuis l'URL : " + fullUrl);
                                currentPath = "Unknown Path";
                            }
                            try {
                                statusCode = Integer.parseInt(status);
                                success = (statusCode >= 200 && statusCode < 300);
                            } catch (NumberFormatException e) {
                                System.err.println("Impossible d'analyser le code de statut : " + status);
                                statusCode = 0;
                                success = false;
                            }
                            try {
                                responseTime = Long.parseLong(time);
                            } catch (NumberFormatException e) {
                                System.err.println("Impossible d'analyser le temps de réponse : " + time);
                                responseTime = 0;
                            }
                        } else {
                            System.out.println("Aucune correspondance trouvée pour l'URL : " + requestLine2);
                            currentPath = "Unknown Path";
                        }
                    }
                }

                if (line.toLowerCase().contains("error")) {
                    errorMessage = line;
                }
            }

            // Save the results of the last request if any
            if (currentPath != null && requestStarted && !"Unknown Path".equals(currentPath)) {
                saveTestResult(currentPath, httpMethod, success, statusCode, responseTime, errorMessage);
            } else {
                System.out.println("saveTestResult non appelé (fin de la boucle). currentPath est null: " + (currentPath == null) + ", requestStarted est false: " + (!requestStarted) + " or currentPath is Unknown Path");
            }

            int exitCode = process.waitFor();
            System.out.println(exitCode == 0 ? "Tous les tests ont été exécutés avec succès !" : "Certains tests ont échoué. Code de sortie : " + exitCode);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Une erreur s'est produite lors de l'exécution des tests Newman: " + e.getMessage());
        }
        System.out.println("Fin de l'exécution des tests Newman.");
    }

    @Transactional
    protected void saveTestResult(String path, String method, boolean success, int statusCode, long responseTime, String errorMessage) {
        // Do not save if path is unknown path
        if ("Unknown Path".equals(path)) {
            System.out.println("Ignorer l'enregistrement pour Unknown Path");
            return;
        }
        try {
            String level = (statusCode == 500) ? "ERROR" : "INFO";

            // Créez un objet JSON pour stocker le path et la méthode
            ObjectNode metadata = objectMapper.createObjectNode();
            metadata.put("path", path);
            metadata.put("method", method);

            // Recherchez une entrée existante avec le même path et la même méthode
            // Recherche d'un test existant dans la base de données par path et method.
            String metadataString = metadata.toString();

            Optional<ApiMonitoring> existingResult = resultRepository.findByMetadata(metadataString);

            ApiMonitoring result;
            if (existingResult.isPresent()) {

                // Mettre à jour l'entrée existante
                result = existingResult.get();
                result.setTemps(LocalDateTime.now());
                result.setResponseTime(responseTime);
                result.setStatusCode(statusCode);
                result.setErrorMessage(errorMessage);
                result.setLevel(level);

            } else {

                // Créer une nouvelle entrée
                result = new ApiMonitoring();
                result.setPath(path);
                result.setTemps(LocalDateTime.now());
                result.setResponseTime(responseTime);
                result.setStatusCode(statusCode);
                result.setErrorMessage(errorMessage);
                result.setLevel(level);
                result.setMetadata(metadataString); // Stocker le JSON dans la colonne metadata

            }

            resultRepository.save(result);
            System.out.println("Enregistrement réussi pour : " + path + " (" + method + ")");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur lors de l'enregistrement pour : " + path + " (" + method + "): " + e.getMessage());
        }
    }
}*/
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*@Service
public class NewmanService {

    private final ApiMonitoringRepository resultRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NewmanService(ApiMonitoringRepository resultRepository) {
        this.resultRepository = resultRepository;
        System.out.println("NewmanService créé avec le repository : " + resultRepository);
    }

    @Scheduled(cron = "0 * * * * *") // Exécution toutes les minutes
    public void runNewmanTests() {
        System.out.println("Début de l'exécution des tests Newman.");

        try {
            // Récupérer le chemin absolu du fichier postman_collection.json dans le dossier resources
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("postman_collection.json").toURI());

            System.out.println("Chemin du fichier de collection Postman : " + file.getAbsolutePath());

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "C:\\Users\\LENOVO\\AppData\\Roaming\\npm\\newman.cmd",
                    "run", file.getAbsolutePath() // Utilisation du chemin absolu du fichier
            );

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            String currentPath = null;
            String httpMethod = null;
            boolean success = false;
            int statusCode = 0;
            long responseTime = 0;
            String errorMessage = null;
            boolean requestStarted = false;

            while ((line = reader.readLine()) != null) {
                System.out.println("Ligne lue : " + line);

                // Check if a new request is starting based on the "□" character
                if (line.startsWith("□ ")) {

                    String apiPath = line.substring(2).trim();
                    currentPath = apiPath;
                    System.out.println("Début de la requête : " + currentPath);

                    // Read the lines containing the request details (URL, status, etc.)
                    String requestLine1 = reader.readLine(); // Line containing "└"
                    System.out.println("Ligne de requête 1 : " + requestLine1);

                    String requestLine2 = reader.readLine(); // Line containing request details
                    System.out.println("Ligne de requête 2 : " + requestLine2);

                    // Extract request details using regex
                    if (requestLine2 != null && requestLine2.contains(" ")) {
                        Pattern pattern = Pattern.compile("^\\s*(GET|POST|PUT|DELETE)\\s+(https?://[^\\s]+)\\s+\\[(\\d{3})\\s+[^,]+,\\s+([^,]+),\\s+([0-9]+)ms]");
                        Matcher matcher = pattern.matcher(requestLine2);

                        if (matcher.find()) {
                            httpMethod = matcher.group(1);
                            String fullUrl = matcher.group(2);
                            String status = matcher.group(3);
                            String time = matcher.group(5);

                            try {
                                URI uri = new URI(fullUrl);
                                currentPath = uri.getPath();
                                System.out.println("Chemin extrait : " + currentPath);
                            } catch (Exception e) {
                                System.err.println("Erreur lors de l'extraction du path depuis l'URL : " + fullUrl);
                                currentPath = "Unknown Path";
                            }
                            try {
                                statusCode = Integer.parseInt(status);
                                success = (statusCode >= 200 && statusCode < 300);
                            } catch (NumberFormatException e) {
                                System.err.println("Impossible d'analyser le code de statut : " + status);
                                statusCode = 0;
                                success = false;
                            }
                            try {
                                responseTime = Long.parseLong(time);
                            } catch (NumberFormatException e) {
                                System.err.println("Impossible d'analyser le temps de réponse : " + time);
                                responseTime = 0;
                            }

                            requestStarted = true;
                            if (requestLine2.contains("[errored]")) {
                                // Capture error message from the current line itself.
                                errorMessage = "Request Errored: " + requestLine2;
                                processTestResult(currentPath, httpMethod, success, statusCode, responseTime, errorMessage); // Update/Insert and set ERROR level
                                requestStarted = false; // mark the request complete
                            }

                        } else {
                            System.out.println("Aucune correspondance trouvée pour l'URL : " + requestLine2);
                            currentPath = "Unknown Path";
                        }
                    }
                }

                if (line.toLowerCase().contains("error") && currentPath != null && !"Unknown Path".equals(currentPath) && requestStarted) {
                    errorMessage = line;
                    processTestResult(currentPath, httpMethod, success, statusCode, responseTime, errorMessage);
                    requestStarted = false; //Ensure that an errord request is handled.
                }
            }

            int exitCode = process.waitFor();
            System.out.println(exitCode == 0 ? "Tous les tests ont été exécutés avec succès !" : "Certains tests ont échoué. Code de sortie : " + exitCode);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Une erreur s'est produite lors de l'exécution des tests Newman: " + e.getMessage());
        }
        System.out.println("Fin de l'exécution des tests Newman.");
    }

    @Transactional
    protected void processTestResult(String path, String method, boolean success, int statusCode, long responseTime, String errorMessage) {
        // Do not save if path is unknown path
        if ("Unknown Path".equals(path)) {
            System.out.println("Ignorer l'enregistrement pour Unknown Path");
            return;
        }

        try {
            String level = (statusCode == 500 || (errorMessage != null && !errorMessage.isEmpty())) ? "ERROR" : "INFO";

            // Créez un objet JSON pour stocker le path et la méthode
            ObjectNode metadata = objectMapper.createObjectNode();
            metadata.put("path", path);
            metadata.put("method", method);
            String metadataString = metadata.toString();

            Optional<ApiMonitoring> existingResult = resultRepository.findByMetadata(metadataString);

            ApiMonitoring result;
            if (existingResult.isPresent()) {
                // Mettre à jour l'entrée existante
                result = existingResult.get();
                result.setTemps(LocalDateTime.now());
                result.setResponseTime(responseTime);
                result.setStatusCode(statusCode);
                result.setErrorMessage(errorMessage);
                result.setLevel(level);
                resultRepository.save(result); // Save the updated result
                System.out.println("Mise à jour réussie pour : " + path);

            } else {
                // Créer une nouvelle entrée
                result = new ApiMonitoring();
                result.setPath(path);
                result.setTemps(LocalDateTime.now());
                result.setResponseTime(responseTime);
                result.setStatusCode(statusCode);
                result.setErrorMessage(errorMessage);
                result.setLevel(level);
                result.setMetadata(metadataString); // Stocker le JSON dans la colonne metadata
                resultRepository.save(result); // Save the new result
                System.out.println("Enregistrement réussi pour : " + path);
            }



        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur lors de l'enregistrement pour : " + path + ": " + e.getMessage());
        }
    }
}*/
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*@Service
public class NewmanService {

    private final ApiMonitoringRepository resultRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NewmanService(ApiMonitoringRepository resultRepository) {
        this.resultRepository = resultRepository;
        System.out.println("NewmanService créé avec le repository : " + resultRepository);
    }

    @Scheduled(cron = "0 * * * * *") // Exécution toutes les minutes
    public void runNewmanTests() {
        System.out.println("Début de l'exécution des tests Newman.");

        try {
            // Récupérer le chemin absolu du fichier postman_collection.json dans le dossier resources
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("postman_collection.json").toURI());

            System.out.println("Chemin du fichier de collection Postman : " + file.getAbsolutePath());

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "C:\\Users\\LENOVO\\AppData\\Roaming\\npm\\newman.cmd",
                    "run", file.getAbsolutePath() // Utilisation du chemin absolu du fichier
            );

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            String currentPath = null;
            String httpMethod = null;
            boolean success = false;
            int statusCode = 0;
            long responseTime = 0;
            String errorMessage = null;
            boolean requestStarted = false;

            while ((line = reader.readLine()) != null) {
                System.out.println("Ligne lue : " + line);

                // Check if a new request is starting based on the "□" character
                if (line.startsWith("□ ")) {

                    String apiPath = line.substring(2).trim();
                    currentPath = apiPath;
                    System.out.println("Début de la requête : " + currentPath);

                    // Read the lines containing the request details (URL, status, etc.)
                    String requestLine1 = reader.readLine(); // Line containing "└"
                    System.out.println("Ligne de requête 1 : " + requestLine1);

                    String requestLine2 = reader.readLine(); // Line containing request details
                    System.out.println("Ligne de requête 2 : " + requestLine2);

                    // Extract request details using regex
                    if (requestLine2 != null && requestLine2.contains(" ")) {
                        Pattern pattern = Pattern.compile("^\\s*(GET|POST|PUT|DELETE)\\s+(https?://[^\\s]+)\\s+\\[(\\d{3})\\s+[^,]+,\\s+([^,]+),\\s+([0-9]+)ms]");
                        Matcher matcher = pattern.matcher(requestLine2);

                        if (matcher.find()) {
                            httpMethod = matcher.group(1);
                            String fullUrl = matcher.group(2);
                            String status = matcher.group(3);
                            String time = matcher.group(5);

                            try {
                                URI uri = new URI(fullUrl);
                                currentPath = uri.getPath();

                            } catch (Exception e) {
                                System.err.println("Erreur lors de l'extraction du path depuis l'URL : " + fullUrl);
                                currentPath = "Unknown Path";
                            }

                            requestStarted = true;

                            if (requestLine2.contains("[errored]")) {
                                // Capture error message from the current line itself.
                                errorMessage = "Request Errored: " + requestLine2;
                                processTestResult(currentPath, httpMethod, success, statusCode, responseTime, errorMessage); // Update/Insert and set ERROR level
                                requestStarted = false; // mark the request complete
                            }

                        } else {
                            // URL extraction failed
                            currentPath = "Unknown Path";

                            // Do not print 'Aucune correspondance trouvée pour l'URL' anymore.
                            //System.out.println("Aucune correspondance trouvée pour l'URL : " + requestLine2); // Remove
                        }
                    }
                }

                if (line.toLowerCase().contains("error") && currentPath != null && !"Unknown Path".equals(currentPath) && requestStarted) {
                    errorMessage = line;
                    processTestResult(currentPath, httpMethod, success, statusCode, responseTime, errorMessage);
                    requestStarted = false; //Ensure that an errord request is handled.
                }
            }

            int exitCode = process.waitFor();
            System.out.println(exitCode == 0 ? "Tous les tests ont été exécutés avec succès !" : "Certains tests ont échoué. Code de sortie : " + exitCode);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Une erreur s'est produite lors de l'exécution des tests Newman: " + e.getMessage());
        }
        System.out.println("Fin de l'exécution des tests Newman.");
    }

    @Transactional
    protected void processTestResult(String path, String method, boolean success, int statusCode, long responseTime, String errorMessage) {
        // Do not save if path is unknown path
        if ("Unknown Path".equals(path)) {
            System.out.println("Ignorer l'enregistrement pour Unknown Path");
            return;
        }

        try {
            String level = (statusCode == 500 || (errorMessage != null && !errorMessage.isEmpty())) ? "ERROR" : "INFO";

            // Créez un objet JSON pour stocker le path et la méthode
            ObjectNode metadata = objectMapper.createObjectNode();
            metadata.put("path", path);
            metadata.put("method", method);
            String metadataString = metadata.toString();

            Optional<ApiMonitoring> existingResult = resultRepository.findByMetadata(metadataString);

            ApiMonitoring result;
            if (existingResult.isPresent()) {
                // Mettre à jour l'entrée existante
                result = existingResult.get();
                result.setTemps(LocalDateTime.now());
                result.setResponseTime(responseTime);
                result.setStatusCode(statusCode);
                result.setErrorMessage(errorMessage);
                result.setLevel(level);
                resultRepository.save(result); // Save the updated result
                System.out.println("Mise à jour réussie pour : " + path);

            } else {
                // Extract request details using regex
                result = new ApiMonitoring();
                result.setPath(path);
                result.setTemps(LocalDateTime.now());
                result.setResponseTime(responseTime);
                result.setStatusCode(statusCode);
                result.setErrorMessage(errorMessage);
                result.setLevel(level);
                result.setMetadata(metadataString); // Stocker le JSON dans la colonne metadata
                resultRepository.save(result); // Save the new result
                System.out.println("Enregistrement réussi pour : " + path);
            }



        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur lors de l'enregistrement pour : " + path + ": " + e.getMessage());
        }
    }
}*/
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*@Service
public class NewmanService {

    private final ApiMonitoringRepository resultRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NewmanService(ApiMonitoringRepository resultRepository) {
        this.resultRepository = resultRepository;
        System.out.println("NewmanService créé avec le repository : " + resultRepository);
    }

    @Scheduled(cron = "0 * * * * *") // Exécution toutes les minutes
    public void runNewmanTests() {
        System.out.println("Début de l'exécution des tests Newman.");

        try {
            // Récupérer le chemin absolu du fichier postman_collection.json dans le dossier resources
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("postman_collection.json").toURI());

            System.out.println("Chemin du fichier de collection Postman : " + file.getAbsolutePath());

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "C:\\Users\\LENOVO\\AppData\\Roaming\\npm\\newman.cmd",
                    "run", file.getAbsolutePath() // Utilisation du chemin absolu du fichier
            );

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            String currentPath = null;
            String httpMethod = null;
            boolean success = false;
            int statusCode = 0;
            long responseTime = 0;
            String errorMessage = null;
            boolean requestStarted = false;

            while ((line = reader.readLine()) != null) {
                System.out.println("Ligne lue : " + line);

                // Check if a new request is starting based on the "□" character
                if (line.startsWith("□ ")) {

                    String apiPath = line.substring(2).trim();
                    currentPath = apiPath;
                    System.out.println("Début de la requête : " + currentPath);

                    // Read the lines containing the request details (URL, status, etc.)
                    String requestLine1 = reader.readLine(); // Line containing "└"
                    System.out.println("Ligne de requête 1 : " + requestLine1);

                    String requestLine2 = reader.readLine(); // Line containing request details
                    System.out.println("Ligne de requête 2 : " + requestLine2);

                    // Extract request details using regex
                    if (requestLine2 != null && requestLine2.contains(" ")) {
                        Pattern pattern = Pattern.compile("^\\s*(GET|POST|PUT|DELETE)\\s+(https?://[^\\s]+)\\s+\\[(\\d{3})\\s+[^,]+,\\s+([^,]+),\\s+([0-9]+)ms]");
                        Matcher matcher = pattern.matcher(requestLine2);

                        if (matcher.find()) {
                            httpMethod = matcher.group(1);
                            String fullUrl = matcher.group(2);
                            String status = matcher.group(3);
                            String time = matcher.group(5);

                            try {
                                URI uri = new URI(fullUrl);
                                currentPath = uri.getPath();

                                // Assign status code and response time if parsing is successful
                                statusCode = Integer.parseInt(status);
                                responseTime = Long.parseLong(time);

                            } catch (Exception e) {
                                System.err.println("Erreur lors de l'extraction du path depuis l'URL : " + fullUrl);
                                currentPath = "Unknown Path";

                                // if an exception is thrown, then assign the status code to 0 and set the status
                                // errorMessage = "Impossible d'analyser le code de statut ou le temps de réponse. Request Marked Errored";
                                statusCode = 0;
                                responseTime = 0;
                            }

                            requestStarted = true;

                            if (requestLine2.contains("[errored]")) {

                                // Capture error message from the current line itself.
                                errorMessage = "Request Errored: " + requestLine2;

                                // If statusCode and responseTime are zero then assign a default
                                if (statusCode == 0 || responseTime == 0) {
                                    statusCode = 0;
                                    responseTime = 0;
                                }

                                processTestResult(currentPath, httpMethod, success, statusCode, responseTime, errorMessage); // Update/Insert and set ERROR level
                                requestStarted = false; // mark the request complete
                            }

                        } else {
                            // URL extraction failed
                            currentPath = "Unknown Path";
                            // Do not print 'Aucune correspondance trouvée pour l'URL' anymore.
                            //System.out.println("Aucune correspondance trouvée pour l'URL : " + requestLine2); // Remove
                        }
                    }
                }

                if (line.toLowerCase().contains("error") && currentPath != null && !"Unknown Path".equals(currentPath) && requestStarted) {
                    errorMessage = line;
                    processTestResult(currentPath, httpMethod, success, statusCode, responseTime, errorMessage);
                    requestStarted = false; //Ensure that an errord request is handled.
                }
            }

            int exitCode = process.waitFor();
            System.out.println(exitCode == 0 ? "Tous les tests ont été exécutés avec succès !" : "Certains tests ont échoué. Code de sortie : " + exitCode);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Une erreur s'est produite lors de l'exécution des tests Newman: " + e.getMessage());
        }
        System.out.println("Fin de l'exécution des tests Newman.");
    }

    @Transactional
    protected void processTestResult(String path, String method, boolean success, int statusCode, long responseTime, String errorMessage) {
        // Do not save if path is unknown path
        if ("Unknown Path".equals(path)) {
            System.out.println("Ignorer l'enregistrement pour Unknown Path");
            return;
        }

        try {
            String level = (statusCode == 500 || (errorMessage != null && !errorMessage.isEmpty())) ? "ERROR" : "INFO";

            // Créez un objet JSON pour stocker le path et la méthode
            ObjectNode metadata = objectMapper.createObjectNode();
            metadata.put("path", path);
            metadata.put("method", method);
            String metadataString = metadata.toString();

            Optional<ApiMonitoring> existingResult = resultRepository.findByMetadata(metadataString);

            ApiMonitoring result;
            if (existingResult.isPresent()) {
                // Mettre à jour l'entrée existante
                result = existingResult.get();
                result.setTemps(LocalDateTime.now());
                result.setResponseTime(responseTime);
                result.setStatusCode(statusCode);
                result.setErrorMessage(errorMessage);
                result.setLevel(level);
                resultRepository.save(result); // Save the updated result
                System.out.println("Mise à jour réussie pour : " + path);

            } else {
                // Extract request details using regex
                result = new ApiMonitoring();
                result.setPath(path);
                result.setTemps(LocalDateTime.now());
                result.setResponseTime(responseTime);
                result.setStatusCode(statusCode);
                result.setErrorMessage(errorMessage);
                result.setLevel(level);
                result.setMetadata(metadataString); // Stocker le JSON dans la colonne metadata
                resultRepository.save(result); // Save the new result
                System.out.println("Enregistrement réussi pour : " + path);
            }



        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur lors de l'enregistrement pour : " + path + ": " + e.getMessage());
        }
    }
}*/
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*@Service
public class NewmanService {

    private final ApiMonitoringRepository resultRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NewmanService(ApiMonitoringRepository resultRepository) {
        this.resultRepository = resultRepository;
        System.out.println("NewmanService créé avec le repository : " + resultRepository);
    }

    @Scheduled(cron = "0 * * * * *") // Exécution toutes les minutes
    public void runNewmanTests() {
        System.out.println("Début de l'exécution des tests Newman.");

        try {
            // Récupérer le chemin absolu du fichier postman_collection.json dans le dossier resources
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("postman_collection.json").toURI());

            System.out.println("Chemin du fichier de collection Postman : " + file.getAbsolutePath());

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "C:\\Users\\LENOVO\\AppData\\Roaming\\npm\\newman.cmd",
                    "run", file.getAbsolutePath() // Utilisation du chemin absolu du fichier
            );

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            String currentPath = null;
            String httpMethod = null;
            boolean success = false;
            int statusCode = 0;
            long responseTime = 0;
            String errorMessage = null;
            boolean requestStarted = false;

            while ((line = reader.readLine()) != null) {
                System.out.println("Ligne lue : " + line);

                // Check if a new request is starting based on the "□" character
                if (line.startsWith("□ ")) {
                    String apiPath = line.substring(2).trim();
                    currentPath = apiPath;
                    System.out.println("Début de la requête : " + currentPath);

                    // Read the lines containing the request details (URL, status, etc.)
                    String requestLine1 = reader.readLine(); // Line containing "└"
                    System.out.println("Ligne de requête 1 : " + requestLine1);

                    String requestLine2 = reader.readLine(); // Line containing request details
                    System.out.println("Ligne de requête 2 : " + requestLine2);

                    // Initialize statusCode and responseTime to 0
                    statusCode = 0;
                    responseTime = 0;

                    // Extract request details using regex
                    if (requestLine2 != null && requestLine2.contains(" ")) {
                        Pattern pattern = Pattern.compile("^\\s*(GET|POST|PUT|DELETE)\\s+(https?://[^\\s]+)\\s+\\[(\\d{3})\\s+[^,]+,\\s+([^,]+),\\s+([0-9]+)ms]");
                        Matcher matcher = pattern.matcher(requestLine2);

                        if (matcher.find()) {
                            httpMethod = matcher.group(1);
                            String fullUrl = matcher.group(2);
                            String status = matcher.group(3);
                            String time = matcher.group(5);

                            try {
                                URI uri = new URI(fullUrl);
                                currentPath = uri.getPath();
                                // Assign status code and response time if parsing is successful
                                statusCode = Integer.parseInt(status);
                                responseTime = Long.parseLong(time);

                            } catch (Exception e) {
                                System.err.println("Erreur lors de l'extraction du path depuis l'URL : " + fullUrl);
                                currentPath = "Unknown Path";
                                errorMessage = "Erreur lors de l'extraction des détails de la requête";
                            }

                            requestStarted = true;

                            if (requestLine2.contains("[errored]")) {
                                // Capture error message from the current line itself.
                                errorMessage = "Request Errored: " + requestLine2;

                                processTestResult(currentPath, httpMethod, success, statusCode, responseTime, errorMessage); // Update/Insert and set ERROR level
                                requestStarted = false; // mark the request complete
                            }

                        } else {
                            // URL extraction failed
                            currentPath = "Unknown Path";
                            errorMessage = "Impossible d'extraire les informations de la requête";

                        }
                    } else {
                        errorMessage = "Ligne de requête 2 est vide ou mal formée";
                        currentPath = "Unknown Path"; // mark request as UNKNOWN PATH
                    }

                    //Process test result if there was an issue with parsing
                    if(currentPath.equals("Unknown Path"))
                    {
                        processTestResult(currentPath, httpMethod, success, statusCode, responseTime, errorMessage);
                        requestStarted = false;
                    }

                }
                if (line.toLowerCase().contains("error") && currentPath != null && !"Unknown Path".equals(currentPath) && requestStarted) {
                    errorMessage = line;
                    processTestResult(currentPath, httpMethod, success, statusCode, responseTime, errorMessage);
                    requestStarted = false; //Ensure that an errord request is handled.
                }
            }

            int exitCode = process.waitFor();
            System.out.println(exitCode == 0 ? "Tous les tests ont été exécutés avec succès !" : "Certains tests ont échoué. Code de sortie : " + exitCode);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Une erreur s'est produite lors de l'exécution des tests Newman: " + e.getMessage());
        }
        System.out.println("Fin de l'exécution des tests Newman.");
    }

    @Transactional
    protected void processTestResult(String path, String method, boolean success, int statusCode, long responseTime, String errorMessage) {
        // Do not save if path is unknown path
        if ("Unknown Path".equals(path)) {
            System.out.println("Ignorer l'enregistrement pour Unknown Path");
            return;
        }

        try {
            String level = (statusCode == 500 || (errorMessage != null && !errorMessage.isEmpty())) ? "ERROR" : "INFO";

            // Créez un objet JSON pour stocker le path et la méthode
            ObjectNode metadata = objectMapper.createObjectNode();
            metadata.put("path", path);
            metadata.put("method", method);
            String metadataString = metadata.toString();

            Optional<ApiMonitoring> existingResult = resultRepository.findByMetadata(metadataString);

            ApiMonitoring result;
            if (existingResult.isPresent()) {
                // Mettre à jour l'entrée existante
                result = existingResult.get();
                result.setTemps(LocalDateTime.now());
                result.setResponseTime(responseTime);
                result.setStatusCode(statusCode);
                result.setErrorMessage(errorMessage);
                result.setLevel(level);
                resultRepository.save(result); // Save the updated result
                System.out.println("Mise à jour réussie pour : " + path);

            } else {
                // Extract request details using regex
                result = new ApiMonitoring();
                result.setPath(path);
                result.setTemps(LocalDateTime.now());
                result.setResponseTime(responseTime);
                result.setStatusCode(statusCode);
                result.setErrorMessage(errorMessage);
                result.setLevel(level);
                result.setMetadata(metadataString); // Stocker le JSON dans la colonne metadata
                resultRepository.save(result); // Save the new result
                System.out.println("Enregistrement réussi pour : " + path);
            }



        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur lors de l'enregistrement pour : " + path + ": " + e.getMessage());
        }
    }
}*/
/* hedhaaaaaa jawouuuuuuuuuu behiiiiiiiiiiiii*/
/*@Service
public class NewmanService {

    private final ApiMonitoringRepository resultRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NewmanService(ApiMonitoringRepository resultRepository) {
        this.resultRepository = resultRepository;
        System.out.println("NewmanService créé avec le repository : " + resultRepository);
    }

    @Scheduled(cron = "0 * * * * *") // Exécution toutes les minutes
    public void runNewmanTests() {
        System.out.println("Début de l'exécution des tests Newman.");

        try {
            // Récupérer le chemin absolu du fichier postman_collection.json dans le dossier resources
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("postman_collection.json").toURI());

            System.out.println("Chemin du fichier de collection Postman : " + file.getAbsolutePath());

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "C:\\Users\\LENOVO\\AppData\\Roaming\\npm\\newman.cmd",
                    "run", file.getAbsolutePath() // Utilisation du chemin absolu du fichier
            );

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            String currentPath = null;
            String httpMethod = null;
            boolean success = false;
            int statusCode = 0;
            long responseTime = 0;
            String errorMessage = null;
            boolean requestStarted = false;

            while ((line = reader.readLine()) != null) {
                System.out.println("Ligne lue : " + line);

                // Check if a new request is starting based on the "□" character
                if (line.startsWith("□ ")) {
                    String apiPath = line.substring(2).trim();
                    currentPath = apiPath;
                    System.out.println("Début de la requête : " + currentPath);

                    // Read the lines containing the request details (URL, status, etc.)
                    String requestLine1 = reader.readLine(); // Line containing "└"
                    System.out.println("Ligne de requête 1 : " + requestLine1);

                    String requestLine2 = reader.readLine(); // Line containing request details
                    System.out.println("Ligne de requête 2 : " + requestLine2);

                    // Initialize statusCode and responseTime to 0
                    statusCode = 0;
                    responseTime = 0;

                    // Extract request details using regex
                    if (requestLine2 != null && requestLine2.contains(" ")) {
                        // Regex to extract method and URL (even if the line contains [errored])
                        Pattern pattern = Pattern.compile("^\\s*(GET|POST|PUT|DELETE)\\s+(https?://[^\\s]+)");
                        Matcher matcher = pattern.matcher(requestLine2);

                        if (matcher.find()) {
                            httpMethod = matcher.group(1);
                            String fullUrl = matcher.group(2);

                            try {
                                // Extract the path from the full URL
                                URI uri = new URI(fullUrl);
                                currentPath = uri.getPath(); // This will give the path part of the URL
                                System.out.println("Chemin extrait : " + currentPath);

                                // If the request is errored, set statusCode and responseTime to 0
                                if (requestLine2.contains("[errored]")) {
                                    statusCode = 0;
                                    responseTime = 0;
                                    errorMessage = "Request Errored: " + requestLine2;
                                } else {
                                    // Extract status code and response time if available
                                    Pattern statusPattern = Pattern.compile("\\[(\\d{3})\\s+[^,]+,\\s+([^,]+),\\s+([0-9]+)ms]");
                                    Matcher statusMatcher = statusPattern.matcher(requestLine2);
                                    if (statusMatcher.find()) {
                                        statusCode = Integer.parseInt(statusMatcher.group(1));
                                        responseTime = Long.parseLong(statusMatcher.group(3));
                                    }
                                }

                            } catch (Exception e) {
                                System.err.println("Erreur lors de l'extraction du path depuis l'URL : " + fullUrl);
                                currentPath = "Unknown Path";
                                errorMessage = "Erreur lors de l'extraction des détails de la requête";
                            }

                            requestStarted = true;

                            // Process the test result
                            processTestResult(currentPath, httpMethod, success, statusCode, responseTime, errorMessage);
                            requestStarted = false; // mark the request complete

                        } else {
                            // URL extraction failed
                            currentPath = "Unknown Path";
                            errorMessage = "Impossible d'extraire les informations de la requête";
                            System.err.println("Erreur : Impossible d'extraire les informations de la requête.");
                        }
                    } else {
                        errorMessage = "Ligne de requête 2 est vide ou mal formée";
                        currentPath = "Unknown Path"; // mark request as UNKNOWN PATH
                        System.err.println("Erreur : Ligne de requête 2 est vide ou mal formée.");
                    }

                    // Process test result if there was an issue with parsing
                    if (currentPath.equals("Unknown Path")) {
                        processTestResult(currentPath, httpMethod, success, statusCode, responseTime, errorMessage);
                        requestStarted = false;
                    }

                }
                if (line.toLowerCase().contains("error") && currentPath != null && !"Unknown Path".equals(currentPath) && requestStarted) {
                    errorMessage = line;
                    processTestResult(currentPath, httpMethod, success, statusCode, responseTime, errorMessage);
                    requestStarted = false; // Ensure that an errored request is handled.
                }
            }

            int exitCode = process.waitFor();
            System.out.println(exitCode == 0 ? "Tous les tests ont été exécutés avec succès !" : "Certains tests ont échoué. Code de sortie : " + exitCode);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Une erreur s'est produite lors de l'exécution des tests Newman: " + e.getMessage());
        }
        System.out.println("Fin de l'exécution des tests Newman.");
    }

    @Transactional
    protected void processTestResult(String path, String method, boolean success, int statusCode, long responseTime, String errorMessage) {
        // Do not save if path is unknown path
        if ("Unknown Path".equals(path)) {
            System.out.println("Ignorer l'enregistrement pour Unknown Path");
            return;
        }

        try {
            String level = (statusCode == 500 || (errorMessage != null && !errorMessage.isEmpty())) ? "ERROR" : "INFO";

            // Créez un objet JSON pour stocker le path et la méthode
            ObjectNode metadata = objectMapper.createObjectNode();
            metadata.put("path", path);
            metadata.put("method", method);
            String metadataString = metadata.toString();

            Optional<ApiMonitoring> existingResult = resultRepository.findByMetadata(metadataString);

            ApiMonitoring result;
            if (existingResult.isPresent()) {
                // Mettre à jour l'entrée existante
                result = existingResult.get();
                result.setTemps(LocalDateTime.now());
                result.setResponseTime(responseTime);
                result.setStatusCode(statusCode);
                result.setErrorMessage(errorMessage);
                result.setLevel(level);
                resultRepository.save(result); // Save the updated result
                System.out.println("Mise à jour réussie pour : " + path);

            } else {
                // Extract request details using regex
                result = new ApiMonitoring();
                result.setPath(path);
                result.setTemps(LocalDateTime.now());
                result.setResponseTime(responseTime);
                result.setStatusCode(statusCode);
                result.setErrorMessage(errorMessage);
                result.setLevel(level);
                result.setMetadata(metadataString); // Stocker le JSON dans la colonne metadata
                resultRepository.save(result); // Save the new result
                System.out.println("Enregistrement réussi pour : " + path);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur lors de l'enregistrement pour : " + path + ": " + e.getMessage());
        }
    }
}*/
/*import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
*/
/*@Service
public class NewmanService {

    private final ApiMonitoringRepository resultRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NewmanService(ApiMonitoringRepository resultRepository) {
        this.resultRepository = resultRepository;
        System.out.println("NewmanService créé avec le repository : " + resultRepository);
    }

    @Scheduled(cron = "0 * * * * *") // Exécution toutes les minutes
    public void runNewmanTests() {
        System.out.println("Début de l'exécution des tests Newman.");

        try {
            // Récupérer le chemin absolu du fichier postman_collection.json dans le dossier resources
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("postman_collection.json").toURI());

            System.out.println("Chemin du fichier de collection Postman : " + file.getAbsolutePath());

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "C:\\Users\\LENOVO\\AppData\\Roaming\\npm\\newman.cmd",
                    "run", file.getAbsolutePath() // Utilisation du chemin absolu du fichier
            );

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            String currentPath = null;
            String httpMethod = null;
            boolean success = false;
            int statusCode = 0;
            long responseTime = 0;
            String errorMessage = null;
            boolean requestStarted = false;

            while ((line = reader.readLine()) != null) {
                System.out.println("Ligne lue : " + line);

                // Check if a new request is starting based on the "□" character
                if (line.startsWith("□ ")) {
                    String apiPath = line.substring(2).trim();
                    currentPath = apiPath;
                    System.out.println("Début de la requête : " + currentPath);

                    // Read the lines containing the request details (URL, status, etc.)
                    String requestLine1 = reader.readLine(); // Line containing "└"
                    System.out.println("Ligne de requête 1 : " + requestLine1);

                    String requestLine2 = reader.readLine(); // Line containing request details
                    System.out.println("Ligne de requête 2 : " + requestLine2);

                    // Initialize statusCode and responseTime to 0
                    statusCode = 0;
                    responseTime = 0;

                    // Extract request details using regex
                    if (requestLine2 != null && requestLine2.contains(" ")) {
                        Pattern pattern = Pattern.compile("^\\s*(GET|POST|PUT|DELETE)\\s+(https?://[^\\s]+)\\s+\\[(\\d{3})\\s+[^,]+,\\s+([^,]+),\\s+([0-9]+)ms]");
                        Matcher matcher = pattern.matcher(requestLine2);

                        if (matcher.find()) {
                            httpMethod = matcher.group(1);
                            String fullUrl = matcher.group(2);
                            String status = matcher.group(3);
                            String time = matcher.group(5);

                            try {
                                URI uri = new URI(fullUrl);
                                currentPath = uri.getPath();
                                // Assign status code and response time if parsing is successful
                                statusCode = Integer.parseInt(status);
                                responseTime = Long.parseLong(time);

                            } catch (Exception e) {
                                System.err.println("Erreur lors de l'extraction du path depuis l'URL : " + fullUrl);
                                currentPath = "Unknown Path";
                                errorMessage = "Erreur lors de l'extraction des détails de la requête";
                            }

                            requestStarted = true;

                            if (requestLine2.contains("[errored]")) {
                                // Capture error message from the current line itself.
                                errorMessage = "Request Errored: " + requestLine2;

                                processTestResult(currentPath, httpMethod, success, statusCode, responseTime, errorMessage); // Update/Insert and set ERROR level
                                requestStarted = false; // mark the request complete
                            }

                        } else {
                            // URL extraction failed
                            currentPath = "Unknown Path";
                            errorMessage = "Impossible d'extraire les informations de la requête";

                        }
                    } else {
                        errorMessage = "Ligne de requête 2 est vide ou mal formée";
                        currentPath = "Unknown Path"; // mark request as UNKNOWN PATH
                    }

                    //Process test result if there was an issue with parsing
                    if(currentPath.equals("Unknown Path"))
                    {
                        processTestResult(currentPath, httpMethod, success, statusCode, responseTime, errorMessage);
                        requestStarted = false;
                    }

                }
                if (line.toLowerCase().contains("error") && currentPath != null && !"Unknown Path".equals(currentPath) && requestStarted) {
                    errorMessage = line;
                    processTestResult(currentPath, httpMethod, success, statusCode, responseTime, errorMessage);
                    requestStarted = false; //Ensure that an errord request is handled.
                }
            }

            int exitCode = process.waitFor();
            System.out.println(exitCode == 0 ? "Tous les tests ont été exécutés avec succès !" : "Certains tests ont échoué. Code de sortie : " + exitCode);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Une erreur s'est produite lors de l'exécution des tests Newman: " + e.getMessage());
        }
        System.out.println("Fin de l'exécution des tests Newman.");
    }

    @Transactional
    protected void processTestResult(String path, String method, boolean success, int statusCode, long responseTime, String errorMessage) {
        // Do not save if path is unknown path
        if ("Unknown Path".equals(path)) {
            System.out.println("Ignorer l'enregistrement pour Unknown Path");
            return;
        }

        try {
            String level = (statusCode == 500 || (errorMessage != null && !errorMessage.isEmpty())) ? "ERROR" : "INFO";

            // Créez un objet JSON pour stocker le path et la méthode
            ObjectNode metadata = objectMapper.createObjectNode();
            metadata.put("path", path);
            metadata.put("method", method);
            String metadataString = metadata.toString();

            Optional<ApiMonitoring> existingResult = resultRepository.findByMetadata(metadataString);

            ApiMonitoring result;
            if (existingResult.isPresent()) {
                // Mettre à jour l'entrée existante
                result = existingResult.get();
                result.setTemps(LocalDateTime.now());
                result.setResponseTime(responseTime);
                result.setStatusCode(statusCode);
                result.setErrorMessage(errorMessage);
                result.setLevel(level);
                resultRepository.save(result); // Save the updated result
                System.out.println("Mise à jour réussie pour : " + path);

            } else {
                // Extract request details using regex
                result = new ApiMonitoring();
                result.setPath(path);
                result.setTemps(LocalDateTime.now());
                result.setResponseTime(responseTime);
                result.setStatusCode(statusCode);
                result.setErrorMessage(errorMessage);
                result.setLevel(level);
                result.setMetadata(metadataString); // Stocker le JSON dans la colonne metadata
                resultRepository.save(result); // Save the new result
                System.out.println("Enregistrement réussi pour : " + path);
            }



        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur lors de l'enregistrement pour : " + path + ": " + e.getMessage());
        }
    }
}*/
/*@Service
public class NewmanService {

    private final ApiMonitoringRepository resultRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NewmanService(ApiMonitoringRepository resultRepository) {
        this.resultRepository = resultRepository;
        System.out.println("NewmanService créé avec le repository : " + resultRepository);
    }

    @Scheduled(cron = "0 * * * * *") // Exécution toutes les minutes
    public void runNewmanTests() {
        System.out.println("Début de l'exécution des tests Newman.");

        try {
            // Récupérer le chemin absolu du fichier postman_collection.json dans le dossier resources
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("postman_collection.json").toURI());

            System.out.println("Chemin du fichier de collection Postman : " + file.getAbsolutePath());

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "C:\\Users\\LENOVO\\AppData\\Roaming\\npm\\newman.cmd",
                    "run", file.getAbsolutePath() // Utilisation du chemin absolu du fichier
            );

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            String currentPath = null;
            String httpMethod = null;
            boolean success = false;
            int statusCode = 0;
            long responseTime = 0;
            StringBuilder errorMessageBuilder = new StringBuilder();
            boolean requestStarted = false;
            boolean failureDetected = false;

            while ((line = reader.readLine()) != null) {
                System.out.println("Ligne lue : " + line);

                // Check if a new request is starting based on the "□" character
                if (line.startsWith("□ ")) {
                    String apiPath = line.substring(2).trim();
                    currentPath = apiPath;
                    System.out.println("Début de la requête : " + currentPath);

                    // Read the lines containing the request details (URL, status, etc.)
                    String requestLine1 = reader.readLine(); // Line containing "└"
                    System.out.println("Ligne de requête 1 : " + requestLine1);

                    String requestLine2 = reader.readLine(); // Line containing request details
                    System.out.println("Ligne de requête 2 : " + requestLine2);

                    // Initialize statusCode and responseTime to 0
                    statusCode = 0;
                    responseTime = 0;

                    // Extract request details using regex
                    if (requestLine2 != null && requestLine2.contains(" ")) {
                        // Regex to extract method and URL (even if the line contains [errored])
                        Pattern pattern = Pattern.compile("^\\s*(GET|POST|PUT|DELETE)\\s+(https?://[^\\s]+)");
                        Matcher matcher = pattern.matcher(requestLine2);

                        if (matcher.find()) {
                            httpMethod = matcher.group(1);
                            String fullUrl = matcher.group(2);

                            try {
                                // Extract the path from the full URL
                                URI uri = new URI(fullUrl);
                                currentPath = uri.getPath(); // This will give the path part of the URL
                                System.out.println("Chemin extrait : " + currentPath);

                                // If the request is errored, set statusCode and responseTime to 0
                                if (requestLine2.contains("[errored]")) {
                                    statusCode = 0;
                                    responseTime = 0;
                                    errorMessageBuilder.append("Request Errored: ").append(requestLine2).append("\n");
                                } else {
                                    // Extract status code and response time if available
                                    Pattern statusPattern = Pattern.compile("\\[(\\d{3})\\s+[^,]+,\\s+([^,]+),\\s+([0-9]+)ms]");
                                    Matcher statusMatcher = statusPattern.matcher(requestLine2);
                                    if (statusMatcher.find()) {
                                        statusCode = Integer.parseInt(statusMatcher.group(1));
                                        responseTime = Long.parseLong(statusMatcher.group(3));
                                    }
                                }

                            } catch (Exception e) {
                                System.err.println("Erreur lors de l'extraction du path depuis l'URL : " + fullUrl);
                                currentPath = "Unknown Path";
                                errorMessageBuilder.append("Erreur lors de l'extraction des détails de la requête").append("\n");
                            }

                            requestStarted = true;

                            // Process the test result
                            processTestResult(currentPath, httpMethod, success, statusCode, responseTime, errorMessageBuilder.toString());
                            errorMessageBuilder.setLength(0); // Clear the error message builder
                            requestStarted = false; // mark the request complete

                        } else {
                            // URL extraction failed
                            currentPath = "Unknown Path";
                            errorMessageBuilder.append("Impossible d'extraire les informations de la requête").append("\n");
                            System.err.println("Erreur : Impossible d'extraire les informations de la requête.");
                        }
                    } else {
                        errorMessageBuilder.append("Ligne de requête 2 est vide ou mal formée").append("\n");
                        currentPath = "Unknown Path"; // mark request as UNKNOWN PATH
                        System.err.println("Erreur : Ligne de requête 2 est vide ou mal formée.");
                    }

                    // Process test result if there was an issue with parsing
                    if (currentPath.equals("Unknown Path")) {
                        processTestResult(currentPath, httpMethod, success, statusCode, responseTime, errorMessageBuilder.toString());
                        errorMessageBuilder.setLength(0); // Clear the error message builder
                        requestStarted = false;
                    }

                }
                if (line.toLowerCase().contains("failure") && currentPath != null && !"Unknown Path".equals(currentPath)) {
                    failureDetected = true;
                }
                if (failureDetected && line.toLowerCase().contains("error") && currentPath != null && !"Unknown Path".equals(currentPath)) {
                    errorMessageBuilder.append(line).append("\n");
                }
                if (failureDetected && line.isEmpty()) {
                    failureDetected = false;
                    processTestResult(currentPath, httpMethod, success, statusCode, responseTime, errorMessageBuilder.toString());
                    errorMessageBuilder.setLength(0); // Clear the error message builder
                }
            }

            int exitCode = process.waitFor();
            System.out.println(exitCode == 0 ? "Tous les tests ont été exécutés avec succès !" : "Certains tests ont échoué. Code de sortie : " + exitCode);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Une erreur s'est produite lors de l'exécution des tests Newman: " + e.getMessage());
        }
        System.out.println("Fin de l'exécution des tests Newman.");
    }

    @Transactional
    protected void processTestResult(String path, String method, boolean success, int statusCode, long responseTime, String errorMessage) {
        // Do not save if path is unknown path
        if ("Unknown Path".equals(path)) {
            System.out.println("Ignorer l'enregistrement pour Unknown Path");
            return;
        }

        try {
            String level = (statusCode == 500 || (errorMessage != null && !errorMessage.isEmpty())) ? "ERROR" : "INFO";

            // Créez un objet JSON pour stocker le path et la méthode
            ObjectNode metadata = objectMapper.createObjectNode();
            metadata.put("path", path);
            metadata.put("method", method);
            String metadataString = metadata.toString();

            Optional<ApiMonitoring> existingResult = resultRepository.findByMetadata(metadataString);

            ApiMonitoring result;
            if (existingResult.isPresent()) {
                // Mettre à jour l'entrée existante
                result = existingResult.get();
                result.setTemps(LocalDateTime.now());
                result.setResponseTime(responseTime);
                result.setStatusCode(statusCode);
                result.setErrorMessage(errorMessage);
                result.setLevel(level);
                resultRepository.save(result); // Save the updated result
                System.out.println("Mise à jour réussie pour : " + path);

            } else {
                // Extract request details using regex
                result = new ApiMonitoring();
                result.setPath(path);
                result.setTemps(LocalDateTime.now());
                result.setResponseTime(responseTime);
                result.setStatusCode(statusCode);
                result.setErrorMessage(errorMessage);
                result.setLevel(level);
                result.setMetadata(metadataString); // Stocker le JSON dans la colonne metadata
                resultRepository.save(result); // Save the new result
                System.out.println("Enregistrement réussi pour : " + path);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur lors de l'enregistrement pour : " + path + ": " + e.getMessage());
        }
    }
}*/
//import java.util.List;
/*@Service
public class NewmanService {

    private final ApiMonitoringRepository resultRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NewmanService(ApiMonitoringRepository resultRepository) {
        this.resultRepository = resultRepository;
        System.out.println("NewmanService créé avec le repository : " + resultRepository);
    }

    @Scheduled(cron = "0 * * * * *") // Exécution toutes les minutes
    public void runNewmanTests() {
        System.out.println("Début de l'exécution des tests Newman.");

        try {
            // Récupérer le chemin absolu du fichier postman_collection.json dans le dossier resources
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("postman_collection.json").toURI());

            System.out.println("Chemin du fichier de collection Postman : " + file.getAbsolutePath());

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "C:\\Users\\LENOVO\\AppData\\Roaming\\npm\\newman.cmd",
                    "run", file.getAbsolutePath() // Utilisation du chemin absolu du fichier
            );

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            String currentPath = null;
            String httpMethod = null;
            boolean success = false;
            int statusCode = 0;
            long responseTime = 0;
            StringBuilder errorMessageBuilder = new StringBuilder();
            boolean requestStarted = false;
            boolean failureDetected = false;

            while ((line = reader.readLine()) != null) {
                System.out.println("Ligne lue : " + line);

                // Check if a new request is starting based on the "□" character
                if (line.startsWith("□ ")) {
                    String apiPath = line.substring(2).trim();
                    currentPath = apiPath;
                    System.out.println("Début de la requête : " + currentPath);

                    // Read the lines containing the request details (URL, status, etc.)
                    String requestLine1 = reader.readLine(); // Line containing "└"
                    System.out.println("Ligne de requête 1 : " + requestLine1);

                    String requestLine2 = reader.readLine(); // Line containing request details
                    System.out.println("Ligne de requête 2 : " + requestLine2);

                    // Initialize statusCode and responseTime to 0
                    statusCode = 0;
                    responseTime = 0;

                    // Extract request details using regex
                    if (requestLine2 != null && requestLine2.contains(" ")) {
                        // Regex to extract method and URL (even if the line contains [errored])
                        Pattern pattern = Pattern.compile("^\\s*(GET|POST|PUT|DELETE)\\s+(https?://[^\\s]+)");
                        Matcher matcher = pattern.matcher(requestLine2);

                        if (matcher.find()) {
                            httpMethod = matcher.group(1);
                            String fullUrl = matcher.group(2);

                            try {
                                // Extract the path from the full URL
                                URI uri = new URI(fullUrl);
                                currentPath = uri.getPath(); // This will give the path part of the URL
                                System.out.println("Chemin extrait : " + currentPath);

                                // If the request is errored, set statusCode and responseTime to 0
                                if (requestLine2.contains("[errored]")) {
                                    statusCode = 0;
                                    responseTime = 0;
                                    errorMessageBuilder.append("Request Errored: ").append(requestLine2).append("\n");
                                } else {
                                    // Extract status code and response time if available
                                    Pattern statusPattern = Pattern.compile("\\[(\\d{3})\\s+[^,]+,\\s+([^,]+),\\s+([0-9]+)ms]");
                                    Matcher statusMatcher = statusPattern.matcher(requestLine2);
                                    if (statusMatcher.find()) {
                                        statusCode = Integer.parseInt(statusMatcher.group(1));
                                        responseTime = Long.parseLong(statusMatcher.group(3));
                                    }
                                }

                            } catch (Exception e) {
                                System.err.println("Erreur lors de l'extraction du path depuis l'URL : " + fullUrl);
                                currentPath = "Unknown Path";
                                errorMessageBuilder.append("Erreur lors de l'extraction des détails de la requête").append("\n");
                            }

                            requestStarted = true;

                            // Process the test result
                            processTestResult(currentPath, httpMethod, success, statusCode, responseTime, errorMessageBuilder.toString());
                            errorMessageBuilder.setLength(0); // Clear the error message builder
                            requestStarted = false; // mark the request complete

                        } else {
                            // URL extraction failed
                            currentPath = "Unknown Path";
                            errorMessageBuilder.append("Impossible d'extraire les informations de la requête").append("\n");
                            System.err.println("Erreur : Impossible d'extraire les informations de la requête.");
                        }
                    } else {
                        errorMessageBuilder.append("Ligne de requête 2 est vide ou mal formée").append("\n");
                        currentPath = "Unknown Path"; // mark request as UNKNOWN PATH
                        System.err.println("Erreur : Ligne de requête 2 est vide ou mal formée.");
                    }

                    // Process test result if there was an issue with parsing
                    if (currentPath.equals("Unknown Path")) {
                        processTestResult(currentPath, httpMethod, success, statusCode, responseTime, errorMessageBuilder.toString());
                        errorMessageBuilder.setLength(0); // Clear the error message builder
                        requestStarted = false;
                    }

                }
                // Détecter une ligne "failure"
                if (line.toLowerCase().contains("failure") && currentPath != null && !"Unknown Path".equals(currentPath)) {
                    failureDetected = true;
                }
                // Capturer chaque message d'erreur après une ligne "failure"
                if (failureDetected && (line.trim().startsWith("AssertionError") || line.trim().startsWith("Error") || line.trim().startsWith("JSON Error"))) {
                    errorMessageBuilder.append(line.trim()).append("\n");
                }
                // Réinitialiser après une ligne vide (fin du bloc d'erreur)
                if (failureDetected && line.trim().isEmpty()) {
                    failureDetected = false;
                    String errorMessage = errorMessageBuilder.toString().trim();
                    if (!errorMessage.isEmpty()) {
                        if (currentPath != null && !"Unknown Path".equals(currentPath)) {
                            // Enregistrer l'erreur pour le chemin actuel
                            processTestResult(currentPath, httpMethod, success, statusCode, responseTime, errorMessage);
                        } else {
                            // Enregistrer l'erreur pour tous les chemins
                            List<ApiMonitoring> allPaths = resultRepository.findAll();
                            for (ApiMonitoring pathEntry : allPaths) {
                                // Extraire la méthode HTTP de la colonne metadata
                                String method = extractMethodFromMetadata(pathEntry.getMetadata());
                                processTestResult(pathEntry.getPath(), method, success, statusCode, responseTime, errorMessage);
                            }
                        }
                    }
                    errorMessageBuilder.setLength(0); // Clear the error message builder
                }
            }

            int exitCode = process.waitFor();
            System.out.println(exitCode == 0 ? "Tous les tests ont été exécutés avec succès !" : "Certains tests ont échoué. Code de sortie : " + exitCode);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Une erreur s'est produite lors de l'exécution des tests Newman: " + e.getMessage());
        }
        System.out.println("Fin de l'exécution des tests Newman.");
    }

    @Transactional
    protected void processTestResult(String path, String method, boolean success, int statusCode, long responseTime, String errorMessage) {
        // Do not save if path is unknown path
        if ("Unknown Path".equals(path)) {
            System.out.println("Ignorer l'enregistrement pour Unknown Path");
            return;
        }

        try {
            String level = (statusCode == 500 || (errorMessage != null && !errorMessage.isEmpty())) ? "ERROR" : "INFO";

            // Créez un objet JSON pour stocker le path et la méthode
            ObjectNode metadata = objectMapper.createObjectNode();
            metadata.put("path", path);
            metadata.put("method", method);
            String metadataString = metadata.toString();

            Optional<ApiMonitoring> existingResult = resultRepository.findByMetadata(metadataString);

            ApiMonitoring result;
            if (existingResult.isPresent()) {
                // Mettre à jour l'entrée existante
                result = existingResult.get();
                result.setTemps(LocalDateTime.now());
                result.setResponseTime(responseTime);
                result.setStatusCode(statusCode);
                result.setErrorMessage(errorMessage);
                result.setLevel(level);
                resultRepository.save(result); // Save the updated result
                System.out.println("Mise à jour réussie pour : " + path);

            } else {
                // Extract request details using regex
                result = new ApiMonitoring();
                result.setPath(path);
                result.setTemps(LocalDateTime.now());
                result.setResponseTime(responseTime);
                result.setStatusCode(statusCode);
                result.setErrorMessage(errorMessage);
                result.setLevel(level);
                result.setMetadata(metadataString); // Stocker le JSON dans la colonne metadata
                resultRepository.save(result); // Save the new result
                System.out.println("Enregistrement réussi pour : " + path);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur lors de l'enregistrement pour : " + path + ": " + e.getMessage());
        }
    }

    /**
     * Méthode pour extraire la méthode HTTP de la colonne metadata.
     *
     * @param metadata La chaîne JSON contenant les métadonnées.
     * @return La méthode HTTP extraite, ou "UNKNOWN" si non trouvée.
     */
    /*private String extractMethodFromMetadata(String metadata) {
        try {
            // Désérialiser le JSON pour extraire la méthode HTTP
            ObjectNode metadataNode = (ObjectNode) objectMapper.readTree(metadata);
            return metadataNode.has("method") ? metadataNode.get("method").asText() : "UNKNOWN";
        } catch (Exception e) {
            System.err.println("Erreur lors de l'extraction de la méthode HTTP depuis les métadonnées : " + e.getMessage());
            return "UNKNOWN";
        }
    }
}*/
/*@Service
public class NewmanService {

    private final ApiMonitoringRepository resultRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NewmanService(ApiMonitoringRepository resultRepository) {
        this.resultRepository = resultRepository;
        System.out.println("NewmanService créé avec le repository : " + resultRepository);
    }

    @Scheduled(cron = "0 * * * * *") // Exécution toutes les minutes
    public void runNewmanTests() {
        System.out.println("Début de l'exécution des tests Newman.");

        try {
            // Récupérer le chemin absolu du fichier postman_collection.json dans le dossier resources
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("postman_collection.json").toURI());

            System.out.println("Chemin du fichier de collection Postman : " + file.getAbsolutePath());

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "C:\\Users\\LENOVO\\AppData\\Roaming\\npm\\newman.cmd",
                    "run", file.getAbsolutePath() // Utilisation du chemin absolu du fichier
            );

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            String currentPath = null;
            String httpMethod = null;
            boolean success = false;
            int statusCode = 0;
            long responseTime = 0;
            StringBuilder errorMessageBuilder = new StringBuilder();
            boolean requestStarted = false;
            boolean inErrorBlock = false; // Track if we're inside an error block

            while ((line = reader.readLine()) != null) {
                System.out.println("Ligne lue : " + line);

                // Check if a new request is starting based on the "□" character
                if (line.startsWith("□ ")) {
                    // If we were in a previous error block, save the error message
                    if (inErrorBlock && currentPath != null) {
                        processTestResult(currentPath, httpMethod, success, statusCode, responseTime, errorMessageBuilder.toString());
                    }

                    String apiPath = line.substring(2).trim();
                    currentPath = apiPath;
                    System.out.println("Début de la requête : " + currentPath);
                    errorMessageBuilder.setLength(0); // Clear error message for new request
                    inErrorBlock = false; // Reset error block status

                    // Read the lines containing the request details (URL, status, etc.)
                    String requestLine1 = reader.readLine(); // Line containing "└"
                    System.out.println("Ligne de requête 1 : " + requestLine1);

                    String requestLine2 = reader.readLine(); // Line containing request details
                    System.out.println("Ligne de requête 2 : " + requestLine2);

                    // Initialize statusCode and responseTime to 0
                    statusCode = 0;
                    responseTime = 0;

                    // Extract request details using regex
                    if (requestLine2 != null && requestLine2.contains(" ")) {
                        // Regex to extract method and URL (even if the line contains [errored])
                        Pattern pattern = Pattern.compile("^\\s*(GET|POST|PUT|DELETE)\\s+(https?://[^\\s]+)");
                        Matcher matcher = pattern.matcher(requestLine2);

                        if (matcher.find()) {
                            httpMethod = matcher.group(1);
                            String fullUrl = matcher.group(2);

                            try {
                                // Extract the path from the full URL
                                URI uri = new URI(fullUrl);
                                currentPath = uri.getPath(); // This will give the path part of the URL
                                System.out.println("Chemin extrait : " + currentPath);

                                // If the request is errored, set statusCode and responseTime to 0
                                if (requestLine2.contains("[errored]")) {
                                    statusCode = 0;
                                    responseTime = 0;
                                    errorMessageBuilder.append("Request Errored: ").append(requestLine2);
                                    inErrorBlock = true;
                                } else {
                                    // Extract status code and response time if available
                                    Pattern statusPattern = Pattern.compile("\\[(\\d{3})\\s+[^,]+,\\s+([^,]+),\\s+([0-9]+)ms]");
                                    Matcher statusMatcher = statusPattern.matcher(requestLine2);
                                    if (statusMatcher.find()) {
                                        statusCode = Integer.parseInt(statusMatcher.group(1));
                                        responseTime = Long.parseLong(statusMatcher.group(3));
                                    }
                                }

                            } catch (Exception e) {
                                System.err.println("Erreur lors de l'extraction du path depuis l'URL : " + fullUrl);
                                currentPath = "Unknown Path";
                                errorMessageBuilder.append("Erreur lors de l'extraction des détails de la requête: ").append(e.getMessage());
                                inErrorBlock = true;
                            }

                            requestStarted = true;

                        } else {
                            // URL extraction failed
                            currentPath = "Unknown Path";
                            errorMessageBuilder.append("Impossible d'extraire les informations de la requête.");
                            inErrorBlock = true;
                            System.err.println("Erreur : Impossible d'extraire les informations de la requête.");
                        }
                    } else {
                        errorMessageBuilder.append("Ligne de requête 2 est vide ou mal formée");
                        currentPath = "Unknown Path"; // mark request as UNKNOWN PATH
                        inErrorBlock = true;
                        System.err.println("Erreur : Ligne de requête 2 est vide ou mal formée.");
                    }

                    // Process test result if there was an issue with parsing
                    if (currentPath.equals("Unknown Path")) {
                        processTestResult(currentPath, httpMethod, success, statusCode, responseTime, errorMessageBuilder.toString());
                        requestStarted = false;
                        inErrorBlock = false;
                    }

                } else if (line.toLowerCase().contains("error") && currentPath != null && !"Unknown Path".equals(currentPath)) {
                    errorMessageBuilder.append(line).append("\n");
                    inErrorBlock = true;

                } else if (inErrorBlock && currentPath != null && !"Unknown Path".equals(currentPath)) {
                    // Append to the errorMessage while inside an error block
                    errorMessageBuilder.append(line).append("\n");
                }
            }

            // Process the last request if there were errors
            if (inErrorBlock && currentPath != null && !"Unknown Path".equals(currentPath)) {
                processTestResult(currentPath, httpMethod, success, statusCode, responseTime, errorMessageBuilder.toString());
            }

            int exitCode = process.waitFor();
            System.out.println(exitCode == 0 ? "Tous les tests ont été exécutés avec succès !" : "Certains tests ont échoué. Code de sortie : " + exitCode);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Une erreur s'est produite lors de l'exécution des tests Newman: " + e.getMessage());
        }
        System.out.println("Fin de l'exécution des tests Newman.");
    }

    @Transactional
    protected void processTestResult(String path, String method, boolean success, int statusCode, long responseTime, String errorMessage) {
        // Do not save if path is unknown path
        if ("Unknown Path".equals(path)) {
            System.out.println("Ignorer l'enregistrement pour Unknown Path");
            return;
        }

        try {
            String level = (statusCode == 500 || (errorMessage != null && !errorMessage.isEmpty())) ? "ERROR" : "INFO";

            // Créez un objet JSON pour stocker le path et la méthode
            ObjectNode metadata = objectMapper.createObjectNode();
            metadata.put("path", path);
            metadata.put("method", method);
            String metadataString = metadata.toString();

            Optional<ApiMonitoring> existingResult = resultRepository.findByMetadata(metadataString);

            ApiMonitoring result;
            if (existingResult.isPresent()) {
                // Mettre à jour l'entrée existante
                result = existingResult.get();
                result.setTemps(LocalDateTime.now());
                result.setResponseTime(responseTime);
                result.setStatusCode(statusCode);
                result.setErrorMessage(errorMessage);
                result.setLevel(level);
                resultRepository.save(result); // Save the updated result
                System.out.println("Mise à jour réussie pour : " + path);

            } else {
                // Extract request details using regex
                result = new ApiMonitoring();
                result.setPath(path);
                result.setTemps(LocalDateTime.now());
                result.setResponseTime(responseTime);
                result.setStatusCode(statusCode);
                result.setErrorMessage(errorMessage);
                result.setLevel(level);
                result.setMetadata(metadataString); // Stocker le JSON dans la colonne metadata
                resultRepository.save(result); // Save the new result
                System.out.println("Enregistrement réussi pour : " + path);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur lors de l'enregistrement pour : " + path + ": " + e.getMessage());
        }
    }
}*/
/*hedha ystoki msg erreur fi case wahda*/
/*@Service
public class NewmanService {

    private final ApiMonitoringRepository resultRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NewmanService(ApiMonitoringRepository resultRepository) {
        this.resultRepository = resultRepository;
        System.out.println("NewmanService créé avec le repository : " + resultRepository);
    }

    @Scheduled(cron = "0 * * * * *") // Exécution toutes les minutes
    public void runNewmanTests() {
        System.out.println("Début de l'exécution des tests Newman.");

        try {
            // Récupérer le chemin absolu du fichier postman_collection.json dans le dossier resources
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("postman_collection.json").toURI());

            System.out.println("Chemin du fichier de collection Postman : " + file.getAbsolutePath());

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "C:\\Users\\LENOVO\\AppData\\Roaming\\npm\\newman.cmd",
                    "run", file.getAbsolutePath() // Utilisation du chemin absolu du fichier
            );

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            String currentPath = null;
            String httpMethod = null;
            boolean success = false;
            int statusCode = 0;
            long responseTime = 0;
            StringBuilder errorMessageBuilder = new StringBuilder();
            boolean requestStarted = false;
            boolean inErrorBlock = false; // Track if we're inside an error block

            while ((line = reader.readLine()) != null) {
                System.out.println("Ligne lue : " + line);

                // Check if a new request is starting based on the "□" character
                if (line.startsWith("□ ")) {
                    // If we were in a previous error block, save the error message
                    if (inErrorBlock && currentPath != null) {
                        processTestResult(currentPath, httpMethod, success, statusCode, responseTime, errorMessageBuilder.toString());
                    }

                    String apiPath = line.substring(2).trim();
                    currentPath = apiPath;
                    System.out.println("Début de la requête : " + currentPath);
                    errorMessageBuilder.setLength(0); // Clear error message for new request
                    inErrorBlock = false; // Reset error block status

                    // Read the lines containing the request details (URL, status, etc.)
                    String requestLine1 = reader.readLine(); // Line containing "└"
                    System.out.println("Ligne de requête 1 : " + requestLine1);

                    String requestLine2 = reader.readLine(); // Line containing request details
                    System.out.println("Ligne de requête 2 : " + requestLine2);

                    // Initialize statusCode and responseTime to 0
                    statusCode = 0;
                    responseTime = 0;

                    // Extract request details using regex
                    if (requestLine2 != null && requestLine2.contains(" ")) {
                        // Regex to extract method and URL (even if the line contains [errored])
                        Pattern pattern = Pattern.compile("^\\s*(GET|POST|PUT|DELETE)\\s+(https?://[^\\s]+)");
                        Matcher matcher = pattern.matcher(requestLine2);

                        if (matcher.find()) {
                            httpMethod = matcher.group(1);
                            String fullUrl = matcher.group(2);

                            try {
                                // Extract the path from the full URL
                                URI uri = new URI(fullUrl);
                                currentPath = uri.getPath(); // This will give the path part of the URL
                                System.out.println("Chemin extrait : " + currentPath);

                                // If the request is errored, set statusCode and responseTime to 0
                                if (requestLine2.contains("[errored]")) {
                                    statusCode = 0;
                                    responseTime = 0;
                                    errorMessageBuilder.append("Request Errored: ").append(requestLine2);
                                    inErrorBlock = true;
                                } else {
                                    // Extract status code and response time if available
                                    Pattern statusPattern = Pattern.compile("\\[(\\d{3})\\s+[^,]+,\\s+([^,]+),\\s+([0-9]+)ms]");
                                    Matcher statusMatcher = statusPattern.matcher(requestLine2);
                                    if (statusMatcher.find()) {
                                        statusCode = Integer.parseInt(statusMatcher.group(1));
                                        responseTime = Long.parseLong(statusMatcher.group(3));
                                    }
                                }

                            } catch (Exception e) {
                                System.err.println("Erreur lors de l'extraction du path depuis l'URL : " + fullUrl);
                                currentPath = "Unknown Path";
                                errorMessageBuilder.append("Erreur lors de l'extraction des détails de la requête: ").append(e.getMessage());
                                inErrorBlock = true;
                            }

                            requestStarted = true;

                        } else {
                            // URL extraction failed
                            currentPath = "Unknown Path";
                            errorMessageBuilder.append("Impossible d'extraire les informations de la requête.");
                            inErrorBlock = true;
                            System.err.println("Erreur : Impossible d'extraire les informations de la requête.");
                        }
                    } else {
                        errorMessageBuilder.append("Ligne de requête 2 est vide ou mal formée");
                        currentPath = "Unknown Path"; // mark request as UNKNOWN PATH
                        inErrorBlock = true;
                        System.err.println("Erreur : Ligne de requête 2 est vide ou mal formée.");
                    }

                    // Process test result if there was an issue with parsing
                    if (currentPath.equals("Unknown Path")) {
                        processTestResult(currentPath, httpMethod, success, statusCode, responseTime, errorMessageBuilder.toString());
                        requestStarted = false;
                        inErrorBlock = false;
                    }

                } else if (line.toLowerCase().contains("failure") && currentPath != null && !"Unknown Path".equals(currentPath)) {
                    // Capture the error message for the current path
                    errorMessageBuilder.append(line).append("\n");
                    inErrorBlock = true;

                } else if (inErrorBlock && currentPath != null && !"Unknown Path".equals(currentPath)) {
                    // Append to the errorMessage while inside an error block
                    errorMessageBuilder.append(line).append("\n");
                }
            }

            // Process the last request if there were errors
            if (inErrorBlock && currentPath != null && !"Unknown Path".equals(currentPath)) {
                processTestResult(currentPath, httpMethod, success, statusCode, responseTime, errorMessageBuilder.toString());
            }

            int exitCode = process.waitFor();
            System.out.println(exitCode == 0 ? "Tous les tests ont été exécutés avec succès !" : "Certains tests ont échoué. Code de sortie : " + exitCode);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Une erreur s'est produite lors de l'exécution des tests Newman: " + e.getMessage());
        }
        System.out.println("Fin de l'exécution des tests Newman.");
    }

    @Transactional
    protected void processTestResult(String path, String method, boolean success, int statusCode, long responseTime, String errorMessage) {
        // Do not save if path is unknown path
        if ("Unknown Path".equals(path)) {
            System.out.println("Ignorer l'enregistrement pour Unknown Path");
            return;
        }

        try {
            String level = (statusCode == 500 || (errorMessage != null && !errorMessage.isEmpty())) ? "ERROR" : "INFO";

            // Créez un objet JSON pour stocker le path et la méthode
            ObjectNode metadata = objectMapper.createObjectNode();
            metadata.put("path", path);
            metadata.put("method", method);
            String metadataString = metadata.toString();

            Optional<ApiMonitoring> existingResult = resultRepository.findByMetadata(metadataString);

            ApiMonitoring result;
            if (existingResult.isPresent()) {
                // Mettre à jour l'entrée existante
                result = existingResult.get();
                result.setTemps(LocalDateTime.now());
                result.setResponseTime(responseTime);
                result.setStatusCode(statusCode);
                result.setErrorMessage(errorMessage);
                result.setLevel(level);
                resultRepository.save(result); // Save the updated result
                System.out.println("Mise à jour réussie pour : " + path);

            } else {
                // Extract request details using regex
                result = new ApiMonitoring();
                result.setPath(path);
                result.setTemps(LocalDateTime.now());
                result.setResponseTime(responseTime);
                result.setStatusCode(statusCode);
                result.setErrorMessage(errorMessage);
                result.setLevel(level);
                result.setMetadata(metadataString); // Stocker le JSON dans la colonne metadata
                resultRepository.save(result); // Save the new result
                System.out.println("Enregistrement réussi pour : " + path);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur lors de l'enregistrement pour : " + path + ": " + e.getMessage());
        }
    }
}*/
import java.util.Map;
import java.util.HashMap;
/* hedha mochkeltouuu yanseri api jdod mel partie msg */
/*@Service
public class NewmanService {

    private final ApiMonitoringRepository resultRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NewmanService(ApiMonitoringRepository resultRepository) {
        this.resultRepository = resultRepository;
        System.out.println("NewmanService créé avec le repository : " + resultRepository);
    }

    @Scheduled(cron = "0 * * * * *") // Exécution toutes les minutes
    public void runNewmanTests() {
        System.out.println("Début de l'exécution des tests Newman.");

        try {
            // Récupérer le chemin absolu du fichier postman_collection.json dans le dossier resources
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("postman_collection.json").toURI());

            System.out.println("Chemin du fichier de collection Postman : " + file.getAbsolutePath());

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "C:\\Users\\LENOVO\\AppData\\Roaming\\npm\\newman.cmd",
                    "run", file.getAbsolutePath() // Utilisation du chemin absolu du fichier
            );

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            String currentPath = null;
            String httpMethod = null;
            boolean success = false;
            int statusCode = 0;
            long responseTime = 0;
            StringBuilder errorMessageBuilder = new StringBuilder();
            boolean requestStarted = false;
            boolean inErrorBlock = false; // Track if we're inside an error block
            Map<String, String> errorMessagesByPath = new HashMap<>(); // Store errors by path

            while ((line = reader.readLine()) != null) {
                System.out.println("Ligne lue : " + line);

                // Check if a new request is starting based on the "□" character
                if (line.startsWith("□ ")) {
                    // If we were in a previous error block, save the error message
                    if (inErrorBlock && currentPath != null) {
                        processTestResult(currentPath, httpMethod, success, statusCode, responseTime, errorMessageBuilder.toString());
                    }

                    String apiPath = line.substring(2).trim();
                    currentPath = apiPath;
                    System.out.println("Début de la requête : " + currentPath);
                    errorMessageBuilder.setLength(0); // Clear error message for new request
                    inErrorBlock = false; // Reset error block status

                    // Read the lines containing the request details (URL, status, etc.)
                    String requestLine1 = reader.readLine(); // Line containing "└"
                    System.out.println("Ligne de requête 1 : " + requestLine1);

                    String requestLine2 = reader.readLine(); // Line containing request details
                    System.out.println("Ligne de requête 2 : " + requestLine2);

                    // Initialize statusCode and responseTime to 0
                    statusCode = 0;
                    responseTime = 0;

                    // Extract request details using regex
                    if (requestLine2 != null && requestLine2.contains(" ")) {
                        // Regex to extract method and URL (even if the line contains [errored])
                        Pattern pattern = Pattern.compile("^\\s*(GET|POST|PUT|DELETE)\\s+(https?://[^\\s]+)");
                        Matcher matcher = pattern.matcher(requestLine2);

                        if (matcher.find()) {
                            httpMethod = matcher.group(1);
                            String fullUrl = matcher.group(2);

                            try {
                                // Extract the path from the full URL
                                URI uri = new URI(fullUrl);
                                currentPath = uri.getPath(); // This will give the path part of the URL
                                System.out.println("Chemin extrait : " + currentPath);

                                // If the request is errored, set statusCode and responseTime to 0
                                if (requestLine2.contains("[errored]")) {
                                    statusCode = 0;
                                    responseTime = 0;
                                    errorMessageBuilder.append("Request Errored: ").append(requestLine2);
                                    inErrorBlock = true;
                                } else {
                                    // Extract status code and response time if available
                                    Pattern statusPattern = Pattern.compile("\\[(\\d{3})\\s+[^,]+,\\s+([^,]+),\\s+([0-9]+)ms]");
                                    Matcher statusMatcher = statusPattern.matcher(requestLine2);
                                    if (statusMatcher.find()) {
                                        statusCode = Integer.parseInt(statusMatcher.group(1));
                                        responseTime = Long.parseLong(statusMatcher.group(3));
                                    }
                                }

                            } catch (Exception e) {
                                System.err.println("Erreur lors de l'extraction du path depuis l'URL : " + fullUrl);
                                currentPath = "Unknown Path";
                                errorMessageBuilder.append("Erreur lors de l'extraction des détails de la requête: ").append(e.getMessage());
                                inErrorBlock = true;
                            }

                            requestStarted = true;

                        } else {
                            // URL extraction failed
                            currentPath = "Unknown Path";
                            errorMessageBuilder.append("Impossible d'extraire les informations de la requête.");
                            inErrorBlock = true;
                            System.err.println("Erreur : Impossible d'extraire les informations de la requête.");
                        }
                    } else {
                        errorMessageBuilder.append("Ligne de requête 2 est vide ou mal formée");
                        currentPath = "Unknown Path"; // mark request as UNKNOWN PATH
                        inErrorBlock = true;
                        System.err.println("Erreur : Ligne de requête 2 est vide ou mal formée.");
                    }

                    // Process test result if there was an issue with parsing
                    if (currentPath.equals("Unknown Path")) {
                        processTestResult(currentPath, httpMethod, success, statusCode, responseTime, errorMessageBuilder.toString());
                        requestStarted = false;
                        inErrorBlock = false;
                    }

                } else if (line.matches("^\\s*\\d+\\.\\s.*")) {
                    // Detect independent error messages (e.g., "1. JSONError ...")
                    String errorMessage = line.replaceFirst("^\\s*\\d+\\.\\s*", ""); // Remove the number and dot
                    errorMessagesByPath.put(currentPath, errorMessage); // Associate with current path
                    inErrorBlock = true;

                } else if (line.contains("inside")) {
                    // Extract the path from the error message (e.g., "inside /api/accounts/{id}")
                    Pattern pathPattern = Pattern.compile("inside \"([^\"]+)\"");
                    Matcher pathMatcher = pathPattern.matcher(line);
                    if (pathMatcher.find()) {
                        String errorPath = pathMatcher.group(1);
                        errorMessagesByPath.put(errorPath, errorMessagesByPath.getOrDefault(currentPath, ""));
                    }
                }
            }

            // Process the last request if there were errors
            if (inErrorBlock && currentPath != null && !"Unknown Path".equals(currentPath)) {
                processTestResult(currentPath, httpMethod, success, statusCode, responseTime, errorMessageBuilder.toString());
            }

            // Save all errors by path
            for (Map.Entry<String, String> entry : errorMessagesByPath.entrySet()) {
                processTestResult(entry.getKey(), httpMethod, success, statusCode, responseTime, entry.getValue());
            }

            int exitCode = process.waitFor();
            System.out.println(exitCode == 0 ? "Tous les tests ont été exécutés avec succès !" : "Certains tests ont échoué. Code de sortie : " + exitCode);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Une erreur s'est produite lors de l'exécution des tests Newman: " + e.getMessage());
        }
        System.out.println("Fin de l'exécution des tests Newman.");
    }

    @Transactional
    protected void processTestResult(String path, String method, boolean success, int statusCode, long responseTime, String errorMessage) {
        // Do not save if path is unknown path
        if ("Unknown Path".equals(path)) {
            System.out.println("Ignorer l'enregistrement pour Unknown Path");
            return;
        }

        try {
            String level = (statusCode == 500 || (errorMessage != null && !errorMessage.isEmpty())) ? "ERROR" : "INFO";

            // Créez un objet JSON pour stocker le path et la méthode
            ObjectNode metadata = objectMapper.createObjectNode();
            metadata.put("path", path);
            metadata.put("method", method);
            String metadataString = metadata.toString();

            Optional<ApiMonitoring> existingResult = resultRepository.findByMetadata(metadataString);

            ApiMonitoring result;
            if (existingResult.isPresent()) {
                // Mettre à jour l'entrée existante
                result = existingResult.get();
                result.setTemps(LocalDateTime.now());
                result.setResponseTime(responseTime);
                result.setStatusCode(statusCode);
                result.setErrorMessage(errorMessage);
                result.setLevel(level);
                resultRepository.save(result); // Save the updated result
                System.out.println("Mise à jour réussie pour : " + path);

            } else {
                // Extract request details using regex
                result = new ApiMonitoring();
                result.setPath(path);
                result.setTemps(LocalDateTime.now());
                result.setResponseTime(responseTime);
                result.setStatusCode(statusCode);
                result.setErrorMessage(errorMessage);
                result.setLevel(level);
                result.setMetadata(metadataString); // Stocker le JSON dans la colonne metadata
                resultRepository.save(result); // Save the new result
                System.out.println("Enregistrement réussi pour : " + path);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur lors de l'enregistrement pour : " + path + ": " + e.getMessage());
        }
    }
}*/
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.time.LocalDateTime;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*@Service
public class NewmanService {

    private final ApiMonitoringRepository resultRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NewmanService(ApiMonitoringRepository resultRepository) {
        this.resultRepository = resultRepository;
        System.out.println("NewmanService créé avec le repository : " + resultRepository);
    }

    @Scheduled(cron = "0 * * * * *") // Exécution toutes les minutes
    public void runNewmanTests() {
        System.out.println("Début de l'exécution des tests Newman.");

        try {
            // Récupérer le chemin absolu du fichier postman_collection.json dans le dossier resources
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("postman_collection.json").toURI());

            System.out.println("Chemin du fichier de collection Postman : " + file.getAbsolutePath());

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "C:\\Users\\LENOVO\\AppData\\Roaming\\npm\\newman.cmd",
                    "run", file.getAbsolutePath() // Utilisation du chemin absolu du fichier
            );

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            String currentPath = null;
            String httpMethod = null;
            boolean success = false;
            int statusCode = 0;
            long responseTime = 0;
            StringBuilder errorMessageBuilder = new StringBuilder();
            boolean requestStarted = false;
            boolean inErrorBlock = false; // Track if we're inside an error block
            Map<String, String> errorMessagesByPath = new HashMap<>(); // Store errors by path

            while ((line = reader.readLine()) != null) {
                System.out.println("Ligne lue : " + line);

                // Check if a new request is starting based on the "□" character
                if (line.startsWith("□ ")) {
                    // If we were in a previous error block, save the error message
                    if (inErrorBlock && currentPath != null) {
                        processTestResult(currentPath, httpMethod, success, statusCode, responseTime, errorMessageBuilder.toString());
                    }

                    String apiPath = line.substring(2).trim();
                    currentPath = apiPath;
                    System.out.println("Début de la requête : " + currentPath);
                    errorMessageBuilder.setLength(0); // Clear error message for new request
                    inErrorBlock = false; // Reset error block status

                    // Read the lines containing the request details (URL, status, etc.)
                    String requestLine1 = reader.readLine(); // Line containing "└"
                    System.out.println("Ligne de requête 1 : " + requestLine1);

                    String requestLine2 = reader.readLine(); // Line containing request details
                    System.out.println("Ligne de requête 2 : " + requestLine2);

                    // Initialize statusCode and responseTime to 0
                    statusCode = 0;
                    responseTime = 0;

                    // Extract request details using regex
                    if (requestLine2 != null && requestLine2.contains(" ")) {
                        // Regex to extract method and URL (even if the line contains [errored])
                        Pattern pattern = Pattern.compile("^\\s*(GET|POST|PUT|DELETE)\\s+(https?://[^\\s]+)");
                        Matcher matcher = pattern.matcher(requestLine2);

                        if (matcher.find()) {
                            httpMethod = matcher.group(1);
                            String fullUrl = matcher.group(2);

                            try {
                                // Extract the path from the full URL
                                URI uri = new URI(fullUrl);
                                currentPath = uri.getPath(); // This will give the path part of the URL
                                System.out.println("Chemin extrait : " + currentPath);

                                // If the request is errored, set statusCode and responseTime to 0
                                if (requestLine2.contains("[errored]")) {
                                    statusCode = 0;
                                    responseTime = 0;
                                    errorMessageBuilder.append("Request Errored: ").append(requestLine2);
                                    inErrorBlock = true;
                                } else {
                                    // Extract status code and response time if available
                                    Pattern statusPattern = Pattern.compile("\\[(\\d{3})\\s+[^,]+,\\s+([^,]+),\\s+([0-9]+)ms]");
                                    Matcher statusMatcher = statusPattern.matcher(requestLine2);
                                    if (statusMatcher.find()) {
                                        statusCode = Integer.parseInt(statusMatcher.group(1));
                                        responseTime = Long.parseLong(statusMatcher.group(3));
                                    }
                                }

                            } catch (Exception e) {
                                System.err.println("Erreur lors de l'extraction du path depuis l'URL : " + fullUrl);
                                currentPath = "Unknown Path";
                                errorMessageBuilder.append("Erreur lors de l'extraction des détails de la requête: ").append(e.getMessage());
                                inErrorBlock = true;
                            }

                            requestStarted = true;

                        } else {
                            // URL extraction failed
                            currentPath = "Unknown Path";
                            errorMessageBuilder.append("Impossible d'extraire les informations de la requête.");
                            inErrorBlock = true;
                            System.err.println("Erreur : Impossible d'extraire les informations de la requête.");
                        }
                    } else {
                        errorMessageBuilder.append("Ligne de requête 2 est vide ou mal formée");
                        currentPath = "Unknown Path"; // mark request as UNKNOWN PATH
                        inErrorBlock = true;
                        System.err.println("Erreur : Ligne de requête 2 est vide ou mal formée.");
                    }

                    // Process test result if there was an issue with parsing
                    if (currentPath.equals("Unknown Path")) {
                        processTestResult(currentPath, httpMethod, success, statusCode, responseTime, errorMessageBuilder.toString());
                        requestStarted = false;
                        inErrorBlock = false;
                    }

                } else if (line.matches("^\\s*\\d+\\.\\s.*")) {
                    // Detect independent error messages (e.g., "1. JSONError ...")
                    String errorMessage = line.replaceFirst("^\\s*\\d+\\.\\s*", ""); // Remove the number and dot
                    errorMessagesByPath.put(currentPath, errorMessage); // Associate with current path
                    inErrorBlock = true;

                } else if (line.contains("inside")) {
                    // Extract the path from the error message (e.g., "inside /api/accounts/{id} / Supprimer un compte bancaire")
                    Pattern pathPattern = Pattern.compile("inside \"([^\\s/]+)"); // Extract only the API path
                    Matcher pathMatcher = pathPattern.matcher(line);
                    if (pathMatcher.find()) {
                        String errorPath = pathMatcher.group(1); // Get the API path without description
                        errorMessagesByPath.put(errorPath, errorMessagesByPath.getOrDefault(currentPath, ""));
                    }
                }
            }

            // Process the last request if there were errors
            if (inErrorBlock && currentPath != null && !"Unknown Path".equals(currentPath)) {
                processTestResult(currentPath, httpMethod, success, statusCode, responseTime, errorMessageBuilder.toString());
            }

            // Save all errors by path
            for (Map.Entry<String, String> entry : errorMessagesByPath.entrySet()) {
                processTestResult(entry.getKey(), httpMethod, success, statusCode, responseTime, entry.getValue());
            }

            int exitCode = process.waitFor();
            System.out.println(exitCode == 0 ? "Tous les tests ont été exécutés avec succès !" : "Certains tests ont échoué. Code de sortie : " + exitCode);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Une erreur s'est produite lors de l'exécution des tests Newman: " + e.getMessage());
        }
        System.out.println("Fin de l'exécution des tests Newman.");
    }

    @Transactional
    protected void processTestResult(String path, String method, boolean success, int statusCode, long responseTime, String errorMessage) {
        // Do not save if path is unknown path
        if ("Unknown Path".equals(path)) {
            System.out.println("Ignorer l'enregistrement pour Unknown Path");
            return;
        }

        try {
            String level = (statusCode == 500 || (errorMessage != null && !errorMessage.isEmpty())) ? "ERROR" : "INFO";

            // Créez un objet JSON pour stocker le path et la méthode
            ObjectNode metadata = objectMapper.createObjectNode();
            metadata.put("path", path);
            metadata.put("method", method);
            String metadataString = metadata.toString();

            Optional<ApiMonitoring> existingResult = resultRepository.findByMetadata(metadataString);

            ApiMonitoring result;
            if (existingResult.isPresent()) {
                // Mettre à jour l'entrée existante
                result = existingResult.get();
                result.setTemps(LocalDateTime.now());
                result.setResponseTime(responseTime);
                result.setStatusCode(statusCode);
                result.setErrorMessage(errorMessage);
                result.setLevel(level);
                resultRepository.save(result); // Save the updated result
                System.out.println("Mise à jour réussie pour : " + path);

            } else {
                // Extract request details using regex
                result = new ApiMonitoring();
                result.setPath(path);
                result.setTemps(LocalDateTime.now());
                result.setResponseTime(responseTime);
                result.setStatusCode(statusCode);
                result.setErrorMessage(errorMessage);
                result.setLevel(level);
                result.setMetadata(metadataString); // Stocker le JSON dans la colonne metadata
                resultRepository.save(result); // Save the new result
                System.out.println("Enregistrement réussi pour : " + path);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur lors de l'enregistrement pour : " + path + ": " + e.getMessage());
        }
    }
}*/
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.time.LocalDateTime;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*@Service
public class NewmanService {

    private final ApiMonitoringRepository resultRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NewmanService(ApiMonitoringRepository resultRepository) {
        this.resultRepository = resultRepository;
        System.out.println("NewmanService créé avec le repository : " + resultRepository);
    }

    @Scheduled(cron = "0 * * * * *") // Exécution toutes les minutes
    public void runNewmanTests() {
        System.out.println("Début de l'exécution des tests Newman.");

        try {
            // Récupérer le chemin absolu du fichier postman_collection.json dans le dossier resources
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("postman_collection.json").toURI());

            System.out.println("Chemin du fichier de collection Postman : " + file.getAbsolutePath());

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "C:\\Users\\LENOVO\\AppData\\Roaming\\npm\\newman.cmd",
                    "run", file.getAbsolutePath() // Utilisation du chemin absolu du fichier
            );

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            String currentPath = null;
            String httpMethod = null;
            boolean success = false;
            int statusCode = 0;
            long responseTime = 0;
            StringBuilder errorMessageBuilder = new StringBuilder();
            boolean requestStarted = false;
            boolean inErrorBlock = false; // Track if we're inside an error block

            while ((line = reader.readLine()) != null) {
                System.out.println("Ligne lue : " + line);

                // Check if a new request is starting based on the "□" character
                if (line.startsWith("□ ")) {
                    // If we were in a previous error block, save the error message
                    if (inErrorBlock && currentPath != null) {
                        processTestResult(currentPath, httpMethod, success, statusCode, responseTime, errorMessageBuilder.toString());
                    }

                    String apiPath = line.substring(2).trim();
                    currentPath = apiPath;
                    System.out.println("Début de la requête : " + currentPath);
                    errorMessageBuilder.setLength(0); // Clear error message for new request
                    inErrorBlock = false; // Reset error block status

                    // Read the lines containing the request details (URL, status, etc.)
                    String requestLine1 = reader.readLine(); // Line containing "└"
                    System.out.println("Ligne de requête 1 : " + requestLine1);

                    String requestLine2 = reader.readLine(); // Line containing request details
                    System.out.println("Ligne de requête 2 : " + requestLine2);

                    // Initialize statusCode and responseTime to 0
                    statusCode = 0;
                    responseTime = 0;

                    // Extract request details using regex
                    if (requestLine2 != null && requestLine2.contains(" ")) {
                        // Regex to extract method and URL (even if the line contains [errored])
                        Pattern pattern = Pattern.compile("^\\s*(GET|POST|PUT|DELETE)\\s+(https?://[^\\s]+)");
                        Matcher matcher = pattern.matcher(requestLine2);

                        if (matcher.find()) {
                            httpMethod = matcher.group(1);
                            String fullUrl = matcher.group(2);

                            try {
                                // Extract the path from the full URL
                                URI uri = new URI(fullUrl);
                                currentPath = uri.getPath(); // This will give the path part of the URL
                                System.out.println("Chemin extrait : " + currentPath);

                                // If the request is errored, set statusCode and responseTime to 0
                                if (requestLine2.contains("[errored]")) {
                                    statusCode = 0;
                                    responseTime = 0;
                                    errorMessageBuilder.append("Request Errored: ").append(requestLine2);
                                    inErrorBlock = true;
                                } else {
                                    // Extract status code and response time if available
                                    Pattern statusPattern = Pattern.compile("\\[(\\d{3})\\s+[^,]+,\\s+([^,]+),\\s+([0-9]+)ms]");
                                    Matcher statusMatcher = statusPattern.matcher(requestLine2);
                                    if (statusMatcher.find()) {
                                        statusCode = Integer.parseInt(statusMatcher.group(1));
                                        responseTime = Long.parseLong(statusMatcher.group(3));
                                    }
                                }

                            } catch (Exception e) {
                                System.err.println("Erreur lors de l'extraction du path depuis l'URL : " + fullUrl);
                                currentPath = "Unknown Path";
                                errorMessageBuilder.append("Erreur lors de l'extraction des détails de la requête: ").append(e.getMessage());
                                inErrorBlock = true;
                            }

                            requestStarted = true;

                        } else {
                            // URL extraction failed
                            currentPath = "Unknown Path";
                            errorMessageBuilder.append("Impossible d'extraire les informations de la requête.");
                            inErrorBlock = true;
                            System.err.println("Erreur : Impossible d'extraire les informations de la requête.");
                        }
                    } else {
                        errorMessageBuilder.append("Ligne de requête 2 est vide ou mal formée");
                        currentPath = "Unknown Path"; // mark request as UNKNOWN PATH
                        inErrorBlock = true;
                        System.err.println("Erreur : Ligne de requête 2 est vide ou mal formée.");
                    }

                    // Process test result if there was an issue with parsing
                    if (currentPath.equals("Unknown Path")) {
                        processTestResult(currentPath, httpMethod, success, statusCode, responseTime, errorMessageBuilder.toString());
                        requestStarted = false;
                        inErrorBlock = false;
                    }

                } else if (line.toLowerCase().contains("failure") && currentPath != null && !"Unknown Path".equals(currentPath)) {
                    // Capture the error message for the current path
                    errorMessageBuilder.append(line).append("\n");
                    inErrorBlock = true;

                } else if (inErrorBlock && currentPath != null && !"Unknown Path".equals(currentPath)) {
                    // Append to the errorMessage while inside an error block
                    errorMessageBuilder.append(line).append("\n");
                }
            }

            // Process the last request if there were errors
            if (inErrorBlock && currentPath != null && !"Unknown Path".equals(currentPath)) {
                processTestResult(currentPath, httpMethod, success, statusCode, responseTime, errorMessageBuilder.toString());
            }

            int exitCode = process.waitFor();
            System.out.println(exitCode == 0 ? "Tous les tests ont été exécutés avec succès !" : "Certains tests ont échoué. Code de sortie : " + exitCode);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Une erreur s'est produite lors de l'exécution des tests Newman: " + e.getMessage());
        }
        System.out.println("Fin de l'exécution des tests Newman.");
    }

    @Transactional
    protected void processTestResult(String path, String method, boolean success, int statusCode, long responseTime, String errorMessage) {
        // Do not save if path is unknown path
        if ("Unknown Path".equals(path)) {
            System.out.println("Ignorer l'enregistrement pour Unknown Path");
            return;
        }

        try {
            String level = (statusCode == 500 || (errorMessage != null && !errorMessage.isEmpty())) ? "ERROR" : "INFO";

            // Créez un objet JSON pour stocker le path et la méthode
            ObjectNode metadata = objectMapper.createObjectNode();
            metadata.put("path", path);
            metadata.put("method", method);
            String metadataString = metadata.toString();

            Optional<ApiMonitoring> existingResult = resultRepository.findByMetadata(metadataString);

            ApiMonitoring result;
            if (existingResult.isPresent()) {
                // Mettre à jour l'entrée existante
                result = existingResult.get();
                result.setTemps(LocalDateTime.now());
                result.setResponseTime(responseTime);
                result.setStatusCode(statusCode);
                result.setErrorMessage(errorMessage);
                result.setLevel(level);
                resultRepository.save(result); // Save the updated result
                System.out.println("Mise à jour réussie pour : " + path);

            } else {
                // Extract request details using regex
                result = new ApiMonitoring();
                result.setPath(path);
                result.setTemps(LocalDateTime.now());
                result.setResponseTime(responseTime);
                result.setStatusCode(statusCode);
                result.setErrorMessage(errorMessage);
                result.setLevel(level);
                result.setMetadata(metadataString); // Stocker le JSON dans la colonne metadata
                resultRepository.save(result); // Save the new result
                System.out.println("Enregistrement réussi pour : " + path);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur lors de l'enregistrement pour : " + path + ": " + e.getMessage());
        }
    }
}*/
/*@Service
public class NewmanService {

    private final ApiMonitoringRepository resultRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NewmanService(ApiMonitoringRepository resultRepository) {
        this.resultRepository = resultRepository;
        System.out.println("NewmanService créé avec le repository : " + resultRepository);
    }

    @Scheduled(cron = "0 * * * * *") // Exécution toutes les minutes
    public void runNewmanTests() {
        System.out.println("Début de l'exécution des tests Newman.");

        try {
            // Récupérer le chemin absolu du fichier postman_collection.json
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("postman_collection.json").toURI());

            System.out.println("Chemin du fichier Postman : " + file.getAbsolutePath());

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "C:\\Users\\LENOVO\\AppData\\Roaming\\npm\\newman.cmd",
                    "run", file.getAbsolutePath()
            );

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            String currentPath = null;
            String httpMethod = null;
            boolean success = false;
            int statusCode = 0;
            long responseTime = 0;
            StringBuilder errorMessageBuilder = new StringBuilder();
            boolean requestStarted = false;
            boolean inErrorBlock = false;

            while ((line = reader.readLine()) != null) {
                System.out.println("Ligne lue : " + line);

                if (line.startsWith("□ ")) {
                    if (inErrorBlock && currentPath != null) {
                        processTestResult(currentPath, httpMethod, success, statusCode, responseTime, errorMessageBuilder.toString());
                    }

                    currentPath = line.substring(2).trim();
                    System.out.println("Début de la requête : " + currentPath);
                    errorMessageBuilder.setLength(0);
                    inErrorBlock = false;

                    reader.readLine(); // Ignorer la ligne suivante ("└ ...")
                    String requestLine2 = reader.readLine();
                    System.out.println("Ligne de requête 2 : " + requestLine2);

                    statusCode = 0;
                    responseTime = 0;

                    if (requestLine2 != null) {
                        Pattern pattern = Pattern.compile("^\\s*(GET|POST|PUT|DELETE)\\s+(https?://[^\\s]+)");
                        Matcher matcher = pattern.matcher(requestLine2);

                        if (matcher.find()) {
                            httpMethod = matcher.group(1);
                            String fullUrl = matcher.group(2);

                            try {
                                URI uri = new URI(fullUrl);
                                currentPath = uri.getPath();
                                System.out.println("Chemin extrait : " + currentPath);

                                if (requestLine2.contains("[errored]")) {
                                    statusCode = 0;
                                    responseTime = 0;
                                    errorMessageBuilder.append("Request Errored: ").append(requestLine2);
                                    inErrorBlock = true;
                                } else {
                                    Pattern statusPattern = Pattern.compile("\\[(\\d{3})\\s+[^,]+,\\s+([^,]+),\\s+([0-9]+)ms]");
                                    Matcher statusMatcher = statusPattern.matcher(requestLine2);
                                    if (statusMatcher.find()) {
                                        statusCode = Integer.parseInt(statusMatcher.group(1));
                                        responseTime = Long.parseLong(statusMatcher.group(3));
                                    }
                                }
                            } catch (Exception e) {
                                System.err.println("Erreur lors de l'extraction du path depuis l'URL : " + fullUrl);
                                currentPath = "Unknown Path";
                                errorMessageBuilder.append("Erreur : ").append(e.getMessage());
                                inErrorBlock = true;
                            }
                        } else {
                            currentPath = "Unknown Path";
                            errorMessageBuilder.append("Impossible d'extraire les infos de la requête.");
                            inErrorBlock = true;
                        }
                    }

                    if (currentPath.equals("Unknown Path")) {
                        processTestResult(currentPath, httpMethod, success, statusCode, responseTime, errorMessageBuilder.toString());
                        requestStarted = false;
                        inErrorBlock = false;
                    }
                } else if (line.matches(".*Vérification du schéma de réponse pour le statut \\d{3}.*")) {
                    errorMessageBuilder.append(line).append("\n");
                    inErrorBlock = true;
                } else if (inErrorBlock && currentPath != null) {
                    errorMessageBuilder.append(line).append("\n");
                }
            }

            if (inErrorBlock && currentPath != null) {
                processTestResult(currentPath, httpMethod, success, statusCode, responseTime, errorMessageBuilder.toString());
            }

            int exitCode = process.waitFor();
            System.out.println(exitCode == 0 ? "Tous les tests ont réussi !" : "Certains tests ont échoué. Code de sortie : " + exitCode);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur lors de l'exécution des tests Newman: " + e.getMessage());
        }
        System.out.println("Fin des tests Newman.");
    }

    @Transactional
    protected void processTestResult(String path, String method, boolean success, int statusCode, long responseTime, String errorMessage) {
        if ("Unknown Path".equals(path)) {
            System.out.println("Ignorer l'enregistrement pour Unknown Path");
            return;
        }

        try {
            String level = (statusCode == 500 || (errorMessage != null && !errorMessage.isEmpty())) ? "ERROR" : "INFO";

            ObjectNode metadata = objectMapper.createObjectNode();
            metadata.put("path", path);
            metadata.put("method", method);
            String metadataString = metadata.toString();

            Optional<ApiMonitoring> existingResult = resultRepository.findByMetadata(metadataString);

            ApiMonitoring result;
            if (existingResult.isPresent()) {
                result = existingResult.get();
                result.setTemps(LocalDateTime.now());
                result.setResponseTime(responseTime);
                result.setStatusCode(statusCode);
                result.setErrorMessage(errorMessage);
                result.setLevel(level);
                resultRepository.save(result);
                System.out.println("Mise à jour réussie pour : " + path);
            } else {
                result = new ApiMonitoring();
                result.setPath(path);
                result.setTemps(LocalDateTime.now());
                result.setResponseTime(responseTime);
                result.setStatusCode(statusCode);
                result.setErrorMessage(errorMessage);
                result.setLevel(level);
                result.setMetadata(metadataString);
                resultRepository.save(result);
                System.out.println("Enregistrement réussi pour : " + path);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur lors de l'enregistrement pour : " + path + ": " + e.getMessage());
        }
    }
}*/
/*@Service
public class NewmanService {

    private final ApiMonitoringRepository resultRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NewmanService(ApiMonitoringRepository resultRepository) {
        this.resultRepository = resultRepository;
        System.out.println("NewmanService créé avec le repository : " + resultRepository);
    }

    @Scheduled(cron = "0 * * * * *") // Exécution toutes les minutes
    public void runNewmanTests() {
        System.out.println("Début de l'exécution des tests Newman.");

        try {
            // Récupérer le chemin absolu du fichier postman_collection.json dans le dossier resources
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("postman_collection.json").toURI());

            System.out.println("Chemin du fichier de collection Postman : " + file.getAbsolutePath());

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "C:\\Users\\LENOVO\\AppData\\Roaming\\npm\\newman.cmd",
                    "run", file.getAbsolutePath() // Utilisation du chemin absolu du fichier
            );

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            String currentPath = null;
            String httpMethod = null;
            boolean success = false;
            int statusCode = 0;
            long responseTime = 0;
            StringBuilder errorMessageBuilder = new StringBuilder();
            boolean inErrorBlock = false;

            while ((line = reader.readLine()) != null) {
                System.out.println("Ligne lue : " + line);

                // Check if a new request is starting based on the "□" character
                if (line.startsWith("□ ")) {
                    // If we were in a previous error block, save the error message
                    if (inErrorBlock && currentPath != null) {
                        processTestResult(currentPath, httpMethod, success, statusCode, responseTime, errorMessageBuilder.toString());
                    }

                    String apiPath = line.substring(2).trim();
                    currentPath = apiPath;
                    System.out.println("Début de la requête : " + currentPath);
                    errorMessageBuilder.setLength(0); // Clear error message for new request
                    inErrorBlock = false; // Reset error block status

                    // Read the lines containing the request details (URL, status, etc.)
                    String requestLine1 = reader.readLine(); // Line containing "└"
                    System.out.println("Ligne de requête 1 : " + requestLine1);

                    String requestLine2 = reader.readLine(); // Line containing request details
                    System.out.println("Ligne de requête 2 : " + requestLine2);

                    // Initialize statusCode and responseTime to 0
                    statusCode = 0;
                    responseTime = 0;

                    // Extract request details using regex
                    if (requestLine2 != null && requestLine2.contains(" ")) {
                        // Regex to extract method and URL (even if the line contains [errored])
                        Pattern pattern = Pattern.compile("^\\s*(GET|POST|PUT|DELETE)\\s+(https?://[^\\s]+)");
                        Matcher matcher = pattern.matcher(requestLine2);

                        if (matcher.find()) {
                            httpMethod = matcher.group(1);
                            String fullUrl = matcher.group(2);

                            try {
                                // Extract the path from the full URL
                                URI uri = new URI(fullUrl);
                                currentPath = uri.getPath(); // This will give the path part of the URL
                                System.out.println("Chemin extrait : " + currentPath);

                                // If the request is errored, set statusCode and responseTime to 0
                                if (requestLine2.contains("[errored]")) {
                                    statusCode = 0;
                                    responseTime = 0;
                                    errorMessageBuilder.append("Request Errored: ").append(requestLine2);
                                    inErrorBlock = true;
                                } else {
                                    // Extract status code and response time if available
                                    Pattern statusPattern = Pattern.compile("\\[(\\d{3})\\s+[^,]+,\\s+([^,]+),\\s+([0-9]+)ms]");
                                    Matcher statusMatcher = statusPattern.matcher(requestLine2);
                                    if (statusMatcher.find()) {
                                        statusCode = Integer.parseInt(statusMatcher.group(1));
                                        responseTime = Long.parseLong(statusMatcher.group(3));
                                    }
                                }

                            } catch (Exception e) {
                                System.err.println("Erreur lors de l'extraction du path depuis l'URL : " + fullUrl);
                                currentPath = "Unknown Path";
                                errorMessageBuilder.append("Erreur lors de l'extraction des détails de la requête: ").append(e.getMessage());
                                inErrorBlock = true;
                            }

                        } else {
                            // URL extraction failed
                            currentPath = "Unknown Path";
                            errorMessageBuilder.append("Impossible d'extraire les informations de la requête.");
                            inErrorBlock = true;
                            System.err.println("Erreur : Impossible d'extraire les informations de la requête.");
                        }
                    } else {
                        errorMessageBuilder.append("Ligne de requête 2 est vide ou mal formée");
                        currentPath = "Unknown Path"; // mark request as UNKNOWN PATH
                        inErrorBlock = true;
                        System.err.println("Erreur : Ligne de requête 2 est vide ou mal formée.");
                    }

                    // Process test result if there was an issue with parsing
                    if (currentPath.equals("Unknown Path")) {
                        processTestResult(currentPath, httpMethod, success, statusCode, responseTime, errorMessageBuilder.toString());
                        inErrorBlock = false;
                    }

                } else if (line.toLowerCase().contains("failure") && currentPath != null && !"Unknown Path".equals(currentPath)) {
                    // Capture the error message for the current path

                    // Extract only the error message starting with a number and a dot
                    Pattern errorPattern = Pattern.compile("^\\s*\\d+\\..*");
                    Matcher errorMatcher = errorPattern.matcher(line);

                    if (errorMatcher.find()) {
                        errorMessageBuilder.append(errorMatcher.group(0)).append("\n");
                    }
                    inErrorBlock = true;

                } else if (inErrorBlock && currentPath != null && !"Unknown Path".equals(currentPath)) {
                    // Append to the errorMessage while inside an error block
                    // Extract only the error message starting with a number and a dot
                    Pattern errorPattern = Pattern.compile("^\\s*\\d+\\..*");
                    Matcher errorMatcher = errorPattern.matcher(line);

                    if (errorMatcher.find()) {
                        errorMessageBuilder.append(errorMatcher.group(0)).append("\n");
                    }

                } else if (line.trim().startsWith("√") && currentPath != null) {
                    // If a test case passed (indicated by √), clear the error message
                    errorMessageBuilder.setLength(0);
                    inErrorBlock = false;
                    System.out.println("Test passed, clearing error message for: " + currentPath);
                }
            }

            // Process the last request if there were errors
            if (inErrorBlock && currentPath != null && !"Unknown Path".equals(currentPath)) {
                processTestResult(currentPath, httpMethod, success, statusCode, responseTime, errorMessageBuilder.toString());
            } else if (currentPath != null && !"Unknown Path".equals(currentPath) && errorMessageBuilder.length() == 0) {
                // If the test passed successfully, process it with a null error message
                processTestResult(currentPath, httpMethod, true, statusCode, responseTime, null);
            }

            int exitCode = process.waitFor();
            System.out.println(exitCode == 0 ? "Tous les tests ont été exécutés avec succès !" : "Certains tests ont échoué. Code de sortie : " + exitCode);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Une erreur s'est produite lors de l'exécution des tests Newman: " + e.getMessage());
        }
        System.out.println("Fin de l'exécution des tests Newman.");
    }

    @Transactional
    protected void processTestResult(String path, String method, boolean success, int statusCode, long responseTime, String errorMessage) {
        // Do not save if path is unknown path
        if ("Unknown Path".equals(path)) {
            System.out.println("Ignorer l'enregistrement pour Unknown Path");
            return;
        }

        try {
            String level = (statusCode == 500 || (errorMessage != null && !errorMessage.isEmpty())) ? "ERROR" : "INFO";

            // Créez un objet JSON pour stocker le path et la méthode
            ObjectNode metadata = objectMapper.createObjectNode();
            metadata.put("path", path);
            metadata.put("method", method);
            String metadataString = metadata.toString();

            Optional<ApiMonitoring> existingResult = resultRepository.findByMetadata(metadataString);

            ApiMonitoring result;
            if (existingResult.isPresent()) {
                // Mettre à jour l'entrée existante
                result = existingResult.get();
                result.setTemps(LocalDateTime.now());
                result.setResponseTime(responseTime);
                result.setStatusCode(statusCode);

                // Set errorMessage to null if it's empty
                result.setErrorMessage(errorMessage != null && !errorMessage.isEmpty() ? errorMessage : null);

                result.setLevel(level);
                resultRepository.save(result); // Save the updated result
                System.out.println("Mise à jour réussie pour : " + path);

            } else {
                // Extract request details using regex
                result = new ApiMonitoring();
                result.setPath(path);
                result.setTemps(LocalDateTime.now());
                result.setResponseTime(responseTime);
                result.setStatusCode(statusCode);

                // Set errorMessage to null if it's empty
                result.setErrorMessage(errorMessage != null && !errorMessage.isEmpty() ? errorMessage : null);

                result.setLevel(level);
                result.setMetadata(metadataString); // Stocker le JSON dans la colonne metadata
                resultRepository.save(result); // Save the new result
                System.out.println("Enregistrement réussi pour : " + path);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur lors de l'enregistrement pour : " + path + ": " + e.getMessage());
        }
    }
}*/
/*@Service
public class NewmanService {

    private final ApiMonitoringRepository resultRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NewmanService(ApiMonitoringRepository resultRepository) {
        this.resultRepository = resultRepository;
        System.out.println("NewmanService créé avec le repository : " + resultRepository);
    }

    @Scheduled(cron = "0 * * * * *") // Exécution toutes les minutes
    public void runNewmanTests() {
        System.out.println("Début de l'exécution des tests Newman.");

        try {
            // Récupérer le chemin absolu du fichier postman_collection.json dans le dossier resources
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("postman_collection.json").toURI());

            System.out.println("Chemin du fichier de collection Postman : " + file.getAbsolutePath());

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "C:\\Users\\LENOVO\\AppData\\Roaming\\npm\\newman.cmd",
                    "run", file.getAbsolutePath() // Utilisation du chemin absolu du fichier
            );

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            String currentPath = null;
            String httpMethod = null;
            boolean success = false;
            int statusCode = 0;
            long responseTime = 0;
            StringBuilder errorMessageBuilder = new StringBuilder();
            boolean inErrorBlock = false;
            boolean testPassed = false; // Track if the test case passed successfully

            while ((line = reader.readLine()) != null) {
                System.out.println("Ligne lue : " + line);

                // Check if a new request is starting based on the "□" character
                if (line.startsWith("□ ")) {
                    // If we were in a previous error block, save the error message
                    if (inErrorBlock && currentPath != null) {
                        processTestResult(currentPath, httpMethod, success, statusCode, responseTime,
                                testPassed ? null : errorMessageBuilder.toString()); // Pass null if test passed
                    }

                    String apiPath = line.substring(2).trim();
                    currentPath = apiPath;
                    System.out.println("Début de la requête : " + currentPath);
                    errorMessageBuilder.setLength(0); // Clear error message for new request
                    inErrorBlock = false; // Reset error block status
                    testPassed = false;   // Reset testPassed for the new request

                    // Read the lines containing the request details (URL, status, etc.)
                    String requestLine1 = reader.readLine(); // Line containing "└"
                    System.out.println("Ligne de requête 1 : " + requestLine1);

                    String requestLine2 = reader.readLine(); // Line containing request details
                    System.out.println("Ligne de requête 2 : " + requestLine2);

                    // Initialize statusCode and responseTime to 0
                    statusCode = 0;
                    responseTime = 0;

                    // Extract request details using regex
                    if (requestLine2 != null && requestLine2.contains(" ")) {
                        // Regex to extract method and URL (even if the line contains [errored])
                        Pattern pattern = Pattern.compile("^\\s*(GET|POST|PUT|DELETE)\\s+(https?://[^\\s]+)");
                        Matcher matcher = pattern.matcher(requestLine2);

                        if (matcher.find()) {
                            httpMethod = matcher.group(1);
                            String fullUrl = matcher.group(2);

                            try {
                                // Extract the path from the full URL
                                URI uri = new URI(fullUrl);
                                currentPath = uri.getPath(); // This will give the path part of the URL
                                System.out.println("Chemin extrait : " + currentPath);

                                // If the request is errored, set statusCode and responseTime to 0
                                if (requestLine2.contains("[errored]")) {
                                    statusCode = 0;
                                    responseTime = 0;
                                    errorMessageBuilder.append("Request Errored: ").append(requestLine2);
                                    inErrorBlock = true;
                                } else {
                                    // Extract status code and response time if available
                                    Pattern statusPattern = Pattern.compile("\\[(\\d{3})\\s+[^,]+,\\s+([^,]+),\\s+([0-9]+)ms]");
                                    Matcher statusMatcher = statusPattern.matcher(requestLine2);
                                    if (statusMatcher.find()) {
                                        statusCode = Integer.parseInt(statusMatcher.group(1));
                                        responseTime = Long.parseLong(statusMatcher.group(3));
                                    }
                                }

                            } catch (Exception e) {
                                System.err.println("Erreur lors de l'extraction du path depuis l'URL : " + fullUrl);
                                currentPath = "Unknown Path";
                                errorMessageBuilder.append("Erreur lors de l'extraction des détails de la requête: ")
                                        .append(e.getMessage());
                                inErrorBlock = true;
                            }

                        } else {
                            // URL extraction failed
                            currentPath = "Unknown Path";
                            errorMessageBuilder.append("Impossible d'extraire les informations de la requête.");
                            inErrorBlock = true;
                            System.err.println("Erreur : Impossible d'extraire les informations de la requête.");
                        }
                    } else {
                        errorMessageBuilder.append("Ligne de requête 2 est vide ou mal formée");
                        currentPath = "Unknown Path"; // mark request as UNKNOWN PATH
                        inErrorBlock = true;
                        System.err.println("Erreur : Ligne de requête 2 est vide ou mal formée.");
                    }

                    // Process test result if there was an issue with parsing
                    if (currentPath.equals("Unknown Path")) {
                        processTestResult(currentPath, httpMethod, success, statusCode, responseTime,
                                errorMessageBuilder.toString());
                        inErrorBlock = false;
                    }

                } else if (line.trim().startsWith("√") && currentPath != null) {
                    // If a line starts with √, the API passed
                    testPassed = true;
                    errorMessageBuilder.setLength(0);
                    inErrorBlock = false;
                    System.out.println("API passed (√), error message cleared for: " + currentPath);

                } else if (line.toLowerCase().contains("failure") && currentPath != null
                        && !"Unknown Path".equals(currentPath)) {
                    // Capture the error message for the current path

                    // Extract only the error message starting with a number and a dot
                    Pattern errorPattern = Pattern.compile("^\\s*\\d+\\..*");
                    Matcher errorMatcher = errorPattern.matcher(line);

                    if (errorMatcher.find()) {
                        errorMessageBuilder.append(errorMatcher.group(0)).append("\n");
                    }
                    inErrorBlock = true;

                } else if (inErrorBlock && currentPath != null && !"Unknown Path".equals(currentPath)) {
                    // Append to the errorMessage while inside an error block
                    // Extract only the error message starting with a number and a dot
                    Pattern errorPattern = Pattern.compile("^\\s*\\d+\\..*");
                    Matcher errorMatcher = errorPattern.matcher(line);

                    if (errorMatcher.find()) {
                        errorMessageBuilder.append(errorMatcher.group(0)).append("\n");
                    }

                }
            }

            // Process the last request
            if (currentPath != null && !"Unknown Path".equals(currentPath)) {
                processTestResult(currentPath, httpMethod, success, statusCode, responseTime,
                        testPassed ? null : errorMessageBuilder.toString()); // Pass null if test passed
            }

            int exitCode = process.waitFor();
            System.out.println(exitCode == 0 ? "Tous les tests ont été exécutés avec succès !"
                    : "Certains tests ont échoué. Code de sortie : " + exitCode);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Une erreur s'est produite lors de l'exécution des tests Newman: " + e.getMessage());
        }
        System.out.println("Fin de l'exécution des tests Newman.");
    }

    @Transactional
    protected void processTestResult(String path, String method, boolean success, int statusCode, long responseTime,
                                     String errorMessage) {
        // Do not save if path is unknown path
        if ("Unknown Path".equals(path)) {
            System.out.println("Ignorer l'enregistrement pour Unknown Path");
            return;
        }

        try {
            String level = (statusCode == 500 || (errorMessage != null && !errorMessage.isEmpty())) ? "ERROR" : "INFO";

            // Créez un objet JSON pour stocker le path et la méthode
            ObjectNode metadata = objectMapper.createObjectNode();
            metadata.put("path", path);
            metadata.put("method", method);
            String metadataString = metadata.toString();

            Optional<ApiMonitoring> existingResult = resultRepository.findByMetadata(metadataString);

            ApiMonitoring result;
            if (existingResult.isPresent()) {
                // Mettre à jour l'entrée existante
                result = existingResult.get();
                result.setTemps(LocalDateTime.now());
                result.setResponseTime(responseTime);
                result.setStatusCode(statusCode);

                // Set errorMessage to null if it's empty
                result.setErrorMessage(errorMessage); // It's already null or the error message

                result.setLevel(level);
                resultRepository.save(result); // Save the updated result
                System.out.println("Mise à jour réussie pour : " + path);

            } else {
                // Extract request details using regex
                result = new ApiMonitoring();
                result.setPath(path);
                result.setTemps(LocalDateTime.now());
                result.setResponseTime(responseTime);
                result.setStatusCode(statusCode);

                // Set errorMessage to null if it's empty
                result.setErrorMessage(errorMessage); // It's already null or the error message

                result.setLevel(level);
                result.setMetadata(metadataString); // Stocker le JSON dans la colonne metadata
                resultRepository.save(result); // Save the new result
                System.out.println("Enregistrement réussi pour : " + path);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur lors de l'enregistrement pour : " + path + ": " + e.getMessage());
        }
    }
}*/
/*@Service
public class NewmanService {

    private final ApiMonitoringRepository resultRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NewmanService(ApiMonitoringRepository resultRepository) {
        this.resultRepository = resultRepository;
        System.out.println("NewmanService créé avec le repository : " + resultRepository);
    }

    @Scheduled(cron = "0 * * * * *") // Exécution toutes les minutes
    public void runNewmanTests() {
        System.out.println("Début de l'exécution des tests Newman.");

        try {
            // Récupérer le chemin absolu du fichier postman_collection.json dans le dossier resources
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("postman_collection.json").toURI());

            System.out.println("Chemin du fichier de collection Postman : " + file.getAbsolutePath());

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "C:\\Users\\LENOVO\\AppData\\Roaming\\npm\\newman.cmd",
                    "run", file.getAbsolutePath() // Utilisation du chemin absolu du fichier
            );

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            String currentPath = null;
            String httpMethod = null;
            boolean success = false;
            int statusCode = 0;
            long responseTime = 0;
            StringBuilder errorMessageBuilder = new StringBuilder();
            boolean inErrorBlock = false;
            boolean allTestsPassed = true; // Assume all tests pass until an error is found

            while ((line = reader.readLine()) != null) {
                System.out.println("Ligne lue : " + line);

                // Check if a new request is starting based on the "□" character
                if (line.startsWith("□ ")) {
                    // Process the previous result before starting a new request
                    if (currentPath != null) {
                        processTestResult(currentPath, httpMethod, success, statusCode, responseTime,
                                allTestsPassed ? null : errorMessageBuilder.toString());
                    }

                    String apiPath = line.substring(2).trim();
                    currentPath = apiPath;
                    System.out.println("Début de la requête : " + currentPath);
                    errorMessageBuilder.setLength(0); // Clear error message for new request
                    inErrorBlock = false; // Reset error block status
                    allTestsPassed = true; // Reset allTestsPassed for new request

                    // Read the lines containing the request details (URL, status, etc.)
                    String requestLine1 = reader.readLine(); // Line containing "└"
                    System.out.println("Ligne de requête 1 : " + requestLine1);

                    String requestLine2 = reader.readLine(); // Line containing request details
                    System.out.println("Ligne de requête 2 : " + requestLine2);

                    // Initialize statusCode and responseTime to 0
                    statusCode = 0;
                    responseTime = 0;

                    // Extract request details using regex
                    if (requestLine2 != null && requestLine2.contains(" ")) {
                        // Regex to extract method and URL (even if the line contains [errored])
                        Pattern pattern = Pattern.compile("^\\s*(GET|POST|PUT|DELETE)\\s+(https?://[^\\s]+)");
                        Matcher matcher = pattern.matcher(requestLine2);

                        if (matcher.find()) {
                            httpMethod = matcher.group(1);
                            String fullUrl = matcher.group(2);

                            try {
                                // Extract the path from the full URL
                                URI uri = new URI(fullUrl);
                                currentPath = uri.getPath(); // This will give the path part of the URL
                                System.out.println("Chemin extrait : " + currentPath);

                                // If the request is errored, set statusCode and responseTime to 0
                                if (requestLine2.contains("[errored]")) {
                                    statusCode = 0;
                                    responseTime = 0;
                                    errorMessageBuilder.append("Request Errored: ").append(requestLine2);
                                    inErrorBlock = true;
                                    allTestsPassed = false;

                                } else {
                                    // Extract status code and response time if available
                                    Pattern statusPattern = Pattern.compile("\\[(\\d{3})\\s+[^,]+,\\s+([^,]+),\\s+([0-9]+)ms]");
                                    Matcher statusMatcher = statusPattern.matcher(requestLine2);
                                    if (statusMatcher.find()) {
                                        statusCode = Integer.parseInt(statusMatcher.group(1));
                                        responseTime = Long.parseLong(statusMatcher.group(3));
                                    }
                                }

                            } catch (Exception e) {
                                System.err.println("Erreur lors de l'extraction du path depuis l'URL : " + fullUrl);
                                currentPath = "Unknown Path";
                                errorMessageBuilder.append("Erreur lors de l'extraction des détails de la requête: ")
                                        .append(e.getMessage());
                                inErrorBlock = true;
                                allTestsPassed = false;

                            }

                        } else {
                            // URL extraction failed
                            currentPath = "Unknown Path";
                            errorMessageBuilder.append("Impossible d'extraire les informations de la requête.");
                            inErrorBlock = true;
                            allTestsPassed = false;
                            System.err.println("Erreur : Impossible d'extraire les informations de la requête.");
                        }
                    } else {
                        errorMessageBuilder.append("Ligne de requête 2 est vide ou mal formée");
                        currentPath = "Unknown Path"; // mark request as UNKNOWN PATH
                        inErrorBlock = true;
                        allTestsPassed = false;
                        System.err.println("Erreur : Ligne de requête 2 est vide ou mal formée.");
                    }

                    // Process test result if there was an issue with parsing
                    if (currentPath.equals("Unknown Path")) {
                        processTestResult(currentPath, httpMethod, success, statusCode, responseTime,
                                errorMessageBuilder.toString());
                        inErrorBlock = false;
                    }

                } else if (line.trim().startsWith("√") && currentPath != null) {
                    // If a line starts with √, the API passed - ignore the line
                    System.out.println("API passed (√), ignoring the line: " + line);

                } else if (line.toLowerCase().contains("failure") && currentPath != null
                        && !"Unknown Path".equals(currentPath)) {
                    // Capture the error message for the current path, ensuring to extract only the numeric dot format.
                    Pattern errorPattern = Pattern.compile("^\\s*\\d+\\..*");
                    Matcher errorMatcher = errorPattern.matcher(line);

                    if (errorMatcher.find()) {
                        String errorLine = errorMatcher.group(0);  // get line with number dot
                        errorMessageBuilder.append(errorLine).append("\n");
                        inErrorBlock = true;
                        allTestsPassed = false;
                    }

                } else if (inErrorBlock && currentPath != null && !"Unknown Path".equals(currentPath)) {
                    // Append to the errorMessage while inside an error block, filtering for num dot messages
                    Pattern errorPattern = Pattern.compile("^\\s*\\d+\\..*");
                    Matcher errorMatcher = errorPattern.matcher(line);

                    if (errorMatcher.find()) {
                        String errorLine = errorMatcher.group(0);  // get line with number dot
                        errorMessageBuilder.append(errorLine).append("\n");
                    }

                } else if (currentPath != null && !"Unknown Path".equals(currentPath)) {
                    // If none of the conditions are met (not an error, not a success marker, but we are in a current path),
                    // make sure the last state is that errors are stored
                    allTestsPassed = false;
                }
            }

            // Process the last request
            if (currentPath != null) {
                processTestResult(currentPath, httpMethod, success, statusCode, responseTime,
                        allTestsPassed ? null : errorMessageBuilder.toString());
            }

            int exitCode = process.waitFor();
            System.out.println(exitCode == 0 ? "Tous les tests ont été exécutés avec succès !"
                    : "Certains tests ont échoué. Code de sortie : " + exitCode);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Une erreur s'est produite lors de l'exécution des tests Newman: " + e.getMessage());
        }
        System.out.println("Fin de l'exécution des tests Newman.");
    }

    @Transactional
    protected void processTestResult(String path, String method, boolean success, int statusCode, long responseTime,
                                     String errorMessage) {
        // Do not save if path is unknown path
        if ("Unknown Path".equals(path)) {
            System.out.println("Ignorer l'enregistrement pour Unknown Path");
            return;
        }

        try {
            String level = (statusCode == 500 || (errorMessage != null && !errorMessage.isEmpty())) ? "ERROR" : "INFO";

            // Créez un objet JSON pour stocker le path et la méthode
            ObjectNode metadata = objectMapper.createObjectNode();
            metadata.put("path", path);
            metadata.put("method", method);
            String metadataString = metadata.toString();

            Optional<ApiMonitoring> existingResult = resultRepository.findByMetadata(metadataString);

            ApiMonitoring result;
            if (existingResult.isPresent()) {
                // Mettre à jour l'entrée existante
                result = existingResult.get();
                result.setTemps(LocalDateTime.now());
                result.setResponseTime(responseTime);
                result.setStatusCode(statusCode);

                // Set errorMessage to null if it's empty
                result.setErrorMessage(errorMessage); // It's already null or the error message

                result.setLevel(level);
                resultRepository.save(result); // Save the updated result
                System.out.println("Mise à jour réussie pour : " + path);

            } else {
                // Extract request details using regex
                result = new ApiMonitoring();
                result.setPath(path);
                result.setTemps(LocalDateTime.now());
                result.setResponseTime(responseTime);
                result.setStatusCode(statusCode);

                // Set errorMessage to null if it's empty
                result.setErrorMessage(errorMessage); // It's already null or the error message

                result.setLevel(level);
                result.setMetadata(metadataString); // Stocker le JSON dans la colonne metadata
                resultRepository.save(result); // Save the new result
                System.out.println("Enregistrement réussi pour : " + path);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur lors de l'enregistrement pour : " + path + ": " + e.getMessage());
        }
    }
}*/
/* codeeeee hedhaaaaaaa cvvvvvvv */
@Service
public class NewmanService {

    private final ApiMonitoringRepository resultRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NewmanService(ApiMonitoringRepository resultRepository) {
        this.resultRepository = resultRepository;
        System.out.println("NewmanService créé avec le repository : " + resultRepository);
    }

    @Scheduled(cron = "0 0 */6 * * *") // Exécution toutes les minutes
    public void runNewmanTests() {
        System.out.println("Début de l'exécution des tests Newman.");

        try {
            // Récupérer le chemin du fichier postman_collection.json dans le dossier resources
            ClassLoader classLoader = getClass().getClassLoader(); //retourne la classe actuelle.
            File file = new File(classLoader.getResource("postman_collection.json").toURI());
            //Cette méthode cherche le fichier "postman_collection.json" dans les ressources du projet.
            //Elle retourne une URL pointant vers ce fichier.

            System.out.println("Chemin du fichier de collection Postman : " + file.getAbsolutePath());

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "C:\\Users\\LENOVO\\AppData\\Roaming\\npm\\newman.cmd",
                    "run", file.getAbsolutePath() // Utilisation du chemin absolu du fichier
            );

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start(); //Démarre le processus externe

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream())); //pour lire les données afficher dans la console
            //BufferedReader permet de lire la sortie ligne par ligne.
            String line;
            String currentPath = null;
            String httpMethod = null;
            boolean success = false;
            int statusCode = 0;
            long responseTime = 0;
            StringBuilder errorMessageBuilder = new StringBuilder();
            boolean inErrorBlock = false;
            boolean hasNumDotError = false; //indicateur : si une ligne contient un nombre suivie d'un point cela signifie qu'il ya une erreur dans l'api

            while ((line = reader.readLine()) != null) {
                System.out.println("Ligne lue : " + line);


                if (line.startsWith("□ ")) { //ligne qui contient les informations ur l'api

                    if (currentPath != null) { //la premiére fois currentpath est null donc on n'enregistre rien
                        processTestResult(currentPath, httpMethod, success, statusCode, responseTime,
                                hasNumDotError ? errorMessageBuilder.toString() : null); //Si hasNumDotError est vrai → on passe le message d'erreur Sinon on passe null (pas d'erreur).
                    }

                    // Renitialisation des paramétres
                    String apiPath = line.substring(2).trim(); //trim() supprime les espaces avant/après.
                    currentPath = apiPath;
                    System.out.println("Début de la requête : " + currentPath);
                    errorMessageBuilder.setLength(0);
                    inErrorBlock = false;
                    hasNumDotError = false;


                    String requestLine1 = reader.readLine(); // ligne qui "└"
                    System.out.println("Ligne de requête 1 : " + requestLine1);

                    String requestLine2 = reader.readLine();
                    System.out.println("Ligne de requête 2 : " + requestLine2);

                    // initialisation statusCode et responseTime a 0
                    statusCode = 0;
                    responseTime = 0;


                    if (requestLine2 != null && requestLine2.contains(" ")) {

                        Pattern pattern = Pattern.compile("^\\s*(GET|POST|PUT|DELETE)\\s+(https?://[^\\s]+)"); //Pattern présente un modéle a suivre
                        //Pattern est un modèle précompilé qui recherche une méthode HTTP (GET, POST, PUT, DELETE) suivie d'une URL dans une ligne de texte.
                        Matcher matcher = pattern.matcher(requestLine2); //matcher compare ente le modéle a suivre et la ligne affiché
                        //Matcher tente de trouver une correspondance entre pattern et requestLine2.

                        if (matcher.find()) {
                            httpMethod = matcher.group(1); //Contient la méthode HTTP
                            String fullUrl = matcher.group(2); //Contient l'URL complète

                            try {

                                URI uri = new URI(fullUrl);
                                currentPath = uri.getPath();  //Convertit fullUrl en un objet URI et récupère uniquement le chemin (/api/users).
                                System.out.println("Chemin extrait : " + currentPath);

                                // si la partie ou ilya les informations de l'api contient [errored]
                                if (requestLine2.contains("[errored]")) {
                                    statusCode = 0;
                                    responseTime = 0;
                                    errorMessageBuilder.append("Request Errored: ").append(requestLine2);
                                    inErrorBlock = true;
                                } else {

                                    Pattern statusPattern = Pattern.compile("\\[(\\d{3})\\s+[^,]+,\\s+([^,]+),\\s+([0-9]+)ms]");
                                    Matcher statusMatcher = statusPattern.matcher(requestLine2);
                                    if (statusMatcher.find()) {
                                        statusCode = Integer.parseInt(statusMatcher.group(1));
                                        responseTime = Long.parseLong(statusMatcher.group(3));
                                    }
                                }

                            } catch (Exception e) {
                                System.err.println("Erreur lors de l'extraction du path depuis l'URL : " + fullUrl);
                                currentPath = "Unknown Path";
                                errorMessageBuilder.append("Erreur lors de l'extraction des détails de la requête: ")
                                        .append(e.getMessage());
                                inErrorBlock = true;
                            }

                        } else {
                            // extraction de l'url incorrecte
                            currentPath = "Unknown Path";
                            errorMessageBuilder.append("Impossible d'extraire les informations de la requête.");
                            inErrorBlock = true;
                            System.err.println("Erreur : Impossible d'extraire les informations de la requête.");
                        }
                    } else {
                        errorMessageBuilder.append("Ligne de requête 2 est vide ou mal formée");
                        currentPath = "Unknown Path";
                        inErrorBlock = true;
                        System.err.println("Erreur : Ligne de requête 2 est vide ou mal formée.");
                    }


                    if (currentPath.equals("Unknown Path")) {
                        processTestResult(currentPath, httpMethod, success, statusCode, responseTime,
                                errorMessageBuilder.toString());
                        inErrorBlock = false;
                    }

                    // si la ligne contient ce symbole √ (elle est considére comme valide)
                } else if (line.trim().startsWith("√") && currentPath != null) {
                    System.out.println("API passed (√), ignoring the line: " + line);

                } else {
                    //détection des erreurs numérotés
                    Pattern errorPattern = Pattern.compile("^\\s*\\d+\\..*");
                    Matcher errorMatcher = errorPattern.matcher(line);

                    if (errorMatcher.find()) {
                        String errorLine = errorMatcher.group(0);  // get line with number dot (exemple 1.)
                        errorMessageBuilder.append(errorLine).append("\n");
                        inErrorBlock = true;
                        hasNumDotError = true; 
                    }

                }
            }


            if (currentPath != null) {
                processTestResult(currentPath, httpMethod, success, statusCode, responseTime,
                        hasNumDotError ? errorMessageBuilder.toString() : null);
            //ceci sert a enregistrer le dernier test car cependant, ce test ne sera pas enregistré immédiatement tant qu'on ne passe pas à une nouvelle ligne qui marque une nouvelle API.
            }

            int exitCode = process.waitFor();
            //ffiche si tous les tests sont réussis (exitCode == 0) ou non.
            System.out.println(exitCode == 0 ? "Tous les tests ont été exécutés avec succès !"
                    : "Certains tests ont échoué. Code de sortie : " + exitCode);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Une erreur s'est produite lors de l'exécution des tests Newman: " + e.getMessage());
        }
        System.out.println("Fin de l'exécution des tests Newman.");
    }

    @Transactional
    protected void processTestResult(String path, String method, boolean success, int statusCode, long responseTime,
                                     String errorMessage) {

        if ("Unknown Path".equals(path)) {
            System.out.println("Ignorer l'enregistrement pour Unknown Path");
            return;
        }

        try {
            String level = (statusCode == 500 || (errorMessage != null && !errorMessage.isEmpty())) ? "ERROR" : "INFO";

            // metadata : path + methode
            ObjectNode metadata = objectMapper.createObjectNode();
            metadata.put("path", path);
            metadata.put("method", method);
            String metadataString = metadata.toString();

            Optional<ApiMonitoring> existingResult = resultRepository.findByMetadata(metadataString);

            ApiMonitoring result;
            if (existingResult.isPresent()) {
                // Mettre à jour l'entrée existante
                result = existingResult.get();
                result.setTemps(LocalDateTime.now());
                result.setResponseTime(responseTime);
                result.setStatusCode(statusCode);


                result.setErrorMessage(errorMessage);

                result.setLevel(level);
                resultRepository.save(result);
                System.out.println("Mise à jour réussie pour : " + path);

            } else {

                result = new ApiMonitoring();
                result.setPath(path);
                result.setTemps(LocalDateTime.now());
                result.setResponseTime(responseTime);
                result.setStatusCode(statusCode);


                result.setErrorMessage(errorMessage);

                result.setLevel(level);
                result.setMetadata(metadataString);
                resultRepository.save(result);
                System.out.println("Enregistrement réussi pour : " + path);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur lors de l'enregistrement pour : " + path + ": " + e.getMessage());
        }
    }
}
