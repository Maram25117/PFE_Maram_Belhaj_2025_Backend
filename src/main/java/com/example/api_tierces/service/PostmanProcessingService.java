package com.example.api_tierces.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.api_tierces.model.*;
import com.example.api_tierces.repository.*;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
/* Code shiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiih */
/*@Service
public class PostmanProcessingService {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApiResponseRepository apiResponseRepository;

    @Autowired
    private ApiRepository apiRepository;

    private static final String POSTMAN_COLLECTION_PATH = "src/main/resources/";

    // 📌 **Méthode principale de traitement**
    @Transactional
    public String processPostmanCollection(String postmanCollectionJson) {
        try {
            JsonNode collectionNode = objectMapper.readTree(postmanCollectionJson);
            JsonNode itemsNode = collectionNode.get("item");

            processItems(itemsNode);

            saveCollectionToResources(collectionNode);
            return "Collection Postman mise à jour et enregistrée avec succès !";
        } catch (Exception e) {
            return "Erreur lors du traitement de la collection Postman : " + e.getMessage();
        }
    }

    // 📌 **Parcourt récursif des éléments Postman**
    private void processItems(JsonNode itemsNode) {
        if (itemsNode == null || !itemsNode.isArray()) {
            return;
        }

        for (JsonNode item : itemsNode) {
            replacePathParameters(item);
            modifyRequestBody(item);
            addTestsToRequest(item);

            if (item.has("item")) {
                processItems(item.get("item"));
            }
        }
    }

    // 📌 **Remplacement dynamique des paramètres dans l'URL et prise en charge des DELETE**
    private void replacePathParameters(JsonNode item) {
        if (item == null || !item.has("request")) {
            return;
        }

        JsonNode requestNode = item.get("request");
        if (!requestNode.has("url") || !requestNode.has("method")) {
            return;
        }

        JsonNode urlNode = requestNode.get("url");
        String method = requestNode.get("method").asText();

        if (urlNode.has("variable")) {
            for (JsonNode variable : urlNode.get("variable")) {
                if (variable.has("key") && variable.has("value")) {
                    String key = variable.get("key").asText();
                    String value = variable.get("value").asText();

                    // 🔥 **Si la méthode est DELETE, on force l'ID à 4**
                    if ("DELETE".equalsIgnoreCase(method) && (key.equalsIgnoreCase("id") || value.matches("\\d+"))) {
                        ((ObjectNode) variable).put("value", "4");
                    } else {
                        String replacementValue = getDefaultValueForType(value);
                        ((ObjectNode) variable).put("value", replacementValue);
                    }
                }
            }
        }

        // 🔥 **Modification de l'URL si l'ID est dans le chemin**
        if (urlNode.has("raw")) {
            String rawUrl = urlNode.get("raw").asText();
            if ("DELETE".equalsIgnoreCase(method)) {
                rawUrl = rawUrl.replaceAll("/\\d+($|\\?)", "/4$1"); // Remplace l'ID dans l'URL par 4
                ((ObjectNode) urlNode).put("raw", rawUrl);
            }
        }
    }

    // 📌 **Modification dynamique du corps des requêtes**
    private void modifyRequestBody(JsonNode item) {
        if (item == null || !item.has("request")) {
            return;
        }

        JsonNode requestNode = item.get("request");
        if (!requestNode.has("body") || !requestNode.get("body").has("raw")) {
            return;
        }

        String body = requestNode.get("body").get("raw").asText();

        body = body.replaceAll("\"<long>\"", "3")
                .replaceAll("\"<integer>\"", "10")
                .replaceAll("\"<string>\"", "\"TestString\"")
                .replaceAll("\"<boolean>\"", "true")
                .replaceAll("\"<double>\"", "99.99")
                .replaceAll("\"<number>\"", "100")
                .replaceAll("\"<dateTime>\"", "\"2023-10-01T00:00:00Z\"");

        ((ObjectNode) requestNode.get("body")).put("raw", body);
    }

    // 📌 **Ajout des tests Postman**
   private void addTestsToRequest(JsonNode item) {
        if (item == null || !item.has("request")) {
            return;
        }

        JsonNode requestNode = item.get("request");
        if (!requestNode.has("url") || !requestNode.has("method")) {
            return;
        }

        if (!item.has("response") || !item.get("response").isArray()) {
            return;
        }

        List<Integer> statusCodes = new ArrayList<>();
        Map<Integer, JsonNode> responseSchemas = new HashMap<>();

        for (JsonNode responseNode : item.get("response")) {
            if (!responseNode.has("code") || !responseNode.has("body")) {
                continue;
            }

            int statusCode = responseNode.get("code").asInt();
            String responseBody = responseNode.get("body").asText();
            statusCodes.add(statusCode);

            if (statusCode == 200 && !responseBody.isEmpty()) {
                try {
                    JsonNode responseJson = objectMapper.readTree(responseBody);
                    responseSchemas.put(statusCode, responseJson);
                } catch (Exception e) {
                    System.out.println("❌ Impossible de parser la réponse JSON");
                }
            }
        }

        StringBuilder testScript = new StringBuilder();
        testScript.append("pm.test(\"Vérification des réponses possibles\", function () {\n")
                .append("    pm.expect([").append(statusCodes.toString().replaceAll("[\\[\\]]", ""))
                .append("]).to.include(pm.response.code);\n")
                .append("});\n");

        // 🔎 Vérification du schéma attendu uniquement si le statut est 200
        if (responseSchemas.containsKey(200)) {
            JsonNode schema = responseSchemas.get(200);
            testScript.append("pm.test(\"Vérification de la réponse de l'API\", function () {\n")
                    .append("    const jsonResponse = pm.response.json();\n")
                    .append("    if (pm.response.code === 200) {\n")
                    .append("        if (Array.isArray(jsonResponse)) {\n")
                    .append("            pm.expect(jsonResponse).to.be.an('array').and.not.be.empty;\n")
                    .append("            jsonResponse.forEach(function(item) {\n");

            for (String field : getSchemaFields(schema)) {
                testScript.append("                pm.expect(item).to.have.property(\"").append(field).append("\");\n");
            }

            testScript.append("            });\n")
                    .append("        } else {\n");

            for (String field : getSchemaFields(schema)) {
                testScript.append("            pm.expect(jsonResponse).to.have.property(\"").append(field).append("\");\n");
            }

            testScript.append("        }\n    }\n")
                    .append("});\n");
        }

        ObjectNode eventNode = objectMapper.createObjectNode();
        eventNode.put("listen", "test");

        ObjectNode scriptNode = objectMapper.createObjectNode();
        scriptNode.put("type", "text/javascript");
        scriptNode.put("exec", testScript.toString());

        eventNode.set("script", scriptNode);
        ArrayNode eventArray = objectMapper.createArrayNode();
        eventArray.add(eventNode);

        ((ObjectNode) item).set("event", eventArray);
    }
    // 📌 **Détermination des valeurs par défaut pour les types dynamiques**
    private String getDefaultValueForType(String type) {
        switch (type.toLowerCase()) {
            case "<long>":
            case "long":
            case "id":
                return "3";
            case "<integer>":
            case "int":
                return "10";
            case "<string>":
            case "string":
            case "name":
                return "\"TestString\"";
            case "<boolean>":
            case "bool":
                return "true";
            case "<double>":
            case "float":
                return "99.99";
            case "<number>":
                return "100";
            case "<datetime>":
                return "\"2023-10-01T00:00:00Z\"";
            default:
                return "\"default_value\"";
        }
    }


    // 📌 **Récupérer les champs du schéma JSON**
    private List<String> getSchemaFields(JsonNode schema) {
        List<String> fields = new ArrayList<>();
        schema.fieldNames().forEachRemaining(fields::add);
        return fields;
    }

    // 📌 **Sauvegarde de la collection**
    private void saveCollectionToResources(JsonNode collectionNode) throws Exception {
        File targetFile = new File(POSTMAN_COLLECTION_PATH);
        try (FileWriter writer = new FileWriter(targetFile)) {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(writer, collectionNode);
        }
    }
}*/


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.api_tierces.model.*;
import com.example.api_tierces.repository.*;

import java.io.File;
import java.io.FileWriter;
import java.util.*;
import java.util.stream.Collectors;

