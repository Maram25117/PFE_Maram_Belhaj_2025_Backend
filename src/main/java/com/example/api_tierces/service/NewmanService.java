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
import java.util.Map;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class NewmanService {


    private final ApiMonitoringRepository resultRepository;
    private final MeterRegistry meterRegistry; /*Cette ligne déclare une variable meterRegistry qui permettra d’enregistrer et d’exposer des métriques comme les compteurs, minuteries, etc.*/
    /*Elle est marquée final, ce qui signifie que sa référence ne peut pas être modifiée une fois initialisée.*/
    private final ObjectMapper objectMapper = new ObjectMapper(); // un nouvel objet ObjectMapper est directement instancié pour pouvoir faire des conversions entre objets et JSON.


    private final Map<String, TestMetricData> latestTestMetrics = new ConcurrentHashMap<>(); //ConcurrentHashMap est utilisé ici pour assurer un accès concurrent sécurisé à cette structure (plusieurs threads peuvent y accéder sans causer d’erreurs).

    private final AtomicLong lastCompletionTimestamp = new AtomicLong(0); //AtomicLong est une classe utilisée pour gérer un entier long de manière thread-safe, surtout quand plusieurs threads doivent lire et écrire cette valeur.


    private record TestMetricData(
            AtomicLong responseTimeMs, //(sous forme de AtomicLong pour pouvoir être modifié de manière thread-safe)
            AtomicInteger statusCode, //thread safe : Peut être utilisé par plusieurs threads en même temps sans risque d'erreur
            AtomicInteger success
    ) {
        TestMetricData() {
            this(new AtomicLong(0), new AtomicInteger(0), new AtomicInteger(0));
            //Ce constructeur sans argument permet d'initialiser un objet TestMetricData avec 0
        }
    }//Une record en Java est une classe spéciale conçue pour représenter des données immuables.


    public NewmanService(ApiMonitoringRepository resultRepository, MeterRegistry meterRegistry) {
        this.resultRepository = resultRepository;
        this.meterRegistry = meterRegistry;
        System.out.println("NewmanService créé avec le repository : " + resultRepository + " et MeterRegistry: " + meterRegistry);

        Gauge.builder("newman_run_last_completion_timestamp_seconds", this.lastCompletionTimestamp::get) //Cette ligne crée une gauge (jauge) nommée "newman_run_last_completion_timestamp_seconds".
                .description("Unix timestamp du dernier traitement complet des tests Newman")
                .register(meterRegistry);
        System.out.println("Gauge 'newman_run_last_completion_timestamp_seconds' enregistré.");
        /*this.lastCompletionTimestamp::get est une référence de méthode qui fournit la valeur actuelle.
          .description(...) donne une explication humaine de ce que la jauge représente.
          .register(meterRegistry) enregistre cette métrique dans le système de monitoring.*/
    }


    @Scheduled(cron = "${newman.schedule.cron:* * * * * *}")
    public void runScheduledNewmanTests() {
        System.out.println("Début de l'exécution planifiée des tests Newman.");
        String defaultCollectionResourceName = "postman/postman_collection.json"; //chemin du fichier postma_collection

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


    public String processPostmanCollection(String postmanCollectionJson) {
        System.out.println("Début du traitement manuel de la collection Postman fournie.");
        Path tempFilePath = null;
        try {
            tempFilePath = Files.createTempFile("postman_manual_", ".json");
            System.out.println("Fichier temporaire créé : " + tempFilePath.toString());
            Files.writeString(tempFilePath, postmanCollectionJson, StandardOpenOption.WRITE);
            boolean processSuccess = executeNewmanAndProcessResults(tempFilePath.toAbsolutePath().toString());
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

        if (!processSuccess || currentRunResults.isEmpty() && exitCode == 0) {
            meterRegistry.counter("newman_run_errors_total", Tags.of("reason", processSuccess ? "parsing_failure" : "newman_exit_code")).increment();
        }
        return processSuccess;
    }


    private void parseNewmanOutput(BufferedReader reader, List<ParsedResult> results) throws Exception {
        String line;
        ParsedResult currentResult = null;
        StringBuilder errorMessageBuilder = new StringBuilder();
        boolean inErrorBlock = false;
        boolean hasNumDotError = false;

        Pattern requestLinePattern = Pattern.compile("^\\s*(GET|POST|PUT|DELETE|PATCH|HEAD|OPTIONS)\\s+(https?://[^\\s]+)\\s+\\[(.*?)\\]");

        // *** PATTERN CORRIGÉ pour Status/Temps (gère ms et s) ***
        // Groupe 1: Status Code (\d{3})
        // Groupe 2: Valeur Temps ([\d.]+) - chiffres ou point
        // Groupe 3: Unité (ms|s) - ms ou s
        Pattern statusPattern = Pattern.compile("(\\d{3})\\s+[^,]+,\\s+[^,]+,\\s+([\\d.]+)(ms|s)");

        String currentFolderName = "Global";

        while ((line = reader.readLine()) != null) {
            if (line.trim().startsWith("□ ") || line.trim().startsWith("→ ")) {
                if (currentResult != null) {
                    finalizeAndAddResult(currentResult, hasNumDotError, errorMessageBuilder, results);
                    currentResult = null;
                }
                currentFolderName = line.substring(line.indexOf(" ")).trim();
                errorMessageBuilder.setLength(0); inErrorBlock = false; hasNumDotError = false;
                continue;
            }


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


            if (currentResult.httpMethod == null && currentResult.path == null) {
                Matcher requestMatcher = requestLinePattern.matcher(line);
                if (requestMatcher.find()) {
                    currentResult.httpMethod = requestMatcher.group(1);
                    String fullUrl = requestMatcher.group(2);
                    String statusAndTimePart = requestMatcher.group(3);


                    String sanitizedUrl = fullUrl;
                    String placeholder = "<integer>";
                    if (sanitizedUrl.contains(placeholder)) {
                        String replacement = "3";
                        sanitizedUrl = sanitizedUrl.replace(placeholder, replacement);
                        System.out.println("WARN: URL nettoyée : '" + fullUrl + "' -> '" + sanitizedUrl + "'");
                    }


                    try {
                        URI uri = new URI(sanitizedUrl);
                        currentResult.path = uri.getPath();
                        if (uri.getQuery() != null) currentResult.path += "?" + uri.getQuery();

                        if (line.contains("[errored]")) {
                            currentResult.statusCode = 0; currentResult.responseTime = 0;
                            errorMessageBuilder.append("Request Errored: ").append(line.trim());
                            inErrorBlock = true;
                        } else {
                            Matcher statusMatcher = statusPattern.matcher(statusAndTimePart);
                            if (statusMatcher.find()) {
                                try {
                                    currentResult.statusCode = Integer.parseInt(statusMatcher.group(1));

                                    String timeValueStr = statusMatcher.group(2);
                                    String timeUnit = statusMatcher.group(3);

                                    double timeValue = Double.parseDouble(timeValueStr);

                                    if ("s".equalsIgnoreCase(timeUnit)) {
                                        timeValue = timeValue * 1000;
                                    }

                                    currentResult.responseTime = Math.round(timeValue);

                                } catch (NumberFormatException nfe) {
                                    System.err.println("  Erreur format numérique Status/Temps : '" + statusAndTimePart + "' pour " + currentResult.apiName + " - " + nfe.getMessage());
                                    currentResult.statusCode = 0; currentResult.responseTime = 0;
                                    errorMessageBuilder.append("Erreur format numérique Status/Temps: ").append(statusAndTimePart);
                                    inErrorBlock = true;
                                }
                            } else {
                                System.err.println("  Impossible d'extraire status/temps (pattern corrigé) de : '" + statusAndTimePart + "' pour " + currentResult.apiName + " sur ligne: " + line);
                                currentResult.statusCode = 0; currentResult.responseTime = 0;
                                errorMessageBuilder.append("Erreur parsing Status/Temps (Pattern): ").append(statusAndTimePart);
                                inErrorBlock = true;
                            }

                        }
                    } catch (URISyntaxException e) {
                        System.err.println("  Erreur parsing URL/Status depuis : " + line + " pour " + currentResult.apiName + " - " + e.getMessage());
                        currentResult.path = "Unknown Path";
                        currentResult.statusCode = 0; currentResult.responseTime = 0;
                        errorMessageBuilder.append("Erreur parsing URL/Status (").append(e.getClass().getSimpleName()).append("): ").append(e.getMessage());
                        inErrorBlock = true;
                    } catch (Exception e) {
                        System.err.println("  Erreur inattendue lors du traitement de la ligne de détails : " + line + " pour " + currentResult.apiName + " - " + e.getMessage());
                        currentResult.path = "Unknown Path";
                        currentResult.statusCode = 0; currentResult.responseTime = 0;
                        errorMessageBuilder.append("Erreur Générale Traitement Détails: ").append(e.getMessage());
                        inErrorBlock = true;
                    }
                    continue;
                }
            }


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

        }


        if (currentResult != null) {
            if (currentResult.httpMethod == null || currentResult.path == null) {
                System.err.println("ERREUR PARSING: Fin de la sortie atteinte avant d'avoir les détails complets pour la dernière requête: " + currentResult.apiName);
                currentResult.path = currentResult.path == null ? "Unknown Path (Incomplete End)" : currentResult.path;
                errorMessageBuilder.append("Parsing Error: End of stream reached before request details line found.");
                inErrorBlock = true;
            }
            finalizeAndAddResult(currentResult, hasNumDotError, errorMessageBuilder, results);
        }
    }

    private void finalizeAndAddResult(ParsedResult result, boolean hasNumDotError, StringBuilder errorMsgBuilder, List<ParsedResult> resultsList) {
        if (result.path == null || result.httpMethod == null) {
            System.err.println("Skipping result for API '" + result.apiName + "' due to missing path or method.");
            if (errorMsgBuilder.length() > 0) System.err.println("  Captured error context: " + errorMsgBuilder.toString());
            return;
        }

        if ("Unknown Path".equals(result.path) || errorMsgBuilder.toString().contains("Request Errored:") || hasNumDotError) {
            result.success = false;
            if (errorMsgBuilder.length() == 0) {
                if(hasNumDotError) errorMsgBuilder.append("Assertion failed, details not captured.");
                else errorMsgBuilder.append("Request failed during setup, parsing or due to [errored] tag.");
            }
        }

        else {
            result.success = true;
            String currentError = errorMsgBuilder.toString();
            if (currentError.startsWith("Request failed with status code:") || currentError.startsWith("Unexpected status code:")) {
                errorMsgBuilder.setLength(0);
            }
        }


        result.errorMessage = errorMsgBuilder.toString().trim();


        System.out.println("Résultat finalisé: Path=" + result.path + ", Method=" + result.httpMethod +
                ", Success=" + result.success +
                ", Status=" + result.statusCode + ", Time=" + result.responseTime + "ms" +
                (result.errorMessage.isEmpty() ? "" : ", Error=Present"));
        resultsList.add(result);
    }


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

        this.lastCompletionTimestamp.set(nowEpochSeconds);
    }



    private void updateMetricsForResult(ParsedResult result) {
        String metricKey = result.httpMethod + ":" + result.path;
        Tags tags = Tags.of(
                Tag.of("path", result.path),
                Tag.of("method", result.httpMethod),
                Tag.of("api_name", result.apiName != null ? result.apiName : "unknown")
        );


        TestMetricData metricData = latestTestMetrics.computeIfAbsent(metricKey, k -> {
            TestMetricData newData = new TestMetricData();
            // Gauge pour le temps de réponse
            Gauge.builder("newman_test_response_time_ms", newData.responseTimeMs::get).tags(tags).description("Response time in milliseconds for the Newman test").register(meterRegistry);
            // Gauge pour le code statut HTTP réel
            Gauge.builder("newman_test_status_code", newData.statusCode::get).tags(tags).description("HTTP status code for the Newman test").register(meterRegistry);
            // Gauge pour le succès basé sur les scripts Postman
            Gauge.builder("newman_test_success", newData.success::get).tags(tags).description("Indicates if the Postman test script passed (1) or failed (0)").register(meterRegistry);
            System.out.println("Registered new metrics for: " + k);
            return newData;
        });
        metricData.responseTimeMs().set(result.responseTime);
        metricData.statusCode().set(result.statusCode);
        metricData.success().set(result.success ? 1 : 0);
    }


    @Transactional
    protected void persistResult(ParsedResult parsedResult, LocalDateTime timestamp) {
        if ("Unknown Path".equals(parsedResult.path)) {
            System.out.println("Ignorer l'enregistrement DB pour Unknown Path");
            return;
        }
        try {

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
            dbRecord.setStatusCode(parsedResult.statusCode);
            dbRecord.setErrorMessage(parsedResult.errorMessage.isEmpty() ? null : parsedResult.errorMessage);
            dbRecord.setLevel(level);
            resultRepository.save(dbRecord);

        } catch (Exception e) {
            System.err.println("Erreur lors de l'enregistrement DB pour : " + parsedResult.path + " (" + parsedResult.httpMethod + "): " + e.getMessage());
            e.printStackTrace();
        }
    }


    private static class ParsedResult {
        String apiName;
        String path;
        String httpMethod;
        boolean success = false;
        int statusCode = 0;
        long responseTime = 0;
        String errorMessage = "";
    }

}