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
                       "run", "" //Exécute la collection de tests Postman Reqres_API.
               );*/
        /*try {
            // Récupérer le chemin absolu du fichier  dans le dossier resources
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("").toURI());

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
import java.util.concurrent.TimeUnit;
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

            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("").toURI());

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

            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("").toURI());

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

            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("p_c.json").toURI());

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

            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("p_c.json").toURI());

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

            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("p_c.json").toURI());

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

            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("p_c.json").toURI());

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

            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("p_c.json").toURI());

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

            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("p_c.json").toURI());

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

            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("p_c.json").toURI());

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

            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("p_c.json").toURI());

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

            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("p_c.json").toURI());

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

            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("p_c.json").toURI());

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

            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("p_c.json").toURI());

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

            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("p_c.json").toURI());

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

            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("p_c.json").toURI());

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

            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("p_c.json").toURI());

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

            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("p_c.json").toURI());

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

            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("p_c.json").toURI());

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

            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("p_c.json").toURI());

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
/*@Service
public class NewmanService {

    private final ApiMonitoringRepository resultRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NewmanService(ApiMonitoringRepository resultRepository) {
        this.resultRepository = resultRepository;
        System.out.println("NewmanService créé avec le repository : " + resultRepository);
    }

    @Scheduled(cron = "0 0 * * * *") // Exécution toutes les minutes
    public void runNewmanTests() {
        System.out.println("Début de l'exécution des tests Newman.");

        try {
            // Récupérer le chemin du fichier p_c.json dans le dossier resources
            ClassLoader classLoader = getClass().getClassLoader(); //retourne la classe actuelle.
            File file = new File(classLoader.getResource("postman_collection.json").toURI());
            //Cette méthode cherche le fichier "p_c.json" dans les ressources du projet.
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
}*/
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
// Removed import for @Value: import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Assuming ApiMonitoring and ApiMonitoringRepository are defined in correct packages
// import com.example.api_tierces.model.ApiMonitoring;
// import com.example.api_tierces.repository.ApiMonitoringRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.concurrent.atomic.AtomicLong;
// Imports nécessaires
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Assurez-vous que ces imports pointent vers vos classes réelles
// import com.example.api_tierces.model.ApiMonitoring;
// import com.example.api_tierces.repository.ApiMonitoringRepository;