/*@Service
public class PostmanProcessingService {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApiResponseRepository apiResponseRepository;

    @Autowired
    private ApiRepository apiRepository;

    @Autowired
    private ApiParametersRepository apiParametersRepository;

    private static final String POSTMAN_COLLECTION_PATH = "src/main/resources/";

    // 📌 **Méthode principale de traitement**
    @Transactional
    public String processPostmanCollection(String postmanCollectionJson) {
        try {
            JsonNode collectionNode = objectMapper.readTree(postmanCollectionJson);
            JsonNode itemsNode = collectionNode.get("item");

            processItems(itemsNode);

            saveCollectionToResources(collectionNode);
            return "Collection Postman mise à jour et enregistrée avec succès !";
        } catch (Exception e) {
            return "Erreur lors du traitement de la collection Postman : " + e.getMessage();
        }
    }

    // 📌 **Parcourt récursif des éléments Postman**
    private void processItems(JsonNode itemsNode) {
        if (itemsNode == null || !itemsNode.isArray()) {
            return;
        }

        for (JsonNode item : itemsNode) {
            replacePathParameters(item);
            modifyRequestBody(item);
            addTestsToRequest(item);

            if (item.has("item")) {
                processItems(item.get("item"));
            }
        }
    }
    // 📌 **Remplacement dynamique des paramètres dans l'URL**
    private void replacePathParameters(JsonNode item) {
        if (item == null || !item.has("request")) {
            return;
        }

        JsonNode requestNode = item.get("request");
        if (!requestNode.has("url") || !requestNode.has("method")) {
            return;
        }

        JsonNode urlNode = requestNode.get("url");
        String method = requestNode.get("method").asText();

        // 🔥 **Traitement des Path Variables**
        if (urlNode.has("path")) {
            ArrayNode pathVariables = (ArrayNode) urlNode.get("path");
            for (JsonNode pathVariable : pathVariables) {
                if (pathVariable.has("key") && pathVariable.has("value")) {
                    String key = pathVariable.get("key").asText();
                    String value = pathVariable.get("value").asText();

                    // Si la valeur est un placeholder (ex: <long>), on la remplace par une valeur par défaut
                    if (value.startsWith("<") && value.endsWith(">")) {
                        String type = value.substring(1, value.length() - 1); // Extraire le type (ex: "long")
                        String replacementValue = getDefaultValueForType(type); // Obtenir la valeur par défaut
                        ((ObjectNode) pathVariable).put("value", replacementValue); // Remplacer la valeur
                    }
                }
            }
        }

        // 🔥 **Traitement des Query Parameters**
        if (urlNode.has("query")) {
            for (JsonNode queryParam : urlNode.get("query")) {
                if (queryParam.has("key") && queryParam.has("value")) {
                    String key = queryParam.get("key").asText();
                    String value = queryParam.get("value").asText();

                    // Récupérer le type du paramètre à partir de la base de données
                    ApiParameters parameter = apiParametersRepository.findByName(key);
                    if (parameter != null && "Aucun exemple disponible".equals(value)) {
                        // Remplacer par une valeur par défaut adaptée au type
                        String replacementValue = getDefaultValueForType(parameter.getData_type());
                        ((ObjectNode) queryParam).put("value", replacementValue);
                    }
                }
            }
        }

        // 🔥 **Traitement des variables dans l'URL**
        if (urlNode.has("variable")) {
            for (JsonNode variable : urlNode.get("variable")) {
                if (variable.has("key") && variable.has("value")) {
                    String key = variable.get("key").asText();
                    String value = variable.get("value").asText();

                    // 🔥 **Si la méthode est DELETE, on force l'ID à 4**
                    if ("DELETE".equalsIgnoreCase(method) && (key.equalsIgnoreCase("id") || value.matches("\\d+"))) {
                        ((ObjectNode) variable).put("value", "4");
                    } else {
                        // Remplacer les placeholders par des valeurs par défaut
                        String replacementValue = getDefaultValueForType(value);
                        ((ObjectNode) variable).put("value", replacementValue);
                    }
                }
            }
        }

        // 🔥 **Modification de l'URL si l'ID est dans le chemin (pour DELETE)**
        if (urlNode.has("raw")) {
            String rawUrl = urlNode.get("raw").asText();
            if ("DELETE".equalsIgnoreCase(method)) {
                rawUrl = rawUrl.replaceAll("/\\d+($|\\?)", "/4$1"); // Remplace l'ID dans l'URL par 4
                ((ObjectNode) urlNode).put("raw", rawUrl);
            }
        }
    }
    // 📌 **Remplacement dynamique des paramètres dans l'URL**
    // 📌 **Remplacement dynamique des paramètres dans l'URL**
   /* private void replacePathParameters(JsonNode item) {
        if (item == null || !item.has("request")) {
            return;
        }

        JsonNode requestNode = item.get("request");
        if (!requestNode.has("url") || !requestNode.has("method")) {
            return;
        }

        JsonNode urlNode = requestNode.get("url");
        String method = requestNode.get("method").asText();

        // 🔥 **Traitement des Path Variables**
        if (urlNode.has("path")) {
            ArrayNode pathVariables = (ArrayNode) urlNode.get("path");
            for (JsonNode pathVariable : pathVariables) {
                if (pathVariable.has("key") && pathVariable.has("value")) {
                    String key = pathVariable.get("key").asText();
                    String value = pathVariable.get("value").asText();

                    // Si la valeur est un placeholder (ex: <long>), on la remplace par une valeur par défaut
                    if (value.startsWith("<") && value.endsWith(">")) {
                        String type = value.substring(1, value.length() - 1); // Extraire le type (ex: "long")
                        String replacementValue = getDefaultValueForType(type); // Obtenir la valeur par défaut
                        ((ObjectNode) pathVariable).put("value", replacementValue); // Remplacer la valeur
                    }
                }
            }
        }

        // 🔥 **Traitement des Query Parameters**
        if (urlNode.has("query")) {
            for (JsonNode queryParam : urlNode.get("query")) {
                if (queryParam.has("key") && queryParam.has("value")) {
                    String key = queryParam.get("key").asText();
                    String value = queryParam.get("value").asText();

                    // Récupérer le type du paramètre à partir de la base de données
                    ApiParameters parameter = apiParametersRepository.findByName(key);
                    if (parameter != null && "Aucun exemple disponible".equals(value)) {
                        // Remplacer par une valeur par défaut adaptée au type
                        String replacementValue = getDefaultValueForType(parameter.getData_type());
                        ((ObjectNode) queryParam).put("value", replacementValue);
                    }
                }
            }
        }

        // 🔥 **Modification de l'URL si l'ID est dans le chemin (pour DELETE)**
        if (urlNode.has("raw")) {
            String rawUrl = urlNode.get("raw").asText();
            if ("DELETE".equalsIgnoreCase(method)) {
                rawUrl = rawUrl.replaceAll("/\\d+($|\\?)", "/4$1"); // Remplace l'ID dans l'URL par 4
                ((ObjectNode) urlNode).put("raw", rawUrl);
            }
        }
    }*/
    // 📌 **Détermination des valeurs par défaut pour les types dynamiques**
    /*private String getDefaultValueForType(String type) {
        if (type == null) {
            return "\"default_value\"";
        }

        switch (type.toLowerCase()) {
            case "long":
            case "integer":
            case "int":
                return "10"; // Valeur par défaut pour les entiers
            case "string":
                return "\"TestString\""; // Valeur par défaut pour les chaînes de caractères
            case "boolean":
            case "bool":
                return "true"; // Valeur par défaut pour les booléens
            case "double":
            case "float":
                return "99.99"; // Valeur par défaut pour les nombres décimaux
            case "datetime":
            case "date":
                return "\"2023-10-01T00:00:00Z\""; // Valeur par défaut pour les dates
            default:
                return "\"default_value\""; // Valeur par défaut générique
        }
    }

    // 📌 **Modification dynamique du corps des requêtes**
    private void modifyRequestBody(JsonNode item) {
        if (item == null || !item.has("request")) {
            return;
        }

        JsonNode requestNode = item.get("request");
        if (!requestNode.has("body") || !requestNode.get("body").has("raw")) {
            return;
        }

        String body = requestNode.get("body").get("raw").asText();

        body = body.replaceAll("\"<long>\"", "3")
                .replaceAll("\"<integer>\"", "10")
                .replaceAll("\"<string>\"", "\"TestString\"")
                .replaceAll("\"<boolean>\"", "true")
                .replaceAll("\"<double>\"", "99.99")
                .replaceAll("\"<number>\"", "100")
                .replaceAll("\"<dateTime>\"", "\"2023-10-01T00:00:00Z\"");

        ((ObjectNode) requestNode.get("body")).put("raw", body);
    }

    // 📌 **Ajout des tests Postman**
    private void addTestsToRequest(JsonNode item) {
        if (item == null || !item.has("request")) {
            return;
        }

        JsonNode requestNode = item.get("request");
        if (!requestNode.has("url") || !requestNode.has("method")) {
            return;
        }

        if (!item.has("response") || !item.get("response").isArray()) {
            return;
        }

        List<Integer> statusCodes = new ArrayList<>();
        Map<Integer, JsonNode> responseSchemas = new HashMap<>();

        for (JsonNode responseNode : item.get("response")) {
            if (!responseNode.has("code") || !responseNode.has("body")) {
                continue;
            }

            int statusCode = responseNode.get("code").asInt();
            String responseBody = responseNode.get("body").asText();
            statusCodes.add(statusCode);

            if (statusCode == 200 && !responseBody.isEmpty()) {
                try {
                    JsonNode responseJson = objectMapper.readTree(responseBody);
                    responseSchemas.put(statusCode, responseJson);
                } catch (Exception e) {
                    System.out.println("❌ Impossible de parser la réponse JSON");
                }
            }
        }

        StringBuilder testScript = new StringBuilder();
        testScript.append("pm.test(\"Vérification des réponses possibles\", function () {\n")
                .append("    pm.expect([").append(statusCodes.toString().replaceAll("[\\[\\]]", ""))
                .append("]).to.include(pm.response.code);\n")
                .append("});\n");

        // 🔎 Vérification du schéma attendu uniquement si le statut est 200
        if (responseSchemas.containsKey(200)) {
            JsonNode schema = responseSchemas.get(200);
            testScript.append("pm.test(\"Vérification de la réponse de l'API\", function () {\n")
                    .append("    const jsonResponse = pm.response.json();\n")
                    .append("    if (pm.response.code === 200) {\n")
                    .append("        if (Array.isArray(jsonResponse)) {\n")
                    .append("            pm.expect(jsonResponse).to.be.an('array').and.not.be.empty;\n")
                    .append("            jsonResponse.forEach(function(item) {\n");

            for (String field : getSchemaFields(schema)) {
                testScript.append("                pm.expect(item).to.have.property(\"").append(field).append("\");\n");
            }

            testScript.append("            });\n")
                    .append("        } else {\n");

            for (String field : getSchemaFields(schema)) {
                testScript.append("            pm.expect(jsonResponse).to.have.property(\"").append(field).append("\");\n");
            }

            testScript.append("        }\n    }\n")
                    .append("});\n");
        }

        ObjectNode eventNode = objectMapper.createObjectNode();
        eventNode.put("listen", "test");

        ObjectNode scriptNode = objectMapper.createObjectNode();
        scriptNode.put("type", "text/javascript");
        scriptNode.put("exec", testScript.toString());

        eventNode.set("script", scriptNode);
        ArrayNode eventArray = objectMapper.createArrayNode();
        eventArray.add(eventNode);

        ((ObjectNode) item).set("event", eventArray);
    }

    // 📌 **Récupérer les champs du schéma JSON**
    private List<String> getSchemaFields(JsonNode schema) {
        List<String> fields = new ArrayList<>();
        schema.fieldNames().forEachRemaining(fields::add);
        return fields;
    }

    // 📌 **Sauvegarde de la collection**
    private void saveCollectionToResources(JsonNode collectionNode) throws Exception {
        File targetFile = new File(POSTMAN_COLLECTION_PATH);
        try (FileWriter writer = new FileWriter(targetFile)) {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(writer, collectionNode);
        }
    }
}*/
/*codeeeee hedhaaaaaa shihhhhhhhhhhhhhhh*/
@Service
public class PostmanProcessingService {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApiResponseRepository apiResponseRepository;

    @Autowired
    private ApiRepository apiRepository;

    @Autowired
    private ApiParametersRepository apiParametersRepository;

    private static final String POSTMAN_COLLECTION_PATH = "src/main/resources/postman_collection.json";

    // Méthode principale de traitement
    @Transactional
    public String processPostmanCollection(String postmanCollectionJson) {
        try {
            JsonNode collectionNode = objectMapper.readTree(postmanCollectionJson);
            JsonNode itemsNode = collectionNode.get("item");

            processItems(itemsNode);

            saveCollectionToResources(collectionNode);
            return "Collection Postman mise à jour et enregistrée avec succès !";
        } catch (Exception e) {
            return "Erreur lors du traitement de la collection Postman : " + e.getMessage();
        }
    }

    // Parcourt récursif des éléments Postman
    private void processItems(JsonNode itemsNode) {
        if (itemsNode == null || !itemsNode.isArray()) {
            return;
        }

        for (JsonNode item : itemsNode) {
            replacePathParameters(item);
            modifyRequestBody(item);
            addTestsToRequest(item);

            if (item.has("item")) {
                processItems(item.get("item"));
            }
        }
    }

    // Remplacement dynamique des paramètres dans l'URL (hedhi awel wahda)
    /*private void replacePathParameters(JsonNode item) {
        if (item == null || !item.has("request")) {
            return;
        }

        JsonNode requestNode = item.get("request");
        if (!requestNode.has("url") || !requestNode.has("method")) {
            return;
        }

        JsonNode urlNode = requestNode.get("url");
        String method = requestNode.get("method").asText();

        // Traitement des Path Variables
        if (urlNode.has("variable")) {
            for (JsonNode variable : urlNode.get("variable")) {
                if (variable.has("key") && variable.has("value")) {
                    String key = variable.get("key").asText();
                    String value = variable.get("value").asText();

                    // Si la méthode est DELETE, on force l'ID à 4
                    if ("DELETE".equalsIgnoreCase(method) && (key.equalsIgnoreCase("id") || value.matches("\\d+"))) {
                        ((ObjectNode) variable).put("value", "4");
                    } else {
                        // Remplacer les placeholders par des valeurs réelles adaptées au type
                        String replacementValue = getRealValueForType(value);
                        ((ObjectNode) variable).put("value", replacementValue);
                    }
                }
            }
        }

        // Traitement des Query Parameters
        if (urlNode.has("query")) {
            for (JsonNode queryParam : urlNode.get("query")) {
                if (queryParam.has("key") && queryParam.has("value")) {
                    String key = queryParam.get("key").asText();
                    String value = queryParam.get("value").asText();

                    // Récupérer le type du paramètre à partir de la base de données
                    //ApiParameters parameter = apiParametersRepository.findByName(key);
                    List<ApiParameters> parameter = apiParametersRepository.findByName(key);
                    if (parameter != null && "Aucun exemple disponible".equals(value)) {
                        // Remplacer par une valeur réelle adaptée au type
                        String replacementValue = getRealValueForType(parameter.getData_type());
                        ((ObjectNode) queryParam).put("value", replacementValue);
                    }
                }
            }
        }

        // Modification de l'URL si l'ID est dans le chemin
        if (urlNode.has("raw")) {
            String rawUrl = urlNode.get("raw").asText();
            if ("DELETE".equalsIgnoreCase(method)) {
                rawUrl = rawUrl.replaceAll("/\\d+($|\\?)", "/4$1"); // Remplace l'ID dans l'URL par 4
                ((ObjectNode) urlNode).put("raw", rawUrl);
            }
        }
    }*/
    /**
     * Remplace les placeholders (ex: "<string>", "<integer>") dans les valeurs
     * des variables de chemin et des paramètres de requête DANS LE JSON Postman fourni.
     * Ne fait PAS appel à la base de données.
     * Gère aussi le cas spécifique du remplacement d'ID pour les requêtes DELETE.
     */
    /*private void replacePathParameters(JsonNode item) { // Renommée
        if (item == null || !item.has("request")) {
            return;
        }

        JsonNode requestNode = item.get("request");
        if (!requestNode.has("url") || !requestNode.has("method")) {
            return;
        }

        JsonNode urlNode = requestNode.get("url");
        String method = requestNode.get("method").asText();

        // --- Traitement des Path Variables (Variables de Chemin) ---
        if (urlNode.has("variable") && urlNode.get("variable").isArray()) {
            for (JsonNode variable : urlNode.get("variable")) {
                // Vérifier que 'variable' est un objet modifiable et a les clés nécessaires
                if (variable.isObject() && variable.has("key") && variable.has("value")) {
                    ObjectNode varObj = (ObjectNode) variable; // Cast sûr ici
                    String key = varObj.path("key").asText();
                    String currentValue = varObj.path("value").asText(); // Lire la valeur actuelle

                    // Logique Spécifique pour DELETE: Remplacer l'ID par '4'
                    // (Basé sur la clé 'id' ou si la valeur actuelle ressemble à un nombre ou un placeholder numérique)
                    if ("DELETE".equalsIgnoreCase(method) &&
                            (key.equalsIgnoreCase("id") || currentValue.matches("\\d+") || currentValue.contains("<long>") || currentValue.contains("<int")))
                    {
                        varObj.put("value", "4"); // Remplacer par l'ID exemple '4'
                    }
                    // Logique Générale: Remplacer les placeholders <...>
                    else if (currentValue.startsWith("<") && currentValue.endsWith(">")) {
                        // Utiliser la valeur actuelle comme indice pour déterminer le remplacement
                        String replacementValue = getRealValueForType(currentValue);
                        varObj.put("value", replacementValue);
                    }
                    // Si ce n'est ni DELETE avec ID, ni un placeholder, on laisse la valeur existante
                }
            }
        }

        // --- Traitement des Query Parameters (Paramètres de Requête) ---
        if (urlNode.has("query") && urlNode.get("query").isArray()) {
            for (JsonNode queryParam : urlNode.get("query")) {
                // Vérifier que 'queryParam' est un objet modifiable et a les clés nécessaires
                if (queryParam.isObject() && queryParam.has("key") && queryParam.has("value")) {
                    ObjectNode queryObj = (ObjectNode) queryParam; // Cast sûr ici
                    // String key = queryObj.path("key").asText(); // Clé non utilisée pour le remplacement ici
                    String currentValue = queryObj.path("value").asText(); // Lire la valeur actuelle

                    // Remplacer SEULEMENT si la valeur actuelle est un placeholder <...>
                    // ou une valeur spécifique indiquant l'absence d'exemple.
                    if ((currentValue.startsWith("<") && currentValue.endsWith(">")) ||
                            "Aucun exemple disponible".equals(currentValue))
                    {
                        // Utiliser la valeur actuelle comme indice pour déterminer le remplacement
                        String replacementValue = getRealValueForType(currentValue);
                        queryObj.put("value", replacementValue);
                    }
                    // Si la valeur actuelle n'est pas un placeholder, on la laisse telle quelle.
                    // Cela préserve les exemples valides potentiellement présents dans la collection importée.
                }
            }
        }

        // --- Modification de l'URL brute pour les requêtes DELETE ---
        // (Cette partie est indépendante de la base de données et peut rester)
        if (urlNode.has("raw")) {
            String rawUrl = urlNode.get("raw").asText();
            if ("DELETE".equalsIgnoreCase(method)) {
                // Essayer de remplacer :variableName ou /nombre par /4 à la fin ou avant un '?' ou '/'
                rawUrl = rawUrl.replaceAll(":\\w+($|\\?|/)", "4$1");
                rawUrl = rawUrl.replaceAll("/\\d+($|\\?|/)", "/4$1");
                // Mettre à jour l'URL brute si elle a été modifiée
                if (urlNode.isObject()) {
                    ((ObjectNode) urlNode).put("raw", rawUrl);
                }
            }
        }
    } // Fin de replaceParameters*/

