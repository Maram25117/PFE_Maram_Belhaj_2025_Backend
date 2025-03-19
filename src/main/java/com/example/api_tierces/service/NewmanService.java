package com.example.api_tierces.service;

import com.example.api_tierces.model.ApiMonitoring;
import com.example.api_tierces.repository.ApiMonitoringRepository;
import com.fasterxml.jackson.databind.ObjectMapper; //Gère la conversion JSON en Java.
import org.springframework.scheduling.annotation.Scheduled; //Annotation pour exécuter automatiquement les tests à intervalles réguliers.
import org.springframework.stereotype.Service; //Indique que cette classe est un service Spring.
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
          try {
               ProcessBuilder processBuilder = new ProcessBuilder( //Utilisation de ProcessBuilder pour exécuter Newman via la ligne de commande.
                       "C:\\Users\\LENOVO\\AppData\\Roaming\\npm\\newman.cmd", // Chemin vers Newman.
                       "run", "postman_collection.json" //Exécute la collection de tests Postman Reqres_API.
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
}