/*code cv rbatnaaaa l metriqueeee b prometheussss */
/*@Service
public class NewmanService { // *** NOM DE CLASSE CORRECT ***

    // --- Dependencies ---
    private final ApiMonitoringRepository resultRepository;
    private final MeterRegistry meterRegistry;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // --- Metric State Storage ---
    private final Map<String, TestMetricData> latestTestMetrics = new ConcurrentHashMap<>();

    private record TestMetricData(
            AtomicLong responseTimeMs,
            AtomicInteger statusCode,
            AtomicInteger success // 1 = test script passed, 0 = test script failed or error
    ) {
        TestMetricData() {
            this(new AtomicLong(0), new AtomicInteger(0), new AtomicInteger(0));
        }
    }

    // --- Constructor Injection ---
    public NewmanService(ApiMonitoringRepository resultRepository, MeterRegistry meterRegistry) { // *** NOM CONSTRUCTEUR CORRIGÉ ***
        this.resultRepository = resultRepository;
        this.meterRegistry = meterRegistry;
        // Log message mis à jour
        System.out.println("NewmanService créé avec le repository : " + resultRepository + " et MeterRegistry: " + meterRegistry);
    }

    // --- Scheduled Newman Execution ---
    @Scheduled(cron = "${newman.schedule.cron:0 * * * * *}") // Exécution toutes les minutes par défaut
    public void runScheduledNewmanTests() {
        System.out.println("Début de l'exécution planifiée des tests Newman.");
        String defaultCollectionResourceName = "postman_collection.json";

        try {
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource(defaultCollectionResourceName).toURI());
            System.out.println("Utilisation de la collection planifiée : " + file.getAbsolutePath());
            executeNewmanAndProcessResults(file.getAbsolutePath());
        } catch (URISyntaxException | NullPointerException e) {
            System.err.println("Erreur critique: Impossible de trouver ou charger le fichier de collection par défaut '" + defaultCollectionResourceName + "' dans les ressources: " + e.getMessage());
            e.printStackTrace();
            meterRegistry.counter("newman_run_errors_total", Tags.of("reason", "resource_not_found")).increment();
        } catch (Exception e) {
            System.err.println("Erreur inattendue lors de la préparation de l'exécution planifiée: " + e.getMessage());
            e.printStackTrace();
            meterRegistry.counter("newman_run_errors_total", Tags.of("reason", "setup_error")).increment();
        }
        System.out.println("Fin de l'exécution planifiée des tests Newman.");
    }

    // --- Manual Processing from JSON String ---
    // Cette méthode est optionnelle si vous n'en avez pas besoin via un Controller
    public String processPostmanCollection(String postmanCollectionJson) {
        System.out.println("Début du traitement manuel de la collection Postman fournie.");
        Path tempFilePath = null;
        try {
            tempFilePath = Files.createTempFile("postman_manual_", ".json");
            System.out.println("Fichier temporaire créé : " + tempFilePath.toString());
            Files.writeString(tempFilePath, postmanCollectionJson, StandardOpenOption.WRITE);
            // executeNewmanAndProcessResults utilise le chemin Newman hardcodé
            boolean success = executeNewmanAndProcessResults(tempFilePath.toAbsolutePath().toString());
            return success ? "Collection traitée avec succès." : "Collection traitée avec des erreurs (voir logs).";
        } catch (IOException e) {
            System.err.println("Erreur I/O lors de la création/écriture du fichier temporaire: " + e.getMessage());
            e.printStackTrace();
            meterRegistry.counter("newman_run_errors_total", Tags.of("reason", "temp_file_io")).increment();
            return "Erreur I/O lors de la préparation du traitement : " + e.getMessage();
        } catch (Exception e) {
            System.err.println("Erreur inattendue lors du traitement manuel de la collection: " + e.getMessage());
            e.printStackTrace();
            meterRegistry.counter("newman_run_errors_total", Tags.of("reason", "unknown_manual")).increment();
            return "Erreur inattendue lors du traitement : " + e.getMessage();
        } finally {
            if (tempFilePath != null) {
                try { Files.deleteIfExists(tempFilePath); System.out.println("Fichier temporaire supprimé : " + tempFilePath.toString()); }
                catch (IOException e) { System.err.println("Attention : Impossible de supprimer le fichier temporaire : " + tempFilePath + " - " + e.getMessage()); }
            }
            System.out.println("Fin du traitement manuel de la collection Postman fournie.");
        }
    }

    // --- Common Newman Execution and Result Processing Logic ---
    private boolean executeNewmanAndProcessResults(String collectionPath) throws IOException, InterruptedException, Exception {
        System.out.println("Exécution de Newman pour la collection : " + collectionPath);
        List<ParsedResult> currentRunResults = new ArrayList<>();
        boolean processSuccess = false;

        String hardcodedNewmanCommandPath = "C:\\Users\\LENOVO\\AppData\\Roaming\\npm\\newman.cmd";
        System.out.println("Utilisation du chemin Newman hardcodé : " + hardcodedNewmanCommandPath);

        ProcessBuilder processBuilder = new ProcessBuilder(
                hardcodedNewmanCommandPath, "run", collectionPath, "--reporters", "cli"
        );
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            parseNewmanOutput(reader, currentRunResults);
        }

        int exitCode = process.waitFor();
        System.out.println(exitCode == 0 ? "Processus Newman terminé avec succès (code 0)." : "Processus Newman terminé avec des erreurs (code " + exitCode + ").");
        processSuccess = (exitCode == 0);

        updatePersistenceAndMetrics(currentRunResults);

        if (!processSuccess || currentRunResults.isEmpty()) {
            meterRegistry.counter("newman_run_errors_total", Tags.of("reason", processSuccess ? "parsing_failure" : "newman_exit_code")).increment();
        }
        return processSuccess;
    }

    // --- Newman Output Parsing Logic ---
    private void parseNewmanOutput(BufferedReader reader, List<ParsedResult> results) throws Exception {
        String line;
        ParsedResult currentResult = null;
        StringBuilder errorMessageBuilder = new StringBuilder();
        boolean inErrorBlock = false;
        boolean hasNumDotError = false;
        boolean expectingRequestDetails = false;

        Pattern requestLinePattern = Pattern.compile("^\\s*(GET|POST|PUT|DELETE|PATCH|HEAD|OPTIONS)\\s+(https?://[^\\s]+)\\s+\\[(.*?)\\]");
        Pattern statusPattern = Pattern.compile("(\\d{3})\\s+[^,]+,\\s+[^,]+,\\s+(\\d+)ms");

        while ((line = reader.readLine()) != null) {
            // Traitement ligne GET/POST/... attendue
            if (currentResult != null && expectingRequestDetails) {
                expectingRequestDetails = false;
                Matcher requestMatcher = requestLinePattern.matcher(line);
                if (requestMatcher.find()) {
                    // Extraction OK... (code identique à la version précédente)
                    currentResult.httpMethod = requestMatcher.group(1);
                    String fullUrl = requestMatcher.group(2);
                    String statusAndTimePart = requestMatcher.group(3);
                    try {
                        URI uri = new URI(fullUrl);
                        currentResult.path = uri.getPath();
                        if (uri.getQuery() != null) currentResult.path += "?" + uri.getQuery();

                        if (line.contains("[errored]")) { // Erreur Newman
                            currentResult.statusCode = 0; currentResult.responseTime = 0;
                            errorMessageBuilder.append("Request Errored: ").append(line.trim());
                            inErrorBlock = true;
                        } else { // Extraction Status/Temps
                            Matcher statusMatcher = statusPattern.matcher(statusAndTimePart);
                            if (statusMatcher.find()) {
                                currentResult.statusCode = Integer.parseInt(statusMatcher.group(1));
                                currentResult.responseTime = Long.parseLong(statusMatcher.group(2));
                            } else {
                                System.err.println("  Impossible d'extraire status/temps de : " + statusAndTimePart + " pour " + currentResult.apiName + " sur la ligne: " + line);
                                currentResult.statusCode = 0; currentResult.responseTime = 0;
                                errorMessageBuilder.append("Erreur parsing Status/Temps sur ligne attendue: ").append(line.trim());
                                inErrorBlock = true;
                            }
                        }
                    } catch (Exception e) { // Erreur parsing URL
                        System.err.println("  Erreur lors de l'extraction du path/status depuis : " + line + " pour " + currentResult.apiName + " - " + e.getMessage());
                        currentResult.path = "Unknown Path";
                        errorMessageBuilder.append("Erreur parsing URL/Status: ").append(e.getMessage());
                        inErrorBlock = true;
                    }
                } else { // Mauvais format après '└'
                    System.err.println("  Format inattendu pour la ligne de détails de requête attendue: " + line + " pour " + currentResult.apiName);
                    if (currentResult.path == null) currentResult.path = "Unknown Path";
                    errorMessageBuilder.append("Format de ligne de requête (après └) inattendu: ").append(line.trim());
                    inErrorBlock = true;
                }
                // Début nouveau test
            } else if (line.trim().startsWith("□ ") || line.trim().startsWith("→ ")) {
                if (currentResult != null) finalizeAndAddResult(currentResult, hasNumDotError, errorMessageBuilder, results);
                currentResult = new ParsedResult();
                currentResult.apiName = line.substring(line.indexOf(" ")).trim();
                errorMessageBuilder.setLength(0);
                inErrorBlock = false;
                hasNumDotError = false; // Réinitialisation importante
                expectingRequestDetails = false;
                // Ligne '└'
            } else if (currentResult != null && line.trim().startsWith("└")) {
                expectingRequestDetails = true;
                // Ligne assertion passée '√'
            } else if (line.trim().startsWith("√") && currentResult != null) {
                // Pas d'action nécessaire
                // Ligne erreur d'assertion '1.', '2.', etc.
            } else if (currentResult != null) {
                Pattern errorPattern = Pattern.compile("^\\s*\\d+\\.\\s+(.*)");
                Matcher errorMatcher = errorPattern.matcher(line);
                if (errorMatcher.find()) {
                    if (!hasNumDotError) errorMessageBuilder.append("Assertion Errors:\n");
                    errorMessageBuilder.append("  - ").append(errorMatcher.group(1).trim()).append("\n");
                    inErrorBlock = true;
                    hasNumDotError = true; // Indique un échec de script
                } else if (inErrorBlock && line.trim().length() > 0 && !line.trim().matches("^[┌│├└].*")) {
                    errorMessageBuilder.append("    ").append(line.trim()).append("\n");
                }
            }
        } // Fin while

        // Traiter le dernier résultat
        if (currentResult != null) {
            finalizeAndAddResult(currentResult, hasNumDotError, errorMessageBuilder, results);
        }
    }

    // --- Helper pour finaliser et déterminer le succès (Logique Corrigée) ---
    private void finalizeAndAddResult(ParsedResult result, boolean hasNumDotError, StringBuilder errorMsgBuilder, List<ParsedResult> resultsList) {
        if (result.path == null || result.httpMethod == null) {
            System.err.println("Skipping result for API '" + result.apiName + "' due to missing path or method.");
            if (errorMsgBuilder.length() > 0) System.err.println("  Captured error context: " + errorMsgBuilder.toString());
            return;
        }

        // --- LOGIQUE DE SUCCES BASÉE SUR LES ASSERTIONS ---
        if ("Unknown Path".equals(result.path) || errorMsgBuilder.toString().contains("Request Errored")) {
            result.success = false; // Echec fondamental
            if (errorMsgBuilder.length() == 0) errorMsgBuilder.append("Request failed during setup or parsing.");
        } else if (hasNumDotError) {
            result.success = false; // Echec des scripts de test Postman
            // errorMsgBuilder contient déjà les détails
        } else {
            result.success = true; // Aucune erreur d'assertion détectée = Succès du test
            // Nettoyage optionnel des messages d'erreur par défaut si le test réussit
            String currentError = errorMsgBuilder.toString();
            if (currentError.startsWith("Request failed with status code:") || currentError.startsWith("Unexpected status code:")) {
                errorMsgBuilder.setLength(0);
            }
        }
        // --- FIN LOGIQUE ---

        result.errorMessage = errorMsgBuilder.toString().trim();

        System.out.println("Résultat finalisé: Path=" + result.path + ", Method=" + result.httpMethod +
                ", Success=" + result.success + // <= Reflète le succès du script Postman
                ", Status=" + result.statusCode + ", Time=" + result.responseTime + "ms" +
                (result.errorMessage.isEmpty() ? "" : ", Error=Present"));

        resultsList.add(result);
    }

    // --- Update DB and Metrics ---
    private void updatePersistenceAndMetrics(List<ParsedResult> results) {
        LocalDateTime now = LocalDateTime.now();
        final long nowEpochSeconds = now.atZone(ZoneId.systemDefault()).toEpochSecond();

        System.out.println("Traitement de " + results.size() + " résultats parsés.");
        if (results.isEmpty()) {
            System.out.println("Aucun résultat valide n'a été parsé depuis la sortie de Newman.");
        }

        for (ParsedResult result : results) {
            if ("Unknown Path".equals(result.path) || result.httpMethod == null) {
                System.out.println("Ignorer la mise à jour pour un résultat incomplet : " + result.apiName);
                continue;
            }
            updateMetricsForResult(result);
            if (resultRepository != null) {
                persistResult(result, now);
            }
        }

        Gauge.builder("newman_run_last_completion_timestamp_seconds", () -> nowEpochSeconds)
                .description("Unix timestamp of the last completed Newman run processing")
                .register(meterRegistry);
    }


    // --- Metrics Update Logic ---
    private void updateMetricsForResult(ParsedResult result) {
        String metricKey = result.httpMethod + ":" + result.path;
        Tags tags = Tags.of(
                Tag.of("path", result.path),
                Tag.of("method", result.httpMethod),
                Tag.of("api_name", result.apiName != null ? result.apiName : "unknown")
        );
        TestMetricData metricData = latestTestMetrics.computeIfAbsent(metricKey, k -> {
            TestMetricData newData = new TestMetricData();
            Gauge.builder("newman_test_response_time_ms", newData.responseTimeMs::get).tags(tags).description("Response time in milliseconds for the Newman test").register(meterRegistry);
            Gauge.builder("newman_test_status_code", newData.statusCode::get).tags(tags).description("HTTP status code for the Newman test").register(meterRegistry);
            // Description de la métrique de succès mise à jour
            Gauge.builder("newman_test_success", newData.success::get).tags(tags).description("Indicates if the Postman test script passed (1) or failed (0)").register(meterRegistry);
            System.out.println("Registered new metrics for: " + k);
            return newData;
        });
        metricData.responseTimeMs().set(result.responseTime);
        metricData.statusCode().set(result.statusCode); // Stocke le code HTTP réel
        metricData.success().set(result.success ? 1 : 0); // Stocke le succès basé sur les assertions
    }

    // --- Database Persistence Logic ---
    @Transactional
    protected void persistResult(ParsedResult parsedResult, LocalDateTime timestamp) {
        if ("Unknown Path".equals(parsedResult.path)) {
            System.out.println("Ignorer l'enregistrement DB pour Unknown Path");
            return;
        }
        try {
            // Level basé sur le succès des assertions
            String level = !parsedResult.success ? "ERROR" : "INFO";

            ObjectNode metadata = objectMapper.createObjectNode();
            metadata.put("path", parsedResult.path);
            metadata.put("method", parsedResult.httpMethod);
            String metadataString = metadata.toString();

            Optional<ApiMonitoring> existingResult = resultRepository.findByMetadata(metadataString);
            ApiMonitoring dbRecord;
            if (existingResult.isPresent()) {
                dbRecord = existingResult.get();
            } else {
                dbRecord = new ApiMonitoring();
                dbRecord.setPath(parsedResult.path);
                dbRecord.setMetadata(metadataString);
                System.out.println("Création d'un nouvel enregistrement DB pour : " + parsedResult.path + " (" + parsedResult.httpMethod + ")");
            }

            dbRecord.setTemps(timestamp);
            dbRecord.setResponseTime(parsedResult.responseTime);
            dbRecord.setStatusCode(parsedResult.statusCode); // Code HTTP réel
            dbRecord.setErrorMessage(parsedResult.errorMessage.isEmpty() ? null : parsedResult.errorMessage); // Erreurs d'assertion ou autres
            dbRecord.setLevel(level); // INFO/ERROR basé sur le succès du script

            resultRepository.save(dbRecord);

        } catch (Exception e) {
            System.err.println("Erreur lors de l'enregistrement DB pour : " + parsedResult.path + " (" + parsedResult.httpMethod + "): " + e.getMessage());
            e.printStackTrace();
        }
    }

    // --- Helper Class for Parsed Results ---
    private static class ParsedResult {
        String apiName; String path; String httpMethod;
        boolean success = false; // Déterminé par les assertions
        int statusCode = 0;      // Code HTTP réel
        long responseTime = 0;   // Temps réel
        String errorMessage = "";// Erreurs d'assertion ou de parsing
    }

    // --- Entity and Repository Placeholders ---
    // (Assurez-vous qu'ils sont définis ailleurs)
    // public class ApiMonitoring { ... }
    // public interface ApiMonitoringRepository extends JpaRepository<...> { ... }
}*/