    /*hedhi chenjareb beha fazet integer te5edh valeur te3ha fel requete*/
    private void replacePathParameters(JsonNode item) { // Renommée dans la réponse précédente
        if (item == null || !item.has("request")) {
            return;
        }

        JsonNode requestNode = item.get("request");
        if (!requestNode.has("url") || !requestNode.has("method") || !requestNode.path("url").isObject()) {
            // S'assurer que url est un objet pour pouvoir le modifier
            return;
        }

        ObjectNode urlNode = (ObjectNode) requestNode.get("url"); // Cast en ObjectNode pour modification
        String method = requestNode.get("method").asText();

        // --- Stockage temporaire des valeurs finales ---
        Map<String, String> finalPathVariables = new HashMap<>();
        List<Map.Entry<String, String>> finalQueryParameters = new ArrayList<>(); // Utiliser List pour garder l'ordre

        // --- 1. Traitement des Path Variables (Variables de Chemin) ---
        if (urlNode.has("variable") && urlNode.get("variable").isArray()) {
            for (JsonNode variable : urlNode.get("variable")) {
                if (variable.isObject() && variable.has("key") && variable.has("value")) {
                    ObjectNode varObj = (ObjectNode) variable;
                    String key = varObj.path("key").asText();
                    String currentValue = varObj.path("value").asText();
                    String finalValue = currentValue; // Valeur par défaut

                    // Logique Spécifique DELETE ID
                    if ("DELETE".equalsIgnoreCase(method) &&
                            (key.equalsIgnoreCase("id") || currentValue.matches("\\d+") || currentValue.contains("<long>") || currentValue.contains("<int>")))
                    {
                        finalValue = "4"; // Remplacer par l'ID exemple '4'
                    }
                    // Logique Générale Placeholders
                    else if (currentValue.startsWith("<") && currentValue.endsWith(">")) {
                        finalValue = getRealValueForType(currentValue); // Remplacer placeholder
                    }

                    // Mettre à jour la valeur DANS l'objet JSON 'variable'
                    varObj.put("value", finalValue);
                    // Stocker la valeur finale pour la reconstruction de 'raw'
                    finalPathVariables.put(key, finalValue);
                }
            }
        }

        // --- 2. Traitement des Query Parameters (Paramètres de Requête) ---
        if (urlNode.has("query") && urlNode.get("query").isArray()) {
            for (JsonNode queryParam : urlNode.get("query")) {
                if (queryParam.isObject() && queryParam.has("key") && queryParam.has("value")) {
                    ObjectNode queryObj = (ObjectNode) queryParam;
                    String key = queryObj.path("key").asText();
                    String currentValue = queryObj.path("value").asText();
                    String finalValue = currentValue; // Valeur par défaut

                    // Remplacer Placeholders ou "Aucun exemple..."
                    if ((currentValue.startsWith("<") && currentValue.endsWith(">")) ||
                            "Aucun exemple disponible".equals(currentValue))
                    {
                        finalValue = getRealValueForType(currentValue);
                    }

                    // Mettre à jour la valeur DANS l'objet JSON 'queryParam'
                    queryObj.put("value", finalValue);
                    // Stocker la paire clé/valeur finale pour la reconstruction de 'raw'
                    // Utilisation de AbstractMap.SimpleEntry pour créer une paire immuable simple
                    finalQueryParameters.add(new AbstractMap.SimpleEntry<>(key, finalValue));
                }
            }
        }

        // --- 3. Reconstruction de l'URL 'raw' ---
        StringBuilder reconstructedUrl = new StringBuilder();
        // Commencer par la base (typiquement une variable Postman comme {{baseUrl}})
        // Essayer de récupérer l'hôte depuis le JSON s'il existe
        if (urlNode.has("host") && urlNode.get("host").isArray() && !urlNode.get("host").isEmpty()) {
            // Prend le premier host, souvent "{{baseUrl}}"
            reconstructedUrl.append(urlNode.get("host").get(0).asText());
        } else {
            reconstructedUrl.append("{{baseUrl}}"); // Fallback si host n'est pas défini comme attendu
        }

        // Ajouter les segments de chemin, en remplaçant les variables
        if (urlNode.has("path") && urlNode.get("path").isArray()) {
            ArrayNode pathSegments = (ArrayNode) urlNode.get("path");
            for (JsonNode segmentNode : pathSegments) {
                String segment = segmentNode.asText();
                if (segment.startsWith(":")) { // C'est une variable de chemin Postman (ex: ":userId")
                    String varKey = segment.substring(1); // Enlever le ':'
                    // Utiliser la valeur finale stockée, ou garder le segment si non trouvé
                    reconstructedUrl.append("/").append(finalPathVariables.getOrDefault(varKey, segment));
                } else if (!segment.isEmpty()){ // Ajouter le segment normal s'il n'est pas vide
                    reconstructedUrl.append("/").append(segment);
                }
            }
            // Gérer le cas où le chemin était juste "/" ou vide - éviter "//" au début
            if (reconstructedUrl.toString().endsWith("{{baseUrl}}/") && pathSegments.isEmpty()){
                // Ne rien faire de plus
            } else if (reconstructedUrl.toString().equals("{{baseUrl}}") && !pathSegments.isEmpty()){
                // Cas où le premier segment ne commence pas par '/', on le force
                reconstructedUrl.delete(0, reconstructedUrl.length()); // Vide le builder
                reconstructedUrl.append("{{baseUrl}}"); // Remet la base
                String firstSegment = pathSegments.get(0).asText();
                reconstructedUrl.append("/").append(finalPathVariables.getOrDefault(firstSegment.startsWith(":") ? firstSegment.substring(1) : firstSegment, firstSegment));
                for(int i=1; i< pathSegments.size(); i++){ // Ajoute le reste
                    String seg = pathSegments.get(i).asText();
                    reconstructedUrl.append("/").append(finalPathVariables.getOrDefault(seg.startsWith(":") ? seg.substring(1) : seg, seg));
                }
            } else if (reconstructedUrl.toString().endsWith("{{baseUrl}}")){
                // Pas de path, ne rien ajouter
            }

        }

        // Ajouter les paramètres de requête s'il y en a
        if (!finalQueryParameters.isEmpty()) {
            reconstructedUrl.append("?");
            String queryPart = finalQueryParameters.stream()
                    .map(entry -> {
                        try {
                            // Encoder la clé ET la valeur pour l'URL
                            String encodedKey = URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8);
                            String encodedValue = URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8);
                            return encodedKey + "=" + encodedValue;
                        } catch (Exception e) {
                            // En cas d'erreur d'encodage (peu probable avec UTF-8), loguer et utiliser les valeurs brutes
                            System.err.println("WARN: Erreur d'encodage URL pour query param: " + entry.getKey() + "=" + entry.getValue() + " - " + e.getMessage());
                            return entry.getKey() + "=" + entry.getValue();
                        }
                    })
                    .collect(Collectors.joining("&")); // Joindre avec '&'
            reconstructedUrl.append(queryPart);
        }

        // Mettre à jour la valeur de 'raw' dans le JSON
        urlNode.put("raw", reconstructedUrl.toString());
        // System.out.println("INFO: URL 'raw' reconstruite pour '" + item.path("name").asText("") + "': " + reconstructedUrl.toString());

    } // Fin de replaceParameters

    /*hedha shih*/
   /*private void replacePathParameters(JsonNode item) {
        if (item == null || !item.has("request")) {
            return;
        }

        JsonNode requestNode = item.get("request");
        if (!requestNode.has("url") || !requestNode.has("method")) {
            return;
        }

        JsonNode urlNode = requestNode.get("url");
        String method = requestNode.get("method").asText();

        // Traitement des Path Variables
        if (urlNode.has("variable")) {
            for (JsonNode variable : urlNode.get("variable")) {
                if (variable.has("key") && variable.has("value")) {
                    String key = variable.get("key").asText();
                    String value = variable.get("value").asText();

                    if ("DELETE".equalsIgnoreCase(method) && (key.equalsIgnoreCase("id") || value.matches("\\d+"))) {
                        ((ObjectNode) variable).put("value", "4");
                    } else {
                        String replacementValue = getRealValueForType(value);
                        ((ObjectNode) variable).put("value", replacementValue);
                    }
                }
            }
        }

        // Traitement des Query Parameters
        if (urlNode.has("query")) {
            for (JsonNode queryParam : urlNode.get("query")) {
                if (queryParam.has("key") && queryParam.has("value")) {
                    String key = queryParam.get("key").asText();
                    String value = queryParam.get("value").asText();

                    List<ApiParameters> parameterList = apiParametersRepository.findByName(key);
                    if (parameterList != null && !parameterList.isEmpty() && "Aucun exemple disponible".equals(value)) {
                        String dataType = parameterList.get(0).getData_type(); // Prendre le premier résultat
                        String replacementValue = getRealValueForType(dataType);
                        ((ObjectNode) queryParam).put("value", replacementValue);
                    }
                }
            }
        }

        // Modification de l'URL si l'ID est dans le chemin
        if (urlNode.has("raw")) {
            String rawUrl = urlNode.get("raw").asText();
            if ("DELETE".equalsIgnoreCase(method)) {
                rawUrl = rawUrl.replaceAll("/\\d+($|\\?)", "/4$1");
                ((ObjectNode) urlNode).put("raw", rawUrl);
            }
        }
    }*/


    // Détermination des valeurs réelles pour les valeurs des paramétres
    /*private String getRealValueForType(String type) {
        if (type == null) {
            return "default_value"; // Valeur par défaut générique
        }

        // Extraire le type entre les cotes (< et >) si présent
        if (type.startsWith("<") && type.endsWith(">")) {
            type = type.substring(1, type.length() - 1);
        }

        switch (type.toLowerCase()) {
            case "long":
            case "integer":
            case "int":
                return "3";
            case "string":
                return "TestString";
            case "boolean":
            case "bool":
                return "true";
            case "double":
            case "float":
                return "99.99";
            case "datetime":
            case "date":
                return "2023-10-01T00:00:00Z";
            default:
                return "default_value";
        }
    }*/
    private String getRealValueForType(String typeHint) {
        if (typeHint == null) {
            return "default_value"; // Retourner une chaîne simple
        }

        String type = typeHint.toLowerCase().trim();
        if (type.startsWith("<") && type.endsWith(">")) {
            type = type.substring(1, type.length() - 1);
        }

        switch (type) {
            case "long":
            case "integer":
            case "int":
                return "3"; // Chaine "3"
            case "string":
                if (typeHint.toLowerCase().trim().equals("<string>")) return "TestString"; // Chaine "TestString"
                else return typeHint;
            case "boolean":
            case "bool":
                return "true"; // Chaine "true"
            case "double":
            case "float":
            case "number":
                return "99.99"; // Chaine "99.99"
            case "datetime":
            case "date":
                return "2024-01-01T10:00:00Z"; // Chaine ISO date
            case "file":
                return ""; // Chaine vide
            default:
                // Si c'était un placeholder inconnu
                if (typeHint.trim().startsWith("<") && typeHint.trim().endsWith(">")) return "unknown_placeholder_value";
                    // Sinon retourner la valeur originale telle quelle
                else return typeHint;
        }
    }

    // Modification dynamique du request body
    private void modifyRequestBody(JsonNode item) {
        if (item == null || !item.has("request")) {
            return;
        }

        JsonNode requestNode = item.get("request");
        if (!requestNode.has("body") || !requestNode.get("body").has("raw")) {
            return;
        }

        String body = requestNode.get("body").get("raw").asText();

        body = body.replaceAll("\"<long>\"", "3")
                .replaceAll("\"<integer>\"", "10")
                .replaceAll("\"<string>\"", "\"TestString\"")
                .replaceAll("\"<boolean>\"", "true")
                .replaceAll("\"<double>\"", "99.99")
                .replaceAll("\"<number>\"", "100")
                .replaceAll("\"<dateTime>\"", "\"2023-10-01T00:00:00Z\"");

        ((ObjectNode) requestNode.get("body")).put("raw", body);
    }


    /*private void addTestsToRequest(JsonNode item) {
        if (item == null || !item.has("request")) {
            return;
        }

        JsonNode requestNode = item.get("request");
        if (!requestNode.has("url") || !requestNode.has("method")) {
            return;
        }

        if (!item.has("response") || !item.get("response").isArray()) {
            return;
        }

        List<Integer> statusCodes = new ArrayList<>();
        Map<Integer, JsonNode> responseSchemas = new HashMap<>();

        for (JsonNode responseNode : item.get("response")) {
            if (!responseNode.has("code") || !responseNode.has("body")) {
                continue;
            }

            int statusCode = responseNode.get("code").asInt();
            String responseBody = responseNode.get("body").asText();
            statusCodes.add(statusCode);

            if (!responseBody.isEmpty()) {
                try {
                    JsonNode responseJson = objectMapper.readTree(responseBody);
                    responseSchemas.put(statusCode, responseJson);
                } catch (Exception e) {
                    System.out.println("Impossible de parser la réponse JSON pour le statut " + statusCode);
                }
            }
        }

        StringBuilder testScript = new StringBuilder();
        testScript.append("pm.test(\"Vérification des réponses possibles\", function () {\n")
                .append("    pm.expect([").append(statusCodes.toString().replaceAll("[\\[\\]]", ""))
                .append("]).to.include(pm.response.code);\n");
        testScript.append("});\n");

        // Vérifier chaque schéma de retour selon son statut
        for (Map.Entry<Integer, JsonNode> entry : responseSchemas.entrySet()) {
            int statusCode = entry.getKey();
            JsonNode schema = entry.getValue();

            testScript.append("pm.test(\"Vérification du schéma de réponse pour le statut ").append(statusCode).append("\", function () {\n")
                    .append("    if (pm.response.code === ").append(statusCode).append(") {\n")
                    .append("        const jsonResponse = pm.response.json();\n");

            testScript.append("        if (Array.isArray(jsonResponse)) {\n")
                    .append("            pm.expect(jsonResponse).to.be.an('array').and.not.be.empty;\n")
                    .append("            jsonResponse.forEach(function(item) {\n");

            for (String field : getSchemaFields(schema)) {
                testScript.append("                pm.expect(item).to.have.property(\"").append(field).append("\");\n");
            }

            testScript.append("            });\n")
                    .append("        } else {\n");

            for (String field : getSchemaFields(schema)) {
                testScript.append("            pm.expect(jsonResponse).to.have.property(\"").append(field).append("\");\n");
            }

            testScript.append("        }\n    }\n");
            testScript.append("});\n");
        }

        ObjectNode eventNode = objectMapper.createObjectNode();
        eventNode.put("listen", "test");

        ObjectNode scriptNode = objectMapper.createObjectNode();
        scriptNode.put("type", "text/javascript");
        scriptNode.put("exec", testScript.toString());

        eventNode.set("script", scriptNode);
        ArrayNode eventArray = objectMapper.createArrayNode();
        eventArray.add(eventNode);

        ((ObjectNode) item).set("event", eventArray);
    }*/
    /*si une requete contient un seul reponse on ne generer pas de test*/
    private void addTestsToRequest(JsonNode item) {
        if (item == null || !item.has("request")) {
            return;
        }

        JsonNode requestNode = item.get("request");
        if (!requestNode.has("url") || !requestNode.has("method")) {
            return;
        }

        if (!item.has("response") || !item.get("response").isArray()) {
            return;
        }

        JsonNode responses = item.get("response");
        if (responses.size() <= 1) {
            // Ne pas ajouter de tests si une seule réponse
            return;
        }

        List<Integer> statusCodes = new ArrayList<>();
        Map<Integer, JsonNode> responseSchemas = new HashMap<>();

        for (JsonNode responseNode : responses) {
            if (!responseNode.has("code") || !responseNode.has("body")) {
                continue;
            }

            int statusCode = responseNode.get("code").asInt();
            String responseBody = responseNode.get("body").asText();
            statusCodes.add(statusCode);

            if (!responseBody.isEmpty()) {
                try {
                    JsonNode responseJson = objectMapper.readTree(responseBody);
                    responseSchemas.put(statusCode, responseJson);
                } catch (Exception e) {
                    System.out.println("Impossible de parser la réponse JSON pour le statut " + statusCode);
                }
            }
        }

        StringBuilder testScript = new StringBuilder();
        testScript.append("pm.test(\"Vérification des réponses possibles\", function () {\n")
                .append("    pm.expect([").append(statusCodes.toString().replaceAll("[\\[\\]]", ""))
                .append("]).to.include(pm.response.code);\n");
        testScript.append("});\n");

        for (Map.Entry<Integer, JsonNode> entry : responseSchemas.entrySet()) {
            int statusCode = entry.getKey();
            JsonNode schema = entry.getValue();

            testScript.append("pm.test(\"Vérification du schéma de réponse pour le statut ").append(statusCode).append("\", function () {\n")
                    .append("    if (pm.response.code === ").append(statusCode).append(") {\n")
                    .append("        const jsonResponse = pm.response.json();\n");

            testScript.append("        if (Array.isArray(jsonResponse)) {\n")
                    .append("            pm.expect(jsonResponse).to.be.an('array').and.not.be.empty;\n")
                    .append("            jsonResponse.forEach(function(item) {\n");

            for (String field : getSchemaFields(schema)) {
                testScript.append("                pm.expect(item).to.have.property(\"").append(field).append("\");\n");
            }

            testScript.append("            });\n")
                    .append("        } else {\n");

            for (String field : getSchemaFields(schema)) {
                testScript.append("            pm.expect(jsonResponse).to.have.property(\"").append(field).append("\");\n");
            }

            testScript.append("        }\n    }\n");
            testScript.append("});\n");
        }

        ObjectNode eventNode = objectMapper.createObjectNode();
        eventNode.put("listen", "test");

        ObjectNode scriptNode = objectMapper.createObjectNode();
        scriptNode.put("type", "text/javascript");
        scriptNode.put("exec", testScript.toString());

        eventNode.set("script", scriptNode);
        ArrayNode eventArray = objectMapper.createArrayNode();
        eventArray.add(eventNode);

        ((ObjectNode) item).set("event", eventArray);
    }


    // Détermination des valeurs par défaut pour les types dynamiques
    private String getDefaultValueForType(String type) {
        switch (type.toLowerCase()) {
            case "<long>":
            case "long":
            case "id":
                return "3";
            case "<integer>":
            case "int":
                return "10";
            case "<string>":
            case "string":
            case "name":
                return "\"TestString\"";
            case "<boolean>":
            case "bool":
                return "true";
            case "<double>":
            case "float":
                return "99.99";
            case "<number>":
                return "100";
            case "<datetime>":
                return "\"2023-10-01T00:00:00Z\"";
            default:
                return "\"default_value\"";
        }
    }

    // Récupérer les champs du schéma JSON
    private List<String> getSchemaFields(JsonNode schema) {
        List<String> fields = new ArrayList<>();
        schema.fieldNames().forEachRemaining(fields::add);
        return fields;
    }

    // Sauvegarde de la collection dans la partie ressources
    private void saveCollectionToResources(JsonNode collectionNode) throws Exception {
        File targetFile = new File(POSTMAN_COLLECTION_PATH);
        try (FileWriter writer = new FileWriter(targetFile)) {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(writer, collectionNode);
        }
    }
}


/*import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Importer vos entités et repositories (Assurez-vous que les packages sont corrects)
import com.example.api_tierces.model.ApiParameters;
import com.example.api_tierces.repository.ApiParametersRepository;
import com.example.api_tierces.repository.ApiResponseRepository;
import com.example.api_tierces.repository.ApiRepository;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Importer vos entités et repositories (Assurez-vous que les packages sont corrects)
import com.example.api_tierces.model.ApiParameters;
import com.example.api_tierces.repository.ApiParametersRepository;
// Supposons que ces autres repos sont injectés si nécessaire ailleurs ou pas du tout utilisés ici
// import com.example.api_tierces.repository.ApiResponseRepository;
// import com.example.api_tierces.repository.ApiRepository;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
/*hedha shih zeda*/
/*@Service
public class PostmanProcessingService {

    @Autowired
    private ObjectMapper objectMapper;

    // Injection des repositories (commenté si non utilisé directement ici, mais gardé pour contexte)
    // @Autowired
    // private ApiResponseRepository apiResponseRepository;
    // @Autowired
    // private ApiRepository apiRepository;
    @Autowired
    private ApiParametersRepository apiParametersRepository; // Utilisé pour les query params

    // Utiliser un nom différent pour le fichier de sortie pour éviter d'écraser l'original pendant les tests
    private static final String POSTMAN_COLLECTION_OUTPUT_PATH = "src/main/resources/postman_collection.json";

    @Transactional // Garder si des opérations DB sont atomiques
    public String processPostmanCollection(String postmanCollectionJson) {
        try {
            JsonNode collectionNode = objectMapper.readTree(postmanCollectionJson);
            JsonNode itemsNode = collectionNode.path("item"); // Utiliser path pour la sécurité

            if (itemsNode.isMissingNode() || !itemsNode.isArray()) {
                return "Format de collection invalide : champ 'item' manquant ou n'est pas un tableau.";
            }

            processItems(itemsNode);

            saveCollectionToResources(collectionNode);
            return "Collection Postman traitée et enregistrée avec succès dans " + POSTMAN_COLLECTION_OUTPUT_PATH;
        } catch (IOException e) {
            System.err.println("Erreur I/O lors du traitement/sauvegarde : " + e.getMessage());
            return "Erreur I/O lors du traitement de la collection Postman : " + e.getMessage();
        } catch (Exception e) {
            System.err.println("Erreur générale lors du traitement de la collection Postman : " + e.getMessage());
            e.printStackTrace();
            return "Erreur générale lors du traitement de la collection Postman : " + e.getMessage();
        }
    }

    private void processItems(JsonNode itemsNode) {
        if (itemsNode == null || !itemsNode.isArray()) {
            return;
        }

        for (JsonNode item : itemsNode) {
            if (item.has("request")) { // Traiter seulement si c'est une requête
                // *** Étape 1 : Prétraiter les réponses (gestion du 'default') ***
                preprocessResponses(item);

                // *** Étape 2 : Remplacer les placeholders dans les paramètres et le corps ***
                replaceParameters(item); // Gère path et query params
                modifyRequestBody(item); // Gère le request body (raw, formdata, urlencoded)

                // *** Étape 3 : Ajouter les scripts de test basés sur les réponses prétraitées ***
                addTestsToRequest(item);
            }

            // Appel récursif pour les sous-dossiers/items
            if (item.has("item")) {
                processItems(item.path("item"));
            }
        }
    }

    private void preprocessResponses(JsonNode item) {
        if (!item.has("response") || !item.get("response").isArray()) {
            return;
        }
        ArrayNode responsesNode = (ArrayNode) item.get("response");
        if (responsesNode.isEmpty()) {
            return;
        }

        boolean hasActual200 = false;
        int defaultCandidateIndex = -1;
        JsonNode defaultCandidateNode = null;
        boolean defaultDetectedByCodeString = false;

        // --- Boucle 1 : Trouver un 200 existant ET identifier le MEILLEUR candidat "default" ---
        for (int i = 0; i < responsesNode.size(); i++) {
            JsonNode response = responsesNode.get(i);
            if (!response.isObject()) continue;

            int numericCode = -999;
            boolean isExplicitDefaultCode = false;
            boolean isProbableDefaultName = response.path("name").asText("").toLowerCase().contains("default");

            // Analyser le champ 'code'
            if (response.has("code")) {
                JsonNode codeNode = response.get("code");
                if (codeNode.isNumber()) {
                    numericCode = codeNode.asInt();
                } else if (codeNode.isTextual() && "default".equalsIgnoreCase(codeNode.asText())) {
                    isExplicitDefaultCode = true;
                    numericCode = 0; // Traiter comme 0 pour la logique
                    defaultDetectedByCodeString = true;
                }
            }

            if (numericCode == 200) {
                hasActual200 = true;
            }

            boolean isDefaultCandidate = isExplicitDefaultCode || numericCode == 0 || isProbableDefaultName;

            if (isDefaultCandidate && defaultCandidateIndex == -1) {
                if (numericCode != 200) {
                    defaultCandidateIndex = i;
                    defaultCandidateNode = response;
                }
            }
        }

        // --- Logique de traitement ---
        if (defaultCandidateIndex != -1 && defaultCandidateNode != null) {
            if (hasActual200) {
                responsesNode.remove(defaultCandidateIndex);
                String detectionMethod = defaultDetectedByCodeString ? "code=\"default\"" : "name/code=0";
                System.out.println("INFO: Item '" + item.path("name").asText("") + "' - Réponse 'default' (détectée via " + detectionMethod + " à l'index " + defaultCandidateIndex + ") supprimée car une réponse 200 existe déjà.");
            } else {
                if (defaultCandidateNode.isObject()) {
                    ObjectNode modifiableDefault = (ObjectNode) defaultCandidateNode;
                    int originalCode = modifiableDefault.path("code").asInt(-999);
                    String originalName = modifiableDefault.path("name").asText("");
                    String detectionMethod = defaultDetectedByCodeString ? "code=\"default\"" : "name/code=0";

                    modifiableDefault.put("code", 200); // Nombre 200
                    modifiableDefault.put("status", "OK");

                    if (!originalName.toLowerCase().contains("ok") && !originalName.toLowerCase().contains("success")) {
                        modifiableDefault.put("name", originalName + " (Processed as 200 OK)");
                    } else {
                        modifiableDefault.put("name", "OK (Processed from Default)");
                    }

                    System.out.println("INFO: Item '" + item.path("name").asText("") + "' - Réponse 'default' (détectée via " + detectionMethod + " à l'index " + defaultCandidateIndex + ", code original approx. " + originalCode + ") transformée en réponse 200.");
                } else {
                    System.err.println("ERREUR: Item '" + item.path("name").asText("") + "' - Noeud 'default' trouvé à l'index " + defaultCandidateIndex + " n'est pas un objet modifiable.");
                }
            }
        }
    } // Fin de preprocessResponses


    private void replaceParameters(JsonNode item) {
        if (item == null || !item.has("request")) return;
        JsonNode requestNode = item.get("request");
        if (!requestNode.has("url") || !requestNode.has("method")) return;

        JsonNode urlNode = requestNode.get("url");
        String method = requestNode.get("method").asText();

        // Traitement des Path Variables
        if (urlNode.has("variable") && urlNode.get("variable").isArray()) {
            for (JsonNode variable : urlNode.get("variable")) {
                if (variable.has("key") && variable.isObject()) {
                    String key = variable.path("key").asText();
                    String value = variable.path("value").asText("");
                    String typeHint = variable.has("type") ? variable.path("type").asText("") : value;

                    if ("DELETE".equalsIgnoreCase(method) && (key.equalsIgnoreCase("id") || typeHint.contains("long") || typeHint.contains("int") || value.matches("\\d+"))) {
                        ((ObjectNode) variable).put("value", "4");
                    } else {
                        String replacementValue = getRealValueForType(typeHint);
                        ((ObjectNode) variable).put("value", replacementValue);
                    }
                }
            }
        }

        // Traitement des Query Parameters
        if (urlNode.has("query") && urlNode.get("query").isArray()) {
            for (JsonNode queryParam : urlNode.get("query")) {
                if (queryParam.has("key") && queryParam.isObject()) {
                    String key = queryParam.path("key").asText();
                    String value = queryParam.path("value").asText("");
                    String typeHint = queryParam.has("type") ? queryParam.path("type").asText("") : value;

                    // Remplacer la valeur si c'est un placeholder <...> ou type file
                    String replacementValue = getRealValueForType(typeHint);
                    if (value.startsWith("<") || typeHint.equalsIgnoreCase("file") || "Aucun exemple disponible".equals(value) || value.isEmpty()) {
                        ((ObjectNode) queryParam).put("value", replacementValue);
                    }
                }
            }
        }

        // Modification de l'URL brute pour DELETE
        if (urlNode.has("raw")) {
            String rawUrl = urlNode.get("raw").asText();
            if ("DELETE".equalsIgnoreCase(method)) {
                rawUrl = rawUrl.replaceAll(":\\w+($|\\?|/)", "4$1"); // :variableName -> 4
                rawUrl = rawUrl.replaceAll("/\\d+($|\\?|/)", "/4$1"); // /nombre -> /4
                ((ObjectNode) urlNode).put("raw", rawUrl);
            }
        }
    }

    private String getRealValueForType(String typeHint) {
        if (typeHint == null) {
            return "";
        }

        String type = typeHint.toLowerCase().trim();
        if (type.startsWith("<") && type.endsWith(">")) {
            type = type.substring(1, type.length() - 1);
        }

        switch (type) {
            case "long":
            case "integer":
            case "int":
                return "3";
            case "string":
                if (typeHint.toLowerCase().trim().equals("<string>")) return "TestString";
                else return typeHint;
            case "boolean":
            case "bool":
                return "true";
            case "double":
            case "float":
            case "number":
                return "99.99";
            case "datetime":
            case "date":
                return "2024-01-01T10:00:00Z";
            case "file":
                return "";
            default:
                if (typeHint.trim().startsWith("<") && typeHint.trim().endsWith(">")) return "unknown_type";
                else return typeHint;
        }
    }

    private void modifyRequestBody(JsonNode item) {
        if (item == null || !item.has("request") || !item.get("request").has("body")) return;

        JsonNode requestNode = item.get("request");
        JsonNode bodyNode = requestNode.get("body");

        if (bodyNode.has("raw")) {
            String rawBody = bodyNode.path("raw").asText();
            if (rawBody != null && !rawBody.isEmpty()) {
                try {
                    JsonNode parsedBody = objectMapper.readTree(rawBody);
                    JsonNode modifiedBody = replacePlaceholdersInJson(parsedBody);
                    String newRawBody = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(modifiedBody);
                    ((ObjectNode) bodyNode).put("raw", newRawBody);
                } catch (IOException e) {
                    System.err.println("Body raw n'est pas JSON valide ou erreur IO, item: " + item.path("name").asText() + " - " + e.getMessage());
                }
            }
        } else if (bodyNode.has("formdata") && bodyNode.get("formdata").isArray()) {
            for (JsonNode formParam : bodyNode.get("formdata")) {
                if (formParam.isObject() && formParam.has("type")) {
                    String paramType = formParam.path("type").asText("");
                    String originalValue = formParam.path("value").asText("");

                    if ("file".equalsIgnoreCase(paramType)) {
                        ((ObjectNode) formParam).put("value", "");
                    } else if ("text".equalsIgnoreCase(paramType)) {
                        if (originalValue.trim().startsWith("<") && originalValue.trim().endsWith(">")) {
                            String replacement = getRealValueForType(originalValue);
                            ((ObjectNode) formParam).put("value", replacement);
                        }
                    }
                }
            }
        } else if (bodyNode.has("urlencoded") && bodyNode.get("urlencoded").isArray()) {
            for (JsonNode encodedParam : bodyNode.get("urlencoded")) {
                if (encodedParam.isObject() && encodedParam.has("value")) {
                    String originalValue = encodedParam.path("value").asText("");
                    if (originalValue.trim().startsWith("<") && originalValue.trim().endsWith(">")) {
                        String replacement = getRealValueForType(originalValue);
                        ((ObjectNode) encodedParam).put("value", replacement);
                    }
                }
            }
        }
    }

    private JsonNode replacePlaceholdersInJson(JsonNode node) {
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                field.setValue(replacePlaceholdersInJson(field.getValue()));
            }
            return objectNode;
        } else if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            for (int i = 0; i < arrayNode.size(); i++) {
                arrayNode.set(i, replacePlaceholdersInJson(arrayNode.get(i)));
            }
            return arrayNode;
        } else if (node.isTextual()) {
            String textValue = node.asText();
            if (textValue.trim().startsWith("<") && textValue.trim().endsWith(">")) {
                String realValueString = getRealValueForType(textValue);
                try {
                    if (realValueString.matches("-?\\d+")) {
                        return objectMapper.convertValue(Long.parseLong(realValueString), JsonNode.class);
                    } else if (realValueString.matches("-?\\d*\\.\\d+")) {
                        return objectMapper.convertValue(Double.parseDouble(realValueString), JsonNode.class);
                    } else if ("true".equalsIgnoreCase(realValueString) || "false".equalsIgnoreCase(realValueString)) {
                        return objectMapper.convertValue(Boolean.parseBoolean(realValueString), JsonNode.class);
                    }
                } catch (NumberFormatException | ClassCastException ignored) {  }
                return objectMapper.convertValue(realValueString, JsonNode.class);
            }
        }
        return node;
    }


    private void addTestsToRequest(JsonNode item) {
        if (item == null || !item.has("request") || !item.has("response") || !item.get("response").isArray()) {
            return;
        }
        ArrayNode responsesNode = (ArrayNode) item.get("response");
        if (responsesNode.isEmpty()) {
            return;
        }

        List<Integer> statusCodes = new ArrayList<>();
        Map<Integer, JsonNode> responseSchemas = new HashMap<>();

        for (JsonNode responseNode : responsesNode) {
            if (!responseNode.has("code")) continue;
            int statusCode = responseNode.path("code").asInt(0);
            if (statusCode <= 0) continue;

            statusCodes.add(statusCode);
            String responseBody = responseNode.path("body").asText(null);

            if (responseBody != null && !responseBody.trim().isEmpty()) {
                try {
                    JsonNode responseJson = objectMapper.readTree(responseBody);
                    responseSchemas.put(statusCode, responseJson);
                } catch (IOException e) {
                    System.err.println("Impossible de parser body (status " + statusCode + ") comme JSON. Item: '" + item.path("name").asText() + "'. Erreur: " + e.getMessage());
                }
            }
        }

        if (statusCodes.isEmpty()) {
            return;
        }

        StringBuilder testScript = new StringBuilder();
        List<Integer> distinctStatusCodes = statusCodes.stream().distinct().collect(Collectors.toList());
        boolean only200 = distinctStatusCodes.size() == 1 && distinctStatusCodes.get(0) == 200;

        if (!only200) {
            String expectedCodes = distinctStatusCodes.stream().map(String::valueOf).collect(Collectors.joining(", "));
            testScript.append("pm.test(\"Status code is one of [" + expectedCodes + "]\", function () {\n");
            testScript.append("    pm.expect(pm.response.code).to.be.oneOf([").append(expectedCodes).append("]);\n");
            testScript.append("});\n\n");
        } else {
            testScript.append("pm.test(\"Status code is 200\", function () {\n");
            testScript.append("    pm.response.to.have.status(200);\n");
            testScript.append("});\n\n");
        }

        for (Map.Entry<Integer, JsonNode> entry : responseSchemas.entrySet()) {
            int statusCode = entry.getKey();
            JsonNode schema = entry.getValue();

            testScript.append("// Tests for status code ").append(statusCode).append("\n");
            testScript.append("if (pm.response.code === ").append(statusCode).append(") {\n");
            testScript.append("    pm.test(\"[").append(statusCode).append("] Response body is valid JSON\", function () {\n");
            testScript.append("        pm.response.to.have.jsonBody();\n");
            testScript.append("    });\n\n");

            testScript.append("    pm.test(\"[").append(statusCode).append("] Response body structure check\", function () {\n");
            testScript.append("        const jsonResponse = pm.response.json();\n");
            if (schema.isArray()) {
                testScript.append("        pm.expect(jsonResponse).to.be.an('array');\n");
                if (!schema.isEmpty()) {
                    JsonNode firstElement = schema.get(0);
                    if (firstElement.isObject()) {
                        testScript.append("        if (jsonResponse.length > 0) {\n");
                        // *** APPEL à getSchemaFields ***
                        List<String> fields = getSchemaFields(firstElement);
                        if (!fields.isEmpty()) {
                            testScript.append("            let firstItem = jsonResponse[0];\n");
                            fields.forEach(field -> testScript.append("            pm.expect(firstItem).to.have.property('").append(field.replace("'", "\\'")).append("');\n"));
                        }
                        testScript.append("        }\n");
                    }
                }
            } else if (schema.isObject()) {
                testScript.append("        pm.expect(jsonResponse).to.be.an('object');\n");
                // *** APPEL à getSchemaFields ***
                List<String> fields = getSchemaFields(schema);
                if (!fields.isEmpty()) {
                    fields.forEach(field -> testScript.append("        pm.expect(jsonResponse).to.have.property('").append(field.replace("'", "\\'")).append("');\n"));
                }
            } else {
                // *** APPEL à getJsonType ***
                String expectedType = getJsonType(schema);
                testScript.append("        pm.expect(jsonResponse).to.be.a('").append(expectedType).append("');\n");
            }
            testScript.append("    });\n");
            testScript.append("}\n\n");
        }

        ObjectNode eventNode = objectMapper.createObjectNode();
        eventNode.put("listen", "test");
        ObjectNode scriptNode = objectMapper.createObjectNode();
        scriptNode.put("type", "text/javascript");
        ArrayNode execArray = objectMapper.createArrayNode();
        Arrays.stream(testScript.toString().split("\n"))
                .map(String::trim)
                .forEach(execArray::add);
        scriptNode.set("exec", execArray);
        eventNode.set("script", scriptNode);

        ArrayNode eventArray;
        if (item.has("event") && item.get("event").isArray()) {
            eventArray = (ArrayNode) item.get("event");
            for (int i = eventArray.size() - 1; i >= 0; i--) {
                if ("test".equals(eventArray.get(i).path("listen").asText(""))) {
                    eventArray.remove(i);
                }
            }
        } else {
            eventArray = objectMapper.createArrayNode();
            ((ObjectNode) item).set("event", eventArray);
        }
        eventArray.add(eventNode);
    }

    // *** MÉTHODE UTILITAIRE MANQUANTE ***
    // Helper: Récupérer les champs d'un objet JSON
    private List<String> getSchemaFields(JsonNode schema) {
        List<String> fields = new ArrayList<>();
        if (schema != null && schema.isObject()) {
            schema.fieldNames().forEachRemaining(fields::add);
        }
        return fields;
    }

    // *** MÉTHODE UTILITAIRE MANQUANTE ***
    // Helper: Obtenir le type JS pour les assertions
    private String getJsonType(JsonNode node) {
        if (node == null || node.isNull()) return "null";
        if (node.isTextual()) return "string";
        if (node.isBoolean()) return "boolean";
        if (node.isNumber()) return "number";
        if (node.isArray()) return "array";
        if (node.isObject()) return "object";
        return "undefined";
    }

    // Sauvegarde de la collection
    private void saveCollectionToResources(JsonNode collectionNode) throws IOException {
        File targetFile = new File(POSTMAN_COLLECTION_OUTPUT_PATH);
        File parentDir = targetFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new IOException("Impossible de créer les dossiers parents: " + parentDir.getAbsolutePath());
            }
        }
        try (FileWriter writer = new FileWriter(targetFile)) {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(writer, collectionNode);
            System.out.println("Collection traitée enregistrée dans : " + targetFile.getAbsolutePath());
        }
    }
}*/




















 // Assurez-vous que le package est correct