// --- Imports ---
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong; // Nécessaire pour le timestamp
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Assurez-vous que ces imports pointent vers vos classes réelles d'entité et repository
// import com.example.api_tierces.model.ApiMonitoring;
// import com.example.api_tierces.repository.ApiMonitoringRepository;

@Service
public class NewmanService { // Nom de classe correct

    // --- Dépendances ---
    private final ApiMonitoringRepository resultRepository; // Assurez-vous que l'import est correct
    private final MeterRegistry meterRegistry;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // --- Stockage état métriques ---
    // Pour les métriques par test (chemin+méthode)
    private final Map<String, TestMetricData> latestTestMetrics = new ConcurrentHashMap<>();
    // Pour le timestamp de la dernière exécution (corrigé pour éviter WARN)
    private final AtomicLong lastCompletionTimestamp = new AtomicLong(0);

    // --- Record pour les données de métrique par test ---
    private record TestMetricData(
            AtomicLong responseTimeMs,
            AtomicInteger statusCode, // Stocke le code HTTP réel
            AtomicInteger success     // 1 = script de test Postman OK, 0 = échec script ou erreur
    ) {
        TestMetricData() {
            this(new AtomicLong(0), new AtomicInteger(0), new AtomicInteger(0));
        }
    }

    // --- Constructeur ---
    public NewmanService(ApiMonitoringRepository resultRepository, MeterRegistry meterRegistry) { // Nom constructeur corrigé
        this.resultRepository = resultRepository;
        this.meterRegistry = meterRegistry;
        System.out.println("NewmanService créé avec le repository : " + resultRepository + " et MeterRegistry: " + meterRegistry);

        // Enregistrement du Gauge de timestamp une seule fois ici
        Gauge.builder("newman_run_last_completion_timestamp_seconds", this.lastCompletionTimestamp::get)
                .description("Unix timestamp du dernier traitement complet des tests Newman")
                .register(meterRegistry);
        System.out.println("Gauge 'newman_run_last_completion_timestamp_seconds' enregistré.");
    }