/*hedha yetreti barcha parameters*/
/*import com.example.api_tierces.model.ApiParameters; // Assurez-vous que l'import est correct
import com.example.api_tierces.repository.ApiParametersRepository; // Assurez-vous que l'import est correct
import com.example.api_tierces.repository.ApiResponseRepository;   // Assurez-vous que l'import est correct
import com.example.api_tierces.repository.ApiRepository;         // Assurez-vous que l'import est correct
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Import pour @Transactional

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List; // <<< Import ajouté/vérifié
import java.util.Map;

// Imports potentiels manquants (à ajouter si nécessaire)
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

@Service
public class PostmanProcessingService {

    // private static final Logger logger = LoggerFactory.getLogger(PostmanProcessingService.class); // Décommentez si vous utilisez un logger

    @Autowired
    private ObjectMapper objectMapper;

    // Supposons que ces repositories sont injectés (comme dans votre code précédent)
    // @Autowired
    // private ApiResponseRepository apiResponseRepository;
    // @Autowired
    // private ApiRepository apiRepository;

    @Autowired
    private ApiParametersRepository apiParametersRepository; // Ce repository est crucial ici

    private static final String POSTMAN_COLLECTION_PATH = "src/main/resources/postman_collection.json";

    // Méthode principale de traitement
    @Transactional // Peut être utile si vous faites d'autres opérations BDD ici
    public String processPostmanCollection(String postmanCollectionJson) {
        try {
            JsonNode collectionNode = objectMapper.readTree(postmanCollectionJson);
            JsonNode infoNode = collectionNode.get("info"); // Garder les infos
            JsonNode itemsNode = collectionNode.get("item");
            JsonNode variableNode = collectionNode.get("variable"); // Garder les variables

            // Créer un nouveau noeud racine pour reconstruire la collection
            ObjectNode updatedCollectionNode = objectMapper.createObjectNode();
            if (infoNode != null) {
                updatedCollectionNode.set("info", infoNode);
            }

            // Traiter les items (c'est ici que les modifications auront lieu)
            if (itemsNode != null && itemsNode.isArray()) {
                processItems(itemsNode); // Modifie itemsNode en place
                updatedCollectionNode.set("item", itemsNode); // Ajouter les items modifiés
            }

            if (variableNode != null) {
                updatedCollectionNode.set("variable", variableNode);
            }

            // Sauvegarder la collection modifiée
            saveCollectionToResources(updatedCollectionNode); // Sauvegarder le noeud mis à jour
            return "Collection Postman mise à jour et enregistrée avec succès !";
        } catch (Exception e) {
            System.err.println("Erreur détaillée lors du traitement Postman: " + e.getMessage());
            e.printStackTrace(); // Imprime la stack trace pour le débogage serveur
            // logger.error("Erreur lors du traitement de la collection Postman", e); // Utilisez un logger si disponible
            // Retourner un message d'erreur plus utile potentiellement
            return "Erreur lors du traitement de la collection Postman : " + e.getClass().getSimpleName() + " - " + e.getMessage();
        }
    }

    // Parcourt récursif des éléments Postman
    private void processItems(JsonNode itemsNode) {
        if (itemsNode == null || !itemsNode.isArray()) {
            return;
        }

        for (JsonNode item : itemsNode) {
            // Appeler les méthodes de modification qui opèrent sur l'item
            replacePathParameters(item);
            modifyRequestBody(item);
            addTestsToRequest(item); // S'assure que cette méthode modifie bien 'item' si nécessaire

            // Traiter récursivement les sous-items
            if (item.has("item") && item.get("item").isArray()) { // Vérifier que 'item' est bien un tableau
                processItems(item.get("item"));
            }
        }
    }

    // Remplacement dynamique des paramètres dans l'URL
    private void replacePathParameters(JsonNode item) {
        if (item == null || !item.has("request")) {
            return;
        }

        JsonNode requestNode = item.get("request");
        if (!requestNode.has("url") || !requestNode.has("method")) {
            return;
        }

        JsonNode urlNode = requestNode.get("url");
        // Vérifier si urlNode est un objet avant d'essayer de le caster ou d'accéder à ses champs
        if (!urlNode.isObject()) {
            System.err.println("Avertissement: Le noeud 'url' n'est pas un objet JSON valide dans l'item: " + item.path("name").asText("sans nom"));
            return;
        }

        String method = requestNode.get("method").asText();

        // Traitement des Path Variables
        if (urlNode.has("variable") && urlNode.get("variable").isArray()) {
            for (JsonNode variable : urlNode.get("variable")) {
                // Vérifier que variable est un objet
                if (variable.isObject() && variable.has("key") && variable.has("value")) {
                    String key = variable.get("key").asText();
                    String value = variable.get("value").asText();

                    // Si la méthode est DELETE, on force l'ID à 4
                    if ("DELETE".equalsIgnoreCase(method) && (key.equalsIgnoreCase("id") || key.toLowerCase().endsWith("id"))) { // Rendre la détection d'ID plus robuste
                        ((ObjectNode) variable).put("value", "4"); // Force la valeur à "4" (string)
                        System.out.println("Info: Forçage de la variable de chemin '" + key + "' à '4' pour la méthode DELETE.");
                    } else {
                        // Remplacer les placeholders par des valeurs réelles adaptées au type
                        String replacementValue = getRealValueForType(value); // value est le placeholder type <type>
                        ((ObjectNode) variable).put("value", replacementValue);
                    }
                }
            }
        }

        // Traitement des Query Parameters
        if (urlNode.has("query") && urlNode.get("query").isArray()) {
            for (JsonNode queryParam : urlNode.get("query")) {
                // Vérifier que queryParam est un objet
                if (queryParam.isObject() && queryParam.has("key") && queryParam.has("value")) {
                    String key = queryParam.get("key").asText();
                    String value = queryParam.get("value").asText();

                    // ---- DEBUT DE LA CORRECTION ----
                    // Récupérer le type du paramètre à partir de la base de données EN UTILISANT LA LISTE
                    List<ApiParameters> parameters = apiParametersRepository.findByName(key); // <<< APPEL CORRIGÉ

                    ApiParameters parameterToUse = null; // Variable pour stocker le paramètre choisi

                    if (parameters != null && !parameters.isEmpty()) {
                        // Si plusieurs paramètres sont trouvés, logguer un avertissement et prendre le premier
                        if (parameters.size() > 1) {
                            System.err.println("Avertissement: Plusieurs ("+ parameters.size() +") paramètres trouvés pour le nom de requête '" + key + "'. Utilisation du premier trouvé (ID: " + parameters.get(0).getId() + "). Assurez-vous que c'est le comportement souhaité.");
                            // Vous pourriez ajouter une logique plus fine ici si nécessaire,
                            // par exemple en essayant de faire correspondre avec l'API parente si vous avez cette info.
                        }
                        parameterToUse = parameters.get(0); // On choisit le premier de la liste
                    } else {
                        // Optionnel: Logguer si aucun paramètre n'est trouvé en BDD
                        // System.out.println("Info: Aucun paramètre défini en base de données pour le nom de requête '" + key + "'.");
                    }
                    // ---- FIN DE LA CORRECTION ----


                    // Utiliser parameterToUse (qui peut être null si non trouvé)
                    if (parameterToUse != null && "Aucun exemple disponible".equals(value)) {
                        // Remplacer par une valeur réelle adaptée au type trouvé en BDD
                        String replacementValue = getRealValueForType(parameterToUse.getData_type());
                        ((ObjectNode) queryParam).put("value", replacementValue);
                    } else if (parameterToUse == null && "Aucun exemple disponible".equals(value)) {
                        // Cas où on a le placeholder mais pas de définition en BDD pour déduire le type
                        System.err.println("Avertissement: Paramètre de requête '" + key + "' non trouvé en BDD. Impossible de remplacer la valeur placeholder 'Aucun exemple disponible' par une valeur typée.");
                        // Option: mettre une valeur par défaut très générique ?
                        // ((ObjectNode) queryParam).put("value", "valeur_par_defaut_inconnue");
                    }
                    // Si 'value' n'est pas "Aucun exemple disponible", on le laisse tel quel (il a déjà une valeur).
                }
            }
        }

        // Modification de l'URL brute si l'ID est dans le chemin pour DELETE
        // Ceci est risqué car ça suppose une structure d'URL spécifique
        if ("DELETE".equalsIgnoreCase(method) && urlNode.has("raw")) {
            String rawUrl = urlNode.get("raw").asText();
            // Essayer de remplacer un segment numérique après un '/' et avant la fin ou un '?'
            String updatedRawUrl = rawUrl.replaceAll("/\\d+(\\?|$)", "/4$1");
            if (!rawUrl.equals(updatedRawUrl)) {
                System.out.println("Info: Mise à jour de l'URL brute pour DELETE : '" + rawUrl + "' -> '" + updatedRawUrl + "'");
                ((ObjectNode) urlNode).put("raw", updatedRawUrl);
            }

            // Mise à jour des segments de chemin également si l'URL brute a changé
            if (!rawUrl.equals(updatedRawUrl) && urlNode.has("path") && urlNode.get("path").isArray()) {
                ArrayNode pathArray = (ArrayNode) urlNode.get("path");
                for (int i = 0; i < pathArray.size(); i++) {
                    if (pathArray.get(i).asText().matches("^\\d+$")) { // Si un segment est juste un nombre
                        pathArray.set(i, objectMapper.getNodeFactory().textNode("4"));
                        System.out.println("Info: Mise à jour du segment de chemin à l'index " + i + " à '4'");
                        break; // Supposer qu'il n'y a qu'un ID numérique dans le chemin
                    }
                }
            }
        }
    }

    // Détermination des valeurs réelles pour les placeholders de type (ex: <string>)
    private String getRealValueForType(String placeholderType) {
        if (placeholderType == null || placeholderType.isBlank()) {
            return "default_value_type_inconnu"; // Valeur par défaut si type null/vide
        }

        String type;
        // Extraire le type entre les cotes (< et >) si présent
        if (placeholderType.startsWith("<") && placeholderType.endsWith(">") && placeholderType.length() > 2) {
            type = placeholderType.substring(1, placeholderType.length() - 1).toLowerCase().trim();
        } else {
            // Si ce n'est pas un placeholder <type>, on suppose que c'est déjà le type lui-même
            type = placeholderType.toLowerCase().trim();
        }


        switch (type) {
            case "long":
            case "integer":
            case "int":
                return "3"; // Retourne une String contenant le nombre
            case "string":
                return "TestStringValue"; // Retourne une String
            case "boolean":
            case "bool":
                return "true"; // Retourne une String "true"
            case "double":
            case "float":
            case "number": // OpenAPI 'number' peut être double/float
                return "99.99"; // Retourne une String contenant le nombre décimal
            case "datetime":
            case "date-time":
            case "date":
                return "2024-01-01T10:00:00Z"; // Retourne une String de date ISO
            default:
                System.err.println("Avertissement: Type inconnu '" + type + "' (venant de '" + placeholderType + "'). Utilisation d'une valeur par défaut générique.");
                return "valeur_pour_" + type; // Valeur par défaut plus descriptive
        }
    }

    // Modification dynamique du request body
    private void modifyRequestBody(JsonNode item) {
        if (item == null || !item.has("request")) {
            return;
        }

        JsonNode requestNode = item.get("request");
        // Vérifier la structure du body avant de continuer
        if (!requestNode.has("body") || !requestNode.get("body").isObject() ||
                !requestNode.get("body").has("raw") || !requestNode.get("body").get("raw").isTextual()) {
            // Pas de body raw textuel à modifier
            return;
        }

        ObjectNode bodyNode = (ObjectNode) requestNode.get("body");
        String rawBody = bodyNode.get("raw").asText();

        // Appliquer les remplacements pour les placeholders JSON <type>
        // Attention: ces remplacements sont basiques et pourraient remplacer des parties de vraies chaînes.
        // Une approche par parsing JSON serait plus robuste mais complexe.
        String updatedBody = rawBody
                .replaceAll("\"<long>\"", "3")          // Remplace "<long>" (avec guillemets) par le nombre 3
                .replaceAll("\"<integer>\"", "10")       // Remplace "<integer>" par le nombre 10
                .replaceAll("\"<string>\"", "\"TestStringInBody\"") // Remplace "<string>" par une chaîne JSON valide
                .replaceAll("\"<boolean>\"", "true")       // Remplace "<boolean>" par le booléen true (sans guillemets)
                .replaceAll("\"<double>\"", "99.99")      // Remplace "<double>" par le nombre 99.99
                .replaceAll("\"<number>\"", "123.45")     // Remplace "<number>" par le nombre 123.45
                .replaceAll("\"<datetime>\"", "\"2024-02-01T12:00:00Z\"") // Remplace "<dateTime>" par une chaîne date/heure
                .replaceAll("\"<array>\"", "[]")        // Remplace "<array>" par un tableau JSON vide
                .replaceAll("\"<object>\"", "{}");      // Remplace "<object>" par un objet JSON vide

        // Gérer le cas où les placeholders n'ont pas de guillemets (moins probable si généré correctement)
        updatedBody = updatedBody
                .replaceAll("<long>", "3")
                .replaceAll("<integer>", "10")
                .replaceAll("<string>", "\"FallbackString\"") // Mettre des guillemets pour le fallback string
                .replaceAll("<boolean>", "true")
                .replaceAll("<double>", "99.99")
                .replaceAll("<number>", "123.45")
                .replaceAll("<datetime>", "\"2024-02-01T12:00:00Z\"")
                .replaceAll("<array>", "[]")
                .replaceAll("<object>", "{}");


        if (!rawBody.equals(updatedBody)) {
            System.out.println("Info: Mise à jour du request body pour l'item: " + item.path("name").asText("sans nom"));
            bodyNode.put("raw", updatedBody);
        }
    }

    // Ajout des tests Postman
    private void addTestsToRequest(JsonNode item) {
        // Vérifications robustes des noeuds
        if (item == null || !item.has("request") || !item.get("request").isObject() ||
                !item.has("response") || !item.get("response").isArray()) {
            System.err.println("Avertissement: Structure d'item invalide pour ajouter des tests pour l'item: " + (item != null ? item.path("name").asText("sans nom") : "null"));
            return;
        }

        JsonNode requestNode = item.get("request"); // On sait que c'est un objet
        ArrayNode responseArray = (ArrayNode) item.get("response"); // On sait que c'est un tableau

        // -- Collecte des informations des réponses définies --
        List<Integer> statusCodes = new ArrayList<>();
        Map<Integer, JsonNode> responseSchemaBodies = new HashMap<>(); // Stocke les corps de réponse parsés en tant que schémas attendus

        for (JsonNode responseNode : responseArray) {
            if (!responseNode.isObject() || !responseNode.has("code") || !responseNode.get("code").isInt()) {
                System.err.println("Avertissement: Réponse invalide ou sans code entier dans l'item: " + item.path("name").asText("sans nom"));
                continue;
            }

            int statusCode = responseNode.get("code").asInt();
            statusCodes.add(statusCode);

            // Essayer de parser le corps de la réponse (qui contient le schéma simplifié)
            if (responseNode.has("body") && responseNode.get("body").isTextual()) {
                String responseBodyStr = responseNode.get("body").asText();
                if (!responseBodyStr.isEmpty() && !responseBodyStr.equals("{}") && !responseBodyStr.equals("[]")) { // Ignorer les corps vides
                    try {
                        JsonNode parsedBodySchema = objectMapper.readTree(responseBodyStr);
                        responseSchemaBodies.put(statusCode, parsedBodySchema);
                    } catch (Exception e) {
                        System.err.println("Avertissement: Impossible de parser le corps JSON de la réponse pour le statut " + statusCode + " dans l'item: " + item.path("name").asText("sans nom") + ". Erreur: " + e.getMessage());
                        // Ne pas ajouter de test de schéma si le corps n'est pas un JSON valide
                    }
                }
            }
        }

        // Si aucune réponse valide n'est trouvée, ne pas ajouter de tests
        if (statusCodes.isEmpty()) {
            System.out.println("Info: Aucune réponse valide trouvée pour ajouter des tests à l'item: " + item.path("name").asText("sans nom"));
            return;
        }

        // -- Construction du script de test --
        StringBuilder testScript = new StringBuilder();

        // Test 1: Vérifier si le code de statut reçu fait partie des codes attendus
        testScript.append("pm.test(\"Status code is one of [" + statusCodes.toString().replaceAll("[\\[\\]]", "") + "]\", function () {\n");
        testScript.append("    pm.expect(pm.response.code).to.be.oneOf([").append(statusCodes.toString().replaceAll("[\\[\\]]", "")).append("]);\n");
        testScript.append("});\n\n");

        // Tests 2+: Vérifier le schéma de la réponse pour chaque statut ayant un schéma défini
        for (Map.Entry<Integer, JsonNode> entry : responseSchemaBodies.entrySet()) {
            int expectedStatusCode = entry.getKey();
            JsonNode expectedSchemaBody = entry.getValue(); // Le corps de réponse parsé (utilisé comme schéma attendu)

            // Ajouter un test spécifique pour ce code de statut
            testScript.append("pm.test(\"Response body schema is valid for status " + expectedStatusCode + "\", function () {\n");
            testScript.append("    // Seulement exécuter ce test si le code de statut correspond\n");
            testScript.append("    if (pm.response.code === " + expectedStatusCode + ") {\n");
            testScript.append("        try {\n");
            testScript.append("            const jsonData = pm.response.json();\n");
            testScript.append("            \n");
            testScript.append("            // Vérification basique: la réponse doit être un objet ou un tableau\n");
            testScript.append("            pm.expect(jsonData).to.be.an('object'); // Postman v7+ 'object' inclut les arrays\n");
            testScript.append("            \n");

            // Déterminer si le schéma attendu est un objet ou un tableau d'objets
            boolean expectArray = expectedSchemaBody.isArray();

            if (expectArray) {
                testScript.append("            // S'attendre à un tableau et vérifier le schéma du premier élément (s'il existe)\n");
                testScript.append("            pm.expect(jsonData).to.be.an('array');\n");
                testScript.append("            if (jsonData.length > 0) {\n");
                testScript.append("                let itemSchema = jsonData[0];\n");
                // Extraire les champs du premier élément du *schéma attendu*
                if (expectedSchemaBody.size() > 0 && expectedSchemaBody.get(0).isObject()) {
                    JsonNode firstElementSchema = expectedSchemaBody.get(0);
                    List<String> fields = getSchemaFields(firstElementSchema);
                    for (String field : fields) {
                        testScript.append("                pm.expect(itemSchema).to.have.property('" + escapeJsString(field) + "');\n");
                        // Optionnel: Ajouter des vérifications de type si possible/nécessaire
                        // testScript.append("                pm.expect(itemSchema." + field + ").to.be.a('string'); // Exemple\n");
                    }
                } else {
                    testScript.append("                // Le schéma attendu est un tableau mais le premier élément n'est pas un objet ou est vide, test basique.\n");
                }
                testScript.append("            }\n");
            } else { // expectObject
                testScript.append("            // S'attendre à un objet et vérifier ses propriétés\n");
                List<String> fields = getSchemaFields(expectedSchemaBody);
                for (String field : fields) {
                    testScript.append("            pm.expect(jsonData).to.have.property('" + escapeJsString(field) + "');\n");
                    // Optionnel: Ajouter des vérifications de type
                    // String expectedType = getJsTypeFromSchema(expectedSchemaBody.get(field)); // Fonction à créer
                    // testScript.append("            pm.expect(jsonData." + field + ").to.be.a('" + expectedType + "');\n");
                }
                // Vérifier s'il n'y a aucune propriété attendue (objet vide {})
                if (fields.isEmpty()) {
                    testScript.append("            // S'attendre à un objet vide\n");
                    testScript.append("            pm.expect(Object.keys(jsonData).length).to.equal(0);\n");
                }
            }

            testScript.append("        } catch (e) {\n");
            testScript.append("            // Échouer le test si le JSON ne peut pas être parsé\n");
            testScript.append("            pm.expect.fail('Response body for status " + expectedStatusCode + " is not valid JSON: ' + e.message);\n");
            testScript.append("        }\n");
            testScript.append("    } else {\n");
            testScript.append("        // Si le code de statut ne correspond pas, passer ce test\n");
            testScript.append("        // console.log('Skipping schema test for status " + expectedStatusCode + " as response code is ' + pm.response.code);\n");
            testScript.append("    }\n");
            testScript.append("});\n\n");
        }

        // -- Assembler et ajouter l'événement 'test' à l'item --
        // Créer la structure d'événement Postman
        ObjectNode eventNode = objectMapper.createObjectNode();
        eventNode.put("listen", "test");

        ObjectNode scriptNode = objectMapper.createObjectNode();
        scriptNode.put("type", "text/javascript");
        scriptNode.put("exec", testScript.toString()); // Utiliser set sans conversion en ArrayNode ici

        eventNode.set("script", scriptNode); // Attribuer l'objet script

        // Créer le tableau d'événements et y ajouter notre événement de test
        ArrayNode eventArray = objectMapper.createArrayNode();
        eventArray.add(eventNode);

        // Ajouter/Remplacer le champ 'event' dans l'item Postman
        ((ObjectNode) item).set("event", eventArray);
        System.out.println("Info: Ajout/Mise à jour des tests Postman pour l'item: " + item.path("name").asText("sans nom"));
    }


    // Récupérer les champs du schéma JSON (les clés de l'objet)
    private List<String> getSchemaFields(JsonNode schemaNode) {
        List<String> fields = new ArrayList<>();
        if (schemaNode != null && schemaNode.isObject()) {
            schemaNode.fieldNames().forEachRemaining(fields::add);
        } else if (schemaNode != null && schemaNode.isArray() && schemaNode.size() > 0 && schemaNode.get(0).isObject()){
            // Si c'est un tableau, prendre les champs du premier élément comme référence
            schemaNode.get(0).fieldNames().forEachRemaining(fields::add);
        }
        // Retourner une liste vide si le schéma n'est pas un objet ou un tableau d'objets non vide
        return fields;
    }

    // Fonction utilitaire pour échapper les caractères spéciaux dans les noms de champs pour JS
    private String escapeJsString(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }


    // Sauvegarde de la collection dans la partie ressources
    private void saveCollectionToResources(JsonNode collectionNode) throws Exception {
        // S'assurer que le répertoire parent existe
        File targetFile = new File(POSTMAN_COLLECTION_PATH);
        File parentDir = targetFile.getParentFile();
        if (!parentDir.exists()) {
            System.out.println("Info: Création du répertoire parent: " + parentDir.getAbsolutePath());
            if (!parentDir.mkdirs()) {
                throw new IOException("Impossible de créer les répertoires parents pour: " + POSTMAN_COLLECTION_PATH);
            }
        }

        // Écrire le fichier
        try (FileWriter writer = new FileWriter(targetFile)) {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(writer, collectionNode);
            System.out.println("Info: Collection Postman enregistrée dans: " + targetFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Erreur lors de l'écriture du fichier de collection Postman: " + e.getMessage());
            throw e; // Renvoyer l'exception pour qu'elle soit gérée plus haut
        }
    }

    // Méthodes getDefaultValueForType et autres helpers non modifiés...
    // Assurez-vous qu'elles existent si elles sont appelées ailleurs.

}*/
/*import com.fasterxml.jackson.databind.ObjectMapper;
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


@Service
public class PostmanProcessingService {

    // --- Dependencies ---
    private final ApiMonitoringRepository resultRepository;
    private final MeterRegistry meterRegistry;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // --- Configuration ---
    // REMOVED: Configurable paths via @Value are removed as requested.

    // --- Metric State Storage ---
    private final Map<String, TestMetricData> latestTestMetrics = new ConcurrentHashMap<>();

    private record TestMetricData(
            AtomicLong responseTimeMs,
            AtomicInteger statusCode,
            AtomicInteger success
    ) {
        TestMetricData() {
            this(new AtomicLong(0), new AtomicInteger(0), new AtomicInteger(0));
        }
    }

    // --- Constructor Injection ---
    public PostmanProcessingService(ApiMonitoringRepository resultRepository, MeterRegistry meterRegistry) {
        this.resultRepository = resultRepository;
        this.meterRegistry = meterRegistry;
        System.out.println("PostmanProcessingService créé avec le repository : " + resultRepository + " et MeterRegistry: " + meterRegistry);
    }

    // --- Scheduled Newman Execution (Uses Hardcoded Default Collection) ---
    // You can still keep the cron expression configurable if desired, or hardcode it too.
    @Scheduled(cron = "${newman.schedule.cron:0 * * * * *}") // Example: Still configurable cron
    // OR Hardcoded: @Scheduled(cron = "0 * * * * *")
    public void runScheduledNewmanTests() {
        System.out.println("Début de l'exécution planifiée des tests Newman.");
        String defaultCollectionResourceName = "postman_collection.json"; // Hardcoded resource name

        try {
            ClassLoader classLoader = getClass().getClassLoader();
            // Load the default collection file directly from resources
            File file = new File(classLoader.getResource(defaultCollectionResourceName).toURI());
            System.out.println("Utilisation de la collection planifiée : " + file.getAbsolutePath());
            // Delegate to the common execution logic
            executeNewmanAndProcessResults(file.getAbsolutePath());
        } catch (URISyntaxException | NullPointerException e) {
            // Error finding or loading the default collection
            System.err.println("Erreur critique: Impossible de trouver ou charger le fichier de collection par défaut '" + defaultCollectionResourceName + "' dans les ressources: " + e.getMessage());
            e.printStackTrace();
            meterRegistry.counter("newman_run_errors_total", Tags.of("reason", "resource_not_found")).increment();
        } catch (Exception e) {
            // Catch other potential exceptions during setup
            System.err.println("Erreur inattendue lors de la préparation de l'exécution planifiée: " + e.getMessage());
            e.printStackTrace();
            meterRegistry.counter("newman_run_errors_total", Tags.of("reason", "setup_error")).increment();
        }
        System.out.println("Fin de l'exécution planifiée des tests Newman.");
    }

    // --- Manual Processing from JSON String ---
    // This method remains largely the same, as it uses a temporary file.
    // It will call executeNewmanAndProcessResults which now uses the hardcoded Newman path.
    public String processPostmanCollection(String postmanCollectionJson) {
        System.out.println("Début du traitement manuel de la collection Postman fournie.");
        Path tempFilePath = null;
        try {
            tempFilePath = Files.createTempFile("postman_manual_", ".json");
            System.out.println("Fichier temporaire créé : " + tempFilePath.toString());
            Files.writeString(tempFilePath, postmanCollectionJson, StandardOpenOption.WRITE);

            // Calls the common logic which now uses the hardcoded Newman path
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
                try {
                    Files.deleteIfExists(tempFilePath);
                    System.out.println("Fichier temporaire supprimé : " + tempFilePath.toString());
                } catch (IOException e) {
                    System.err.println("Attention : Impossible de supprimer le fichier temporaire : " + tempFilePath + " - " + e.getMessage());
                }
            }
            System.out.println("Fin du traitement manuel de la collection Postman fournie.");
        }
    }

    // --- Common Newman Execution and Result Processing Logic ---
    private boolean executeNewmanAndProcessResults(String collectionPath) throws IOException, InterruptedException, Exception {
        System.out.println("Exécution de Newman pour la collection : " + collectionPath);
        List<ParsedResult> currentRunResults = new ArrayList<>();
        boolean success = false;

        // ***** HARDCODED NEWMAN PATH AS REQUESTED *****
        String hardcodedNewmanCommandPath = "C:\\Users\\LENOVO\\AppData\\Roaming\\npm\\newman.cmd";
        System.out.println("Utilisation du chemin Newman hardcodé : " + hardcodedNewmanCommandPath);

        // REMOVED: Check for configured newmanCommandPath

        ProcessBuilder processBuilder = new ProcessBuilder(
                hardcodedNewmanCommandPath, // Use the hardcoded path here
                "run", collectionPath,
                "--reporters", "cli"
        );

        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            parseNewmanOutput(reader, currentRunResults);
        }

        int exitCode = process.waitFor();
        System.out.println(exitCode == 0 ? "Processus Newman terminé avec succès (code 0)." : "Processus Newman terminé avec des erreurs (code " + exitCode + ").");
        success = (exitCode == 0);

        updatePersistenceAndMetrics(currentRunResults);

        if (!success || currentRunResults.isEmpty()) {
            meterRegistry.counter("newman_run_errors_total", Tags.of("reason", success ? "parsing_failure" : "newman_exit_code")).increment();
        }

        return success;
    }
    // --- Newman Output Parsing Logic ---
    private void parseNewmanOutput(BufferedReader reader, List<ParsedResult> results) throws Exception {
        String line;
        ParsedResult currentResult = null;
        StringBuilder errorMessageBuilder = new StringBuilder();
        boolean inErrorBlock = false;
        boolean hasNumDotError = false;
        boolean expectingRequestDetails = false; // <-- NOUVELLE VARIABLE D'ETAT

        // Regex patterns (inchangés)
        Pattern requestLinePattern = Pattern.compile("^\\s*(GET|POST|PUT|DELETE|PATCH|HEAD|OPTIONS)\\s+(https?://[^\\s]+)\\s+\\[(.*?)\\]");
        Pattern statusPattern = Pattern.compile("(\\d{3})\\s+[^,]+,\\s+[^,]+,\\s+(\\d+)ms");

        while ((line = reader.readLine()) != null) {
            // System.out.println("Ligne lue : " + line); // Garder pour le débogage si nécessaire

            // *** DEBUT DES MODIFICATIONS ***

            if (currentResult != null && expectingRequestDetails) {
                // On s'attendait à ce que cette ligne contienne les détails GET/POST...
                expectingRequestDetails = false; // On traite cette attente, qu'elle réussisse ou non

                Matcher requestMatcher = requestLinePattern.matcher(line);
                if (requestMatcher.find()) {
                    currentResult.httpMethod = requestMatcher.group(1);
                    String fullUrl = requestMatcher.group(2);
                    String statusAndTimePart = requestMatcher.group(3);

                    try {
                        URI uri = new URI(fullUrl);
                        currentResult.path = uri.getPath();
                        if (uri.getQuery() != null) {
                            currentResult.path += "?" + uri.getQuery();
                        }
                        // System.out.println("  Méthode: " + currentResult.httpMethod + ", Path extrait: " + currentResult.path);

                        if (line.contains("[errored]")) {
                            currentResult.statusCode = 0;
                            currentResult.responseTime = 0;
                            errorMessageBuilder.append("Request Errored: ").append(line.trim());
                            inErrorBlock = true; // Marquer comme erreur
                        } else {
                            Matcher statusMatcher = statusPattern.matcher(statusAndTimePart);
                            if (statusMatcher.find()) {
                                currentResult.statusCode = Integer.parseInt(statusMatcher.group(1));
                                currentResult.responseTime = Long.parseLong(statusMatcher.group(2));
                                // System.out.println("  Status: " + currentResult.statusCode + ", Temps: " + currentResult.responseTime + "ms");
                            } else {
                                System.err.println("  Impossible d'extraire status/temps de : " + statusAndTimePart + " pour " + currentResult.apiName + " sur la ligne: " + line);
                                currentResult.statusCode = 0;
                                currentResult.responseTime = 0;
                                // Ajouter un message d'erreur si le parsing status/temps échoue explicitement
                                errorMessageBuilder.append("Erreur parsing Status/Temps sur ligne attendue: ").append(line.trim());
                                inErrorBlock = true;
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("  Erreur lors de l'extraction du path/status depuis : " + line + " pour " + currentResult.apiName + " - " + e.getMessage());
                        currentResult.path = "Unknown Path"; // Marquer comme inconnu
                        errorMessageBuilder.append("Erreur parsing URL/Status: ").append(e.getMessage());
                        inErrorBlock = true;
                    }
                } else {
                    // La ligne attendue après '└' n'a pas le format GET/POST...
                    System.err.println("  Format inattendu pour la ligne de détails de requête attendue: " + line + " pour " + currentResult.apiName);
                    if (currentResult.path == null) { // Si on n'a toujours pas de path valide
                        currentResult.path = "Unknown Path"; // Marquer comme inconnu
                    }
                    errorMessageBuilder.append("Format de ligne de requête (après └) inattendu: ").append(line.trim());
                    inErrorBlock = true;
                }
                // On a traité la ligne attendue, on passe à la vérification suivante (assertions, etc.) pour *cette* ligne

            } else if (line.trim().startsWith("□ ") || line.trim().startsWith("→ ")) {
                // Début d'un nouveau bloc de requête
                if (currentResult != null) {
                    finalizeAndAddResult(currentResult, hasNumDotError, errorMessageBuilder, results);
                }
                currentResult = new ParsedResult();
                currentResult.apiName = line.substring(line.indexOf(" ")).trim();
                // System.out.println("Début de la requête : " + currentResult.apiName);
                errorMessageBuilder.setLength(0);
                inErrorBlock = false;
                hasNumDotError = false;
                expectingRequestDetails = false; // Réinitialiser au début d'un nouveau test

            } else if (currentResult != null && line.trim().startsWith("└")) {
                // Ligne '└' trouvée, la *prochaine* ligne devrait contenir les détails GET/POST
                expectingRequestDetails = true;
                // On ne fait rien d'autre sur cette ligne '└' elle-même pour le parsing principal

            } else if (line.trim().startsWith("√") && currentResult != null) {
                // Assertion passée - OK
                // System.out.println("  Assertion(s) passed.");

            } else if (currentResult != null) {
                // Vérification des erreurs d'assertion numérotées
                Pattern errorPattern = Pattern.compile("^\\s*\\d+\\.\\s+(.*)");
                Matcher errorMatcher = errorPattern.matcher(line);
                if (errorMatcher.find()) {
                    if (!hasNumDotError) {
                        errorMessageBuilder.append("Assertion Errors:\n");
                    }
                    errorMessageBuilder.append("  - ").append(errorMatcher.group(1).trim()).append("\n");
                    inErrorBlock = true;
                    hasNumDotError = true;
                } else if (inErrorBlock && line.trim().length() > 0 && !line.trim().matches("^[┌│├└].*")) {
                    // Capturer des lignes supplémentaires potentiellement liées à une erreur
                    errorMessageBuilder.append("    ").append(line.trim()).append("\n");
                }
                // Gérer le cas où une ligne inattendue apparaît après la ligne '└' mais avant la ligne GET/POST
                // Cela est implicitement couvert par la logique 'expectingRequestDetails' au début de la boucle

            }
            // *** FIN DES MODIFICATIONS ***

        } // Fin while loop

        // Traiter le tout dernier résultat après la fin de la boucle
        if (currentResult != null) {
            finalizeAndAddResult(currentResult, hasNumDotError, errorMessageBuilder, results);
        }
    }


    // Helper to finalize parsing of a single result and add it to the list
    // (No changes needed here)
    private void finalizeAndAddResult(ParsedResult result, boolean hasNumDotError, StringBuilder errorMsgBuilder, List<ParsedResult> resultsList) {
        if (result.path == null || result.httpMethod == null) {
            System.err.println("Skipping result for API '" + result.apiName + "' due to missing path or method.");
            if (errorMsgBuilder.length() > 0) {
                System.err.println("  Captured error context: " + errorMsgBuilder.toString());
            }
            return;
        }

        // Determine success based on status and errors found during parsing
        if (hasNumDotError || (result.statusCode >= 400 && result.statusCode != 0)) {
            result.success = false;
            if (errorMsgBuilder.length() == 0 && result.statusCode != 0) {
                errorMsgBuilder.append("Request failed with status code: ").append(result.statusCode);
            }
        } else if (result.statusCode >= 200 && result.statusCode < 300 && result.statusCode=404) {
            result.success = true;
        } else if (result.path.equals("Unknown Path") || (result.statusCode == 0 && !hasNumDotError && !errorMsgBuilder.toString().contains("Request Errored"))) {
            // Handle cases like parsing failure or truly errored request without explicit [errored] tag
            result.success = false;
            if(errorMsgBuilder.length() == 0) {
                errorMsgBuilder.append("Request failed or could not be fully parsed (Status: ").append(result.statusCode).append(")");
            }
        } else if (errorMsgBuilder.toString().contains("Request Errored")) {
            // Explicitly marked as errored during parsing
            result.success = false;
        }
        else { // Default for unexpected codes (e.g., 3xx without error, or other unhandled cases)
            result.success = false; // Treat as failure unless explicitly success
            if(errorMsgBuilder.length() == 0 && result.statusCode != 0) {
                errorMsgBuilder.append("Unexpected status code: ").append(result.statusCode);
            } else if (errorMsgBuilder.length() == 0) {
                errorMsgBuilder.append("Unknown failure reason");
            }
        }

        result.errorMessage = errorMsgBuilder.toString().trim();

        System.out.println("Résultat finalisé: Path=" + result.path + ", Method=" + result.httpMethod + ", Success=" + result.success + ", Status=" + result.statusCode + ", Time=" + result.responseTime + "ms" + (result.errorMessage.isEmpty() ? "" : ", Error=Present"));
        resultsList.add(result);
    }

    // --- Update DB (Optional) and Metrics ---
    // (No changes needed here)
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
                .description("Unix timestamp of the last completed Newman run processing (parsing, metrics, db update)")
                .register(meterRegistry);
    }


    // --- Metrics Update Logic ---
    // (No changes needed here)
    private void updateMetricsForResult(ParsedResult result) {
        String metricKey = result.httpMethod + ":" + result.path;
        Tags tags = Tags.of(
                Tag.of("path", result.path),
                Tag.of("method", result.httpMethod),
                Tag.of("api_name", result.apiName != null ? result.apiName : "unknown")
        );

        TestMetricData metricData = latestTestMetrics.computeIfAbsent(metricKey, k -> {
            TestMetricData newData = new TestMetricData();
            Gauge.builder("newman_test_response_time_ms", newData.responseTimeMs::get)
                    .tags(tags)
                    .description("Response time in milliseconds for the Newman test")
                    .register(meterRegistry);
            Gauge.builder("newman_test_status_code", newData.statusCode::get)
                    .tags(tags)
                    .description("HTTP status code for the Newman test")
                    .register(meterRegistry);
            Gauge.builder("newman_test_success", newData.success::get)
                    .tags(tags)
                    .description("Indicates if the Newman test passed (1) or failed (0)")
                    .register(meterRegistry);
            System.out.println("Registered new metrics for: " + k);
            return newData;
        });

        metricData.responseTimeMs().set(result.responseTime);
        metricData.statusCode().set(result.statusCode);
        metricData.success().set(result.success ? 1 : 0);
    }

    // --- Database Persistence Logic ---
    // (No changes needed here)
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

    // --- Helper Class for Parsed Results ---
    // (No changes needed)
    private static class ParsedResult {
        String apiName;
        String path;
        String httpMethod;
        boolean success = false;
        int statusCode = 0;
        long responseTime = 0;
        String errorMessage = "";
    }

    // --- Entity and Repository Placeholders ---
    // Ensure ApiMonitoring.java and ApiMonitoringRepository.java exist and are correct.
}*/