    // --- Exécution Planifiée Newman ---
    @Scheduled(cron = "${newman.schedule.cron:* * * * * *}")
    public void runScheduledNewmanTests() {
        System.out.println("Début de l'exécution planifiée des tests Newman.");
        String defaultCollectionResourceName = "postman_collection.json"; // Nom du fichier dans resources

        try {
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource(defaultCollectionResourceName).toURI());
            System.out.println("Utilisation de la collection planifiée : " + file.getAbsolutePath());
            // Appel de la logique commune
            executeNewmanAndProcessResults(file.getAbsolutePath());
        } catch (URISyntaxException | NullPointerException e) {
            System.err.println("Erreur critique: Impossible de trouver ou charger le fichier de collection par défaut '" + defaultCollectionResourceName + "' dans les ressources: " + e.getMessage());
            e.printStackTrace();
            meterRegistry.counter("newman_run_errors_total", Tags.of("reason", "resource_not_found")).increment();
        } catch (Exception e) {
            System.err.println("Erreur inattendue lors de la préparation de l'exécution planifiée: " + e.getMessage());
            e.printStackTrace();
            meterRegistry.counter("newman_run_errors_total", Tags.of("reason", "setup_error")).increment();
        }
        System.out.println("Fin de l'exécution planifiée des tests Newman.");
    }

    // --- Traitement Manuel depuis JSON (Optionnel) ---
    public String processPostmanCollection(String postmanCollectionJson) {
        System.out.println("Début du traitement manuel de la collection Postman fournie.");
        Path tempFilePath = null;
        try {
            tempFilePath = Files.createTempFile("postman_manual_", ".json");
            System.out.println("Fichier temporaire créé : " + tempFilePath.toString());
            Files.writeString(tempFilePath, postmanCollectionJson, StandardOpenOption.WRITE);
            boolean processSuccess = executeNewmanAndProcessResults(tempFilePath.toAbsolutePath().toString());
            // Le message de retour indique seulement si le *processus* Newman a réussi,
            // les détails des tests sont dans les logs/métriques/BDD.
            return processSuccess ? "Processus Newman terminé avec succès." : "Processus Newman terminé avec erreurs (voir logs).";
        } catch (IOException e) {
            System.err.println("Erreur I/O lors de la création/écriture du fichier temporaire: " + e.getMessage());
            e.printStackTrace();
            meterRegistry.counter("newman_run_errors_total", Tags.of("reason", "temp_file_io")).increment();
            return "Erreur I/O lors de la préparation du traitement : " + e.getMessage();
        } catch (Exception e) {
            System.err.println("Erreur inattendue lors du traitement manuel de la collection: " + e.getMessage());
            e.printStackTrace();
            meterRegistry.counter("newman_run_errors_total", Tags.of("reason", "unknown_manual")).increment();
            return "Erreur inattendue lors du traitement : " + e.getMessage();
        } finally {
            if (tempFilePath != null) {
                try { Files.deleteIfExists(tempFilePath); System.out.println("Fichier temporaire supprimé : " + tempFilePath.toString()); }
                catch (IOException e) { System.err.println("Attention : Impossible de supprimer le fichier temporaire : " + tempFilePath + " - " + e.getMessage()); }
            }
            System.out.println("Fin du traitement manuel de la collection Postman fournie.");
        }
    }

    // --- Logique Commune Exécution Newman & Traitement Résultats ---
    private boolean executeNewmanAndProcessResults(String collectionPath) throws IOException, InterruptedException, Exception {
        System.out.println("Exécution de Newman pour la collection : " + collectionPath);
        List<ParsedResult> currentRunResults = new ArrayList<>();
        boolean processSuccess = false; // Succès du *processus* Newman

        // Chemin Newman codé en dur
        String hardcodedNewmanCommandPath = "C:\\Users\\LENOVO\\AppData\\Roaming\\npm\\newman.cmd";
        System.out.println("Utilisation du chemin Newman hardcodé : " + hardcodedNewmanCommandPath);

        // Configuration et lancement du processus Newman
        ProcessBuilder processBuilder = new ProcessBuilder(
                hardcodedNewmanCommandPath, "run", collectionPath, "--reporters", "cli"
        );
        processBuilder.redirectErrorStream(true); // Combine stdout et stderr
        Process process = processBuilder.start();

        // Lecture et parsing de la sortie Newman
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            parseNewmanOutput(reader, currentRunResults); // Remplit currentRunResults
        } // Le reader est fermé automatiquement

        // Attente de la fin du processus Newman
        int exitCode = process.waitFor();
        System.out.println(exitCode == 0 ? "Processus Newman terminé avec succès (code 0)." : "Processus Newman terminé avec des erreurs (code " + exitCode + ").");
        processSuccess = (exitCode == 0); // exitCode 0 = Newman a pu s'exécuter sans crash

        // Mise à jour BDD et Métriques basées sur les résultats parsés
        updatePersistenceAndMetrics(currentRunResults);

        // Log erreur si le processus Newman échoue ou si le parsing échoue complètement
        if (!processSuccess || currentRunResults.isEmpty() && exitCode == 0) { // Ajout condition parsing vide même si exit 0
            meterRegistry.counter("newman_run_errors_total", Tags.of("reason", processSuccess ? "parsing_failure" : "newman_exit_code")).increment();
        }

        return processSuccess; // Retourne si le processus Newman lui-même a réussi
    }

    // --- Logique de Parsing de la Sortie Newman ---
    /*private void parseNewmanOutput(BufferedReader reader, List<ParsedResult> results) throws Exception {
        String line;
        ParsedResult currentResult = null;
        StringBuilder errorMessageBuilder = new StringBuilder();
        boolean inErrorBlock = false; // Pour capturer le contexte d'erreur
        boolean hasNumDotError = false; // Indicateur clé : erreur d'assertion numérotée trouvée
        boolean expectingRequestDetails = false; // Indicateur état : attend la ligne GET/POST...

        // Patterns Regex pour extraire les infos
        Pattern requestLinePattern = Pattern.compile("^\\s*(GET|POST|PUT|DELETE|PATCH|HEAD|OPTIONS)\\s+(https?://[^\\s]+)\\s+\\[(.*?)\\]");
        Pattern statusPattern = Pattern.compile("(\\d{3})\\s+[^,]+,\\s+[^,]+,\\s+(\\d+)ms");

        while ((line = reader.readLine()) != null) {
            // System.out.println("Ligne lue : " + line); // Décommenter pour débogage très détaillé

            // Cas 1: On attendait la ligne avec GET/POST... après une ligne '└'
            if (currentResult != null && expectingRequestDetails) {
                expectingRequestDetails = false; // On traite cette attente
                Matcher requestMatcher = requestLinePattern.matcher(line);
                if (requestMatcher.find()) { // Format attendu trouvé
                    currentResult.httpMethod = requestMatcher.group(1);
                    String fullUrl = requestMatcher.group(2);
                    String statusAndTimePart = requestMatcher.group(3); // Partie entre crochets [...]
                    try {
                        // Extraction Path depuis URL
                        URI uri = new URI(fullUrl);
                        currentResult.path = uri.getPath();
                        if (uri.getQuery() != null) currentResult.path += "?" + uri.getQuery();

                        // Vérification Erreur Newman explicite ou extraction Status/Temps
                        if (line.contains("[errored]")) {
                            currentResult.statusCode = 0; currentResult.responseTime = 0;
                            errorMessageBuilder.append("Request Errored: ").append(line.trim());
                            inErrorBlock = true;
                        } else {
                            Matcher statusMatcher = statusPattern.matcher(statusAndTimePart);
                            if (statusMatcher.find()) { // Status/Temps trouvés
                                currentResult.statusCode = Integer.parseInt(statusMatcher.group(1));
                                currentResult.responseTime = Long.parseLong(statusMatcher.group(2));
                            } else { // Échec extraction status/temps
                                System.err.println("  Impossible d'extraire status/temps de : " + statusAndTimePart + " pour " + currentResult.apiName + " sur la ligne: " + line);
                                currentResult.statusCode = 0; currentResult.responseTime = 0;
                                errorMessageBuilder.append("Erreur parsing Status/Temps sur ligne attendue: ").append(line.trim());
                                inErrorBlock = true;
                            }
                        }
                    } catch (Exception e) { // Erreur extraction URL/Path
                        System.err.println("  Erreur lors de l'extraction du path/status depuis : " + line + " pour " + currentResult.apiName + " - " + e.getMessage());
                        currentResult.path = "Unknown Path";
                        errorMessageBuilder.append("Erreur parsing URL/Status: ").append(e.getMessage());
                        inErrorBlock = true;
                    }
                } else { // Mauvais format pour la ligne après '└'
                    System.err.println("  Format inattendu pour la ligne de détails de requête attendue: " + line + " pour " + currentResult.apiName);
                    if (currentResult.path == null) currentResult.path = "Unknown Path";
                    errorMessageBuilder.append("Format de ligne de requête (après └) inattendu: ").append(line.trim());
                    inErrorBlock = true;
                }
                // Cas 2: Début d'un nouveau test (ligne '□' ou '→')
            } else if (line.trim().startsWith("□ ") || line.trim().startsWith("→ ")) {
                // Finaliser le test précédent s'il existe
                if (currentResult != null) {
                    finalizeAndAddResult(currentResult, hasNumDotError, errorMessageBuilder, results);
                }
                // Initialiser pour le nouveau test
                currentResult = new ParsedResult();
                currentResult.apiName = line.substring(line.indexOf(" ")).trim();
                errorMessageBuilder.setLength(0); // Vider buffer erreur
                inErrorBlock = false;
                hasNumDotError = false; // Réinitialiser indicateur d'erreur d'assertion
                expectingRequestDetails = false;
                // Cas 3: Ligne '└', on s'attend aux détails sur la ligne suivante
            } else if (currentResult != null && line.trim().startsWith("└")) {
                expectingRequestDetails = true;
                // Cas 4: Ligne assertion passée '√'
            } else if (line.trim().startsWith("√") && currentResult != null) {
                // Aucune action spécifique nécessaire, l'absence de hasNumDotError suffit
                // Cas 5: Autres lignes pendant un test en cours
            } else if (currentResult != null) {
                // Recherche d'erreurs d'assertion numérotées ('1. ...', '2. ...')
                Pattern errorPattern = Pattern.compile("^\\s*\\d+\\.\\s+(.*)");
                Matcher errorMatcher = errorPattern.matcher(line);
                if (errorMatcher.find()) { // Erreur d'assertion trouvée
                    if (!hasNumDotError) { // Première erreur pour ce test
                        errorMessageBuilder.append("Assertion Errors:\n");
                    }
                    errorMessageBuilder.append("  - ").append(errorMatcher.group(1).trim()).append("\n");
                    inErrorBlock = true;
                    hasNumDotError = true; // !! Marque l'échec du script !!
                } else if (inErrorBlock && line.trim().length() > 0 && !line.trim().matches("^[┌│├└].*")) {
                    // Si on est dans un bloc d'erreur, capturer les lignes suivantes qui ne sont pas de la déco
                    errorMessageBuilder.append("    ").append(line.trim()).append("\n");
                }
            }
        } // Fin de la boucle while (lecture des lignes)

        // Traiter le tout dernier résultat après la fin de la lecture
        if (currentResult != null) {
            finalizeAndAddResult(currentResult, hasNumDotError, errorMessageBuilder, results);
        }
    }*/
    // --- Logique de Parsing de la Sortie Newman (Refondue pour Structure Dossier/Requête) ---
    /*maye5edhch en considération seconde*/
    /*private void parseNewmanOutput(BufferedReader reader, List<ParsedResult> results) throws Exception {
        String line;
        ParsedResult currentResult = null; // Le résultat en cours de construction pour UNE requête (après un '└')
        StringBuilder errorMessageBuilder = new StringBuilder();
        boolean inErrorBlock = false;
        boolean hasNumDotError = false;
        // boolean expectingRequestDetails = false; // On n'attend plus après '└', on cherche directement après

        // Patterns Regex (inchangés)
        Pattern requestLinePattern = Pattern.compile("^\\s*(GET|POST|PUT|DELETE|PATCH|HEAD|OPTIONS)\\s+(https?://[^\\s]+)\\s+\\[(.*?)\\]");
        Pattern statusPattern = Pattern.compile("(\\d{3})\\s+[^,]+,\\s+[^,]+,\\s+(\\d+)ms");
        String currentFolderName = "Global"; // Garder trace du dossier courant (optionnel mais utile pour logs)

        while ((line = reader.readLine()) != null) {
            // System.out.println("DEBUG Ligne: " + line);

            // --- Marqueur de Dossier ---
            if (line.trim().startsWith("□ ") || line.trim().startsWith("→ ")) {
                // Finaliser la *dernière requête* du dossier précédent si elle existe
                if (currentResult != null) {
                    finalizeAndAddResult(currentResult, hasNumDotError, errorMessageBuilder, results);
                    currentResult = null; // Prêt pour le prochain '└'
                }
                // Mettre à jour le nom du dossier courant
                currentFolderName = line.substring(line.indexOf(" ")).trim();
                // System.out.println("DEBUG: Entrée Dossier: " + currentFolderName);
                // Réinitialiser les états d'erreur pour le nouveau dossier/contexte
                errorMessageBuilder.setLength(0);
                inErrorBlock = false;
                hasNumDotError = false;
                continue; // Passer à la ligne suivante
            }

            // --- Marqueur de Requête Exécutée ---
            if (line.trim().startsWith("└ ")) {
                // Finaliser la requête *précédente* (si elle existait)
                if (currentResult != null) {
                    finalizeAndAddResult(currentResult, hasNumDotError, errorMessageBuilder, results);
                }
                // Initialiser pour CETTE requête
                currentResult = new ParsedResult();
                currentResult.apiName = line.substring(line.indexOf(" ")).trim(); // Nom vient de la ligne '└'
                errorMessageBuilder.setLength(0);
                inErrorBlock = false;
                hasNumDotError = false;
                // System.out.println("DEBUG: Début Requête: " + currentResult.apiName + " (Dossier: " + currentFolderName + ")");
                continue; // Passer à la ligne suivante, on attend GET/POST...
            }

            // Si on n'a pas encore vu de '└' pour initialiser, ignorer la ligne
            if (currentResult == null) {
                // Ignorer les lignes avant la première requête (└) ou entre dossiers
                continue;
            }

            // --- Capture de la ligne de détails GET/POST... ---
            // On suppose qu'elle suit directement ou indirectement la ligne '└'
            // (On ne la cherche que si méthode et path sont encore null)
            if (currentResult.httpMethod == null && currentResult.path == null) {
                Matcher requestMatcher = requestLinePattern.matcher(line);
                if (requestMatcher.find()) {
                    // System.out.println("DEBUG: Ligne détails trouvée pour " + currentResult.apiName + ": " + line);
                    currentResult.httpMethod = requestMatcher.group(1);
                    String fullUrl = requestMatcher.group(2);
                    String statusAndTimePart = requestMatcher.group(3);
                    try {
                        URI uri = new URI(fullUrl); // << DOIT ETRE CORRIGE EN AMONT SI PLACEHOLDERS RESTANTS
                        currentResult.path = uri.getPath();
                        if (uri.getQuery() != null) currentResult.path += "?" + uri.getQuery();

                        if (line.contains("[errored]")) {
                            currentResult.statusCode = 0; currentResult.responseTime = 0;
                            errorMessageBuilder.append("Request Errored: ").append(line.trim());
                            inErrorBlock = true;
                        } else {
                            Matcher statusMatcher = statusPattern.matcher(statusAndTimePart);
                            if (statusMatcher.find()) {
                                currentResult.statusCode = Integer.parseInt(statusMatcher.group(1));
                                currentResult.responseTime = Long.parseLong(statusMatcher.group(2));
                            } else {
                                System.err.println("  Impossible d'extraire status/temps de : " + statusAndTimePart + " pour " + currentResult.apiName + " sur ligne: " + line);
                                currentResult.statusCode = 0; currentResult.responseTime = 0;
                                errorMessageBuilder.append("Erreur parsing Status/Temps: ").append(line.trim());
                                inErrorBlock = true;
                            }
                        }
                    } catch (Exception e) { // Principalement URISyntaxException si URL invalide
                        System.err.println("  Erreur parsing URL/Status depuis : " + line + " pour " + currentResult.apiName + " - " + e.getMessage());
                        currentResult.path = "Unknown Path"; // Marquer comme invalide
                        // currentResult.httpMethod est déjà setté depuis le matcher
                        currentResult.statusCode = 0; currentResult.responseTime = 0;
                        errorMessageBuilder.append("Erreur parsing URL/Status (").append(e.getClass().getSimpleName()).append("): ").append(e.getMessage());
                        inErrorBlock = true;
                    }
                    continue; // Ligne GET/POST traitée, passer à la suivante
                }
                // Si ce n'est pas la ligne GET/POST attendue, on continue, elle arrivera peut-être plus tard
                // (ou on la manquera si la structure change trop)
            }

            // --- Capture des Assertions et Erreurs (pour la requête courante) ---
            if (line.trim().startsWith("√")) {
                continue; // Ignorer assertions réussies
            }

            Pattern errorPattern = Pattern.compile("^\\s*\\d+\\.\\s+(.*)");
            Matcher errorMatcher = errorPattern.matcher(line);
            if (errorMatcher.find()) { // Erreur d'assertion numérotée
                if (!hasNumDotError) { errorMessageBuilder.append("Assertion Errors:\n"); }
                errorMessageBuilder.append("  - ").append(errorMatcher.group(1).trim()).append("\n");
                inErrorBlock = true;
                hasNumDotError = true; // Marqueur d'échec du script
                continue;
            }

            // Capturer contexte d'erreur (lignes non vides, non déco, non assertion √)
            if (inErrorBlock && line.trim().length() > 0 && !line.trim().matches("^[┌│├└√□→].*")) {
                errorMessageBuilder.append("    ").append(line.trim()).append("\n");
            }

        } // Fin while

        // --- Traitement du TOUT dernier résultat ---
        if (currentResult != null) {
            // Vérifier si les détails essentiels ont été capturés pour le dernier
            if (currentResult.httpMethod == null || currentResult.path == null) {
                System.err.println("ERREUR PARSING: Fin de la sortie atteinte avant d'avoir les détails complets pour la dernière requête: " + currentResult.apiName);
                currentResult.path = currentResult.path == null ? "Unknown Path (Incomplete End)" : currentResult.path;
                errorMessageBuilder.append("Parsing Error: End of stream reached before request details line found.");
                inErrorBlock = true; // Assurer marquage erreur
            }
            finalizeAndAddResult(currentResult, hasNumDotError, errorMessageBuilder, results);
        }
    }*/ // Fin parseNewmanOutput

    // --- Logique de Parsing de la Sortie Newman (CORRIGÉE pour Temps ms/s ET Structure) ---
    private void parseNewmanOutput(BufferedReader reader, List<ParsedResult> results) throws Exception {
        String line;
        ParsedResult currentResult = null; // Le résultat en cours de construction pour UNE requête (après un '└')
        StringBuilder errorMessageBuilder = new StringBuilder();
        boolean inErrorBlock = false;
        boolean hasNumDotError = false;

        // Pattern pour la ligne de requête principale (inchangé)
        Pattern requestLinePattern = Pattern.compile("^\\s*(GET|POST|PUT|DELETE|PATCH|HEAD|OPTIONS)\\s+(https?://[^\\s]+)\\s+\\[(.*?)\\]");

        // *** PATTERN CORRIGÉ pour Status/Temps (gère ms et s) ***
        // Groupe 1: Status Code (\d{3})
        // Groupe 2: Valeur Temps ([\d.]+) - chiffres ou point
        // Groupe 3: Unité (ms|s) - ms ou s
        Pattern statusPattern = Pattern.compile("(\\d{3})\\s+[^,]+,\\s+[^,]+,\\s+([\\d.]+)(ms|s)");
        // *********************************************************

        String currentFolderName = "Global";

        while ((line = reader.readLine()) != null) {
            // System.out.println("DEBUG Ligne: " + line);

            // --- Marqueur de Dossier ---
            if (line.trim().startsWith("□ ") || line.trim().startsWith("→ ")) {
                if (currentResult != null) {
                    finalizeAndAddResult(currentResult, hasNumDotError, errorMessageBuilder, results);
                    currentResult = null;
                }
                currentFolderName = line.substring(line.indexOf(" ")).trim();
                errorMessageBuilder.setLength(0); inErrorBlock = false; hasNumDotError = false;
                continue;
            }

            // --- Marqueur de Requête Exécutée ---
            if (line.trim().startsWith("└ ")) {
                if (currentResult != null) {
                    finalizeAndAddResult(currentResult, hasNumDotError, errorMessageBuilder, results);
                }
                currentResult = new ParsedResult();
                currentResult.apiName = line.substring(line.indexOf(" ")).trim();
                errorMessageBuilder.setLength(0); inErrorBlock = false; hasNumDotError = false;
                continue;
            }

            if (currentResult == null) continue;

            // --- Capture de la ligne de détails GET/POST... ---
            if (currentResult.httpMethod == null && currentResult.path == null) {
                Matcher requestMatcher = requestLinePattern.matcher(line);
                if (requestMatcher.find()) {
                    currentResult.httpMethod = requestMatcher.group(1);
                    String fullUrl = requestMatcher.group(2);
                    String statusAndTimePart = requestMatcher.group(3);

                    // --- Workaround Optionnel pour URL invalide ---
                    String sanitizedUrl = fullUrl;
                    // Exemple : Remplacer <integer> si présent (adaptez si nécessaire)
                    String placeholder = "<integer>";
                    if (sanitizedUrl.contains(placeholder)) {
                        String replacement = "3"; // Mettre une valeur numérique valide
                        sanitizedUrl = sanitizedUrl.replace(placeholder, replacement);
                        System.out.println("WARN: URL nettoyée : '" + fullUrl + "' -> '" + sanitizedUrl + "'");
                    }
                    // --- Fin Workaround ---

                    try {
                        // Utiliser l'URL (potentiellement nettoyée)
                        URI uri = new URI(sanitizedUrl);
                        currentResult.path = uri.getPath();
                        if (uri.getQuery() != null) currentResult.path += "?" + uri.getQuery();

                        if (line.contains("[errored]")) {
                            currentResult.statusCode = 0; currentResult.responseTime = 0;
                            errorMessageBuilder.append("Request Errored: ").append(line.trim());
                            inErrorBlock = true;
                        } else {
                            // *** Utilisation du PATTERN CORRIGÉ Status/Temps ***
                            Matcher statusMatcher = statusPattern.matcher(statusAndTimePart);
                            if (statusMatcher.find()) {
                                try {
                                    currentResult.statusCode = Integer.parseInt(statusMatcher.group(1)); // Status

                                    String timeValueStr = statusMatcher.group(2); // Valeur temps (String)
                                    String timeUnit = statusMatcher.group(3);     // Unité (String)

                                    double timeValue = Double.parseDouble(timeValueStr); // Parser en double

                                    if ("s".equalsIgnoreCase(timeUnit)) {
                                        timeValue = timeValue * 1000; // Convertir secondes en ms
                                    }

                                    currentResult.responseTime = Math.round(timeValue); // Arrondir et stocker

                                } catch (NumberFormatException nfe) {
                                    System.err.println("  Erreur format numérique Status/Temps : '" + statusAndTimePart + "' pour " + currentResult.apiName + " - " + nfe.getMessage());
                                    currentResult.statusCode = 0; currentResult.responseTime = 0;
                                    errorMessageBuilder.append("Erreur format numérique Status/Temps: ").append(statusAndTimePart);
                                    inErrorBlock = true;
                                }
                            } else { // Le pattern corrigé n'a pas matché
                                System.err.println("  Impossible d'extraire status/temps (pattern corrigé) de : '" + statusAndTimePart + "' pour " + currentResult.apiName + " sur ligne: " + line);
                                currentResult.statusCode = 0; currentResult.responseTime = 0;
                                errorMessageBuilder.append("Erreur parsing Status/Temps (Pattern): ").append(statusAndTimePart);
                                inErrorBlock = true;
                            }
                            // *****************************************************
                        }
                    } catch (URISyntaxException e) { // Erreur si l'URL (même nettoyée) reste invalide
                        System.err.println("  Erreur parsing URL/Status depuis : " + line + " pour " + currentResult.apiName + " - " + e.getMessage());
                        currentResult.path = "Unknown Path";
                        currentResult.statusCode = 0; currentResult.responseTime = 0;
                        errorMessageBuilder.append("Erreur parsing URL/Status (").append(e.getClass().getSimpleName()).append("): ").append(e.getMessage());
                        inErrorBlock = true;
                    } catch (Exception e) { // Autres erreurs
                        System.err.println("  Erreur inattendue lors du traitement de la ligne de détails : " + line + " pour " + currentResult.apiName + " - " + e.getMessage());
                        currentResult.path = "Unknown Path";
                        currentResult.statusCode = 0; currentResult.responseTime = 0;
                        errorMessageBuilder.append("Erreur Générale Traitement Détails: ").append(e.getMessage());
                        inErrorBlock = true;
                    }
                    continue; // Ligne GET/POST traitée
                }
            }

            // --- Capture des Assertions et Erreurs (inchangé) ---
            if (line.trim().startsWith("√")) {
                continue;
            }
            Pattern errorPattern = Pattern.compile("^\\s*\\d+\\.\\s+(.*)");
            Matcher errorMatcher = errorPattern.matcher(line);
            if (errorMatcher.find()) {
                if (!hasNumDotError) { errorMessageBuilder.append("Assertion Errors:\n"); }
                errorMessageBuilder.append("  - ").append(errorMatcher.group(1).trim()).append("\n");
                inErrorBlock = true; hasNumDotError = true;
                continue;
            }
            if (inErrorBlock && line.trim().length() > 0 && !line.trim().matches("^[┌│├└√□→].*")) {
                errorMessageBuilder.append("    ").append(line.trim()).append("\n");
            }

        } // Fin while

        // --- Traitement du TOUT dernier résultat (inchangé) ---
        if (currentResult != null) {
            if (currentResult.httpMethod == null || currentResult.path == null) {
                System.err.println("ERREUR PARSING: Fin de la sortie atteinte avant d'avoir les détails complets pour la dernière requête: " + currentResult.apiName);
                currentResult.path = currentResult.path == null ? "Unknown Path (Incomplete End)" : currentResult.path;
                errorMessageBuilder.append("Parsing Error: End of stream reached before request details line found.");
                inErrorBlock = true;
            }
            finalizeAndAddResult(currentResult, hasNumDotError, errorMessageBuilder, results);
        }
    } // Fin parseNewmanOutput
    // --- Aide pour Finaliser Résultat & Déterminer Succès (Logique Corrigée) ---
    private void finalizeAndAddResult(ParsedResult result, boolean hasNumDotError, StringBuilder errorMsgBuilder, List<ParsedResult> resultsList) {
        // Vérification initiale si les infos de base sont présentes
        if (result.path == null || result.httpMethod == null) {
            System.err.println("Skipping result for API '" + result.apiName + "' due to missing path or method.");
            if (errorMsgBuilder.length() > 0) System.err.println("  Captured error context: " + errorMsgBuilder.toString());
            return; // Ne pas traiter ce résultat incomplet
        }

        // --- LOGIQUE DE SUCCES BASÉE SUR LES ASSERTIONS POSTMAN ---
        // Le test est considéré comme un ECHEC si :
        // 1. Le chemin n'a pas pu être déterminé (`Unknown Path`).
        // 2. Newman a marqué la requête comme `[errored]`.
        // 3. Des erreurs d'assertion numérotées (`hasNumDotError`) ont été détectées.
        if ("Unknown Path".equals(result.path) || errorMsgBuilder.toString().contains("Request Errored:") || hasNumDotError) {
            result.success = false;
            // Ajouter un message générique si aucun message spécifique n'a été capturé
            if (errorMsgBuilder.length() == 0) {
                if(hasNumDotError) errorMsgBuilder.append("Assertion failed, details not captured.");
                else errorMsgBuilder.append("Request failed during setup, parsing or due to [errored] tag.");
            }
        }
        // Sinon (aucune des conditions d'échec ci-dessus), le test est considéré comme un SUCCES
        else {
            result.success = true;
            // Optionnel: Nettoyer les messages d'erreur générés par défaut si le test a réussi ses assertions
            String currentError = errorMsgBuilder.toString();
            if (currentError.startsWith("Request failed with status code:") || currentError.startsWith("Unexpected status code:")) {
                errorMsgBuilder.setLength(0); // On efface car le script a réussi
            }
        }
        // --- FIN LOGIQUE ---

        result.errorMessage = errorMsgBuilder.toString().trim();

        // Log final pour ce test
        System.out.println("Résultat finalisé: Path=" + result.path + ", Method=" + result.httpMethod +
                ", Success=" + result.success + // <= Reflète le succès du script Postman
                ", Status=" + result.statusCode + ", Time=" + result.responseTime + "ms" +
                (result.errorMessage.isEmpty() ? "" : ", Error=Present")); // Indique si un msg d'erreur existe

        // Ajout à la liste des résultats de cette exécution
        resultsList.add(result);
    }

    // --- Mise à Jour Base de Données & Métriques ---
    private void updatePersistenceAndMetrics(List<ParsedResult> results) {
        LocalDateTime now = LocalDateTime.now();
        final long nowEpochSeconds = now.atZone(ZoneId.systemDefault()).toEpochSecond();

        System.out.println("Traitement de " + results.size() + " résultats parsés.");
        if (results.isEmpty()) {
            System.out.println("Aucun résultat valide n'a été parsé depuis la sortie de Newman.");
            // Si aucun résultat n'est parsé, on met quand même à jour le timestamp
            // pour indiquer que la tâche a tourné.
        }

        // Traitement de chaque résultat parsé
        for (ParsedResult result : results) {
            // Ignorer les résultats incomplets (sécurité supplémentaire)
            if ("Unknown Path".equals(result.path) || result.httpMethod == null) {
                System.out.println("Ignorer la mise à jour pour un résultat incomplet : " + result.apiName);
                continue;
            }
            // Mettre à jour les métriques Prometheus pour ce test
            updateMetricsForResult(result);
            // Persister le résultat en base de données si le repository est configuré
            if (resultRepository != null) {
                persistResult(result, now);
            }
        }

        // Mise à jour de la valeur du timestamp pour le Gauge enregistré dans le constructeur
        this.lastCompletionTimestamp.set(nowEpochSeconds);
        // L'enregistrement du Gauge est fait dans le constructeur, pas ici.
    }


    // --- Logique Mise à Jour Métriques par Test ---
    private void updateMetricsForResult(ParsedResult result) {
        String metricKey = result.httpMethod + ":" + result.path; // Clé unique pour la map en mémoire
        // Tags pour les labels Prometheus
        Tags tags = Tags.of(
                Tag.of("path", result.path),
                Tag.of("method", result.httpMethod),
                Tag.of("api_name", result.apiName != null ? result.apiName : "unknown") // Nom de l'API depuis la collection
        );

        // Récupère ou crée l'objet contenant les AtomicLong/Int pour ce test
        TestMetricData metricData = latestTestMetrics.computeIfAbsent(metricKey, k -> {
            // Enregistre les Gauges SEULEMENT lors de la première rencontre de ce test
            TestMetricData newData = new TestMetricData();
            // Gauge pour le temps de réponse
            Gauge.builder("newman_test_response_time_ms", newData.responseTimeMs::get).tags(tags).description("Response time in milliseconds for the Newman test").register(meterRegistry);
            // Gauge pour le code statut HTTP réel
            Gauge.builder("newman_test_status_code", newData.statusCode::get).tags(tags).description("HTTP status code for the Newman test").register(meterRegistry);
            // Gauge pour le succès basé sur les scripts Postman
            Gauge.builder("newman_test_success", newData.success::get).tags(tags).description("Indicates if the Postman test script passed (1) or failed (0)").register(meterRegistry);
            System.out.println("Registered new metrics for: " + k); // Log seulement à la création
            return newData;
        });

        // Met à jour les valeurs atomiques. Le Gauge lira ces nouvelles valeurs.
        metricData.responseTimeMs().set(result.responseTime);
        metricData.statusCode().set(result.statusCode); // Code HTTP réel
        metricData.success().set(result.success ? 1 : 0); // Succès basé sur assertions
    }

    // --- Logique Persistance Base de Données ---
    @Transactional // Assure la gestion transactionnelle si JPA/Hibernate est utilisé
    protected void persistResult(ParsedResult parsedResult, LocalDateTime timestamp) {
        // Ignorer les chemins inconnus
        if ("Unknown Path".equals(parsedResult.path)) {
            System.out.println("Ignorer l'enregistrement DB pour Unknown Path");
            return;
        }
        try {
            // Déterminer le niveau de log basé sur le succès des scripts Postman
            String level = !parsedResult.success ? "ERROR" : "INFO";

            // Créer la chaîne JSON pour les métadonnées (utilisée comme clé unique)
            ObjectNode metadata = objectMapper.createObjectNode();
            metadata.put("path", parsedResult.path);
            metadata.put("method", parsedResult.httpMethod);
            String metadataString = metadata.toString();

            // Chercher un enregistrement existant basé sur les métadonnées
            // Assurez-vous que ApiMonitoringRepository a cette méthode et que metadata est unique en BDD
            Optional<ApiMonitoring> existingResult = resultRepository.findByMetadata(metadataString);

            ApiMonitoring dbRecord; // L'objet entité à sauvegarder
            if (existingResult.isPresent()) {
                // Mise à jour d'un enregistrement existant
                dbRecord = existingResult.get();
                // System.out.println("Mise à jour de l'enregistrement DB pour : " + parsedResult.path + " (" + parsedResult.httpMethod + ")"); // Log optionnel
            } else {
                // Création d'un nouvel enregistrement
                dbRecord = new ApiMonitoring();
                dbRecord.setPath(parsedResult.path);       // Chemin de l'API
                dbRecord.setMetadata(metadataString);      // Clé unique metadata
                // Assurez-vous que votre entité ApiMonitoring a un champ 'method' si vous voulez le stocker séparément
                // Si non, il est déjà dans metadata. Si oui, ajoutez : dbRecord.setMethod(parsedResult.httpMethod);
                System.out.println("Création d'un nouvel enregistrement DB pour : " + parsedResult.path + " (" + parsedResult.httpMethod + ")");
            }

            // Mettre à jour les champs communs (pour création et mise à jour)
            dbRecord.setTemps(timestamp);                              // Heure du test
            dbRecord.setResponseTime(parsedResult.responseTime);       // Temps de réponse réel
            dbRecord.setStatusCode(parsedResult.statusCode);           // Code HTTP réel
            dbRecord.setErrorMessage(parsedResult.errorMessage.isEmpty() ? null : parsedResult.errorMessage); // Msg erreur (assertions...)
            dbRecord.setLevel(level);                                  // INFO/ERROR basé sur succès script

            // Sauvegarder l'entité (crée ou met à jour)
            resultRepository.save(dbRecord);

        } catch (Exception e) {
            // Gestion des erreurs lors de l'interaction avec la BDD
            System.err.println("Erreur lors de l'enregistrement DB pour : " + parsedResult.path + " (" + parsedResult.httpMethod + "): " + e.getMessage());
            e.printStackTrace(); // Afficher la trace complète pour le débogage
        }
    }

    // --- Classe d'Aide pour Résultats Parsés ---
    // Structure interne pour stocker les données extraites pendant le parsing
    private static class ParsedResult {
        String apiName;         // Nom de la requête dans Postman
        String path;            // Chemin de l'API (/api/users)
        String httpMethod;      // GET, POST, etc.
        boolean success = false;// Succès basé sur les scripts Postman
        int statusCode = 0;     // Code HTTP réel retourné
        long responseTime = 0;  // Temps de réponse réel en ms
        String errorMessage = "";// Erreurs capturées (assertions, parsing...)
    }

    // --- Placeholders Entité & Repository ---
    // Vous devez avoir défini vos classes ApiMonitoring et ApiMonitoringRepository ailleurs
    /*
    // Exemple ApiMonitoring.java (dans un package model/entity)
    import jakarta.persistence.*; // ou javax.persistence.*
    import java.time.LocalDateTime;
    @Entity
    @Table(name = "api_monitoring") // Nom de votre table
    public class ApiMonitoring {
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
        private String path;
        private LocalDateTime temps;
        private long responseTime;
        private int statusCode;
        @Column(length = 2048) // Augmenter si les messages d'erreur sont longs
        private String errorMessage;
        private String level;
        @Column(unique = true, length = 512) // Assurez l'unicité et taille suffisante pour metadata
        private String metadata;
        // --- GETTERS ET SETTERS pour tous les champs ---
    }

    // Exemple ApiMonitoringRepository.java (dans un package repository/persistence)
    import org.springframework.data.jpa.repository.JpaRepository;
    import java.util.Optional;
    public interface ApiMonitoringRepository extends JpaRepository<ApiMonitoring, Long> {
        // Méthode pour trouver par la clé metadata
        Optional<ApiMonitoring> findByMetadata(String metadata);
    }
    */
}