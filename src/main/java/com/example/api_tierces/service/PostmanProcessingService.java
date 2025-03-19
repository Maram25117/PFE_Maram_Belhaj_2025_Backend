/*package com.example.api_tierces.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.api_tierces.model.*;
import com.example.api_tierces.repository.*;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;

@Service
public class PostmanProcessingService {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApiResponseRepository apiResponseRepository;

    @Autowired
    private ApiRepository apiRepository;

    private static final String POSTMAN_COLLECTION_PATH = "src/main/resources/postman_collection.json";

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
        if (!requestNode.has("url")) {
            return;
        }

        JsonNode urlNode = requestNode.get("url");
        if (!urlNode.has("variable")) {
            return;
        }

        for (JsonNode variable : urlNode.get("variable")) {
            if (variable.has("key") && variable.has("value")) {
                String valueType = variable.get("value").asText();
                String replacementValue = getDefaultValueForType(valueType);
                ((ObjectNode) variable).put("value", replacementValue);
            }
        }
    }

    // 📌 **Remplacement dynamique dans le Request Body**
    private void modifyRequestBody(JsonNode item) {
        if (item == null || !item.has("request")) {
            return;
        }

        JsonNode requestNode = item.get("request");
        if (!requestNode.has("body") || !requestNode.get("body").has("raw")) {
            return;
        }

        String body = requestNode.get("body").get("raw").asText();

        body = body.replaceAll("\"<long>\"", "1")
                .replaceAll("\"<integer>\"", "10")
                .replaceAll("\"<string>\"", "\"TestString\"")
                .replaceAll("\"<boolean>\"", "true")
                .replaceAll("\"<double>\"", "99.99")
                .replaceAll("\"<number>\"", "100")
                .replaceAll("\"<dateTime>\"", "\"2023-10-01T00:00:00Z\"");

        ((ObjectNode) requestNode.get("body")).put("raw", body);
    }

    // 📌 **Déterminer la valeur par défaut d'un type**
    private String getDefaultValueForType(String type) {
        switch (type.toLowerCase()) {
            case "<long>":
            case "long":
            case "id":
                return "1";
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

    // 📌 **Ajout des scripts de test pour chaque requête**
    private void addTestsToRequest(JsonNode item) {
        if (item == null || !item.has("request")) {
            return;
        }

        JsonNode requestNode = item.get("request");
        if (!requestNode.has("url") || !requestNode.has("method")) {
            return;
        }

        String method = requestNode.get("method").asText();
        String rawPath = requestNode.get("url").get("raw").asText(); // ✅ On garde {{baseUrl}}

        if (!item.has("response") || !item.get("response").isArray()) {
            return;
        }

        // 📌 **Construction du script de test**
        StringBuilder testScript = new StringBuilder();
        testScript.append("pm.test(\"Vérification des réponses possibles\", function () {\n");

        for (JsonNode responseNode : item.get("response")) {
            if (!responseNode.has("code") || !responseNode.has("body")) {
                continue;
            }

            int statusCode = responseNode.get("code").asInt();
            String responseBody = responseNode.get("body").asText();

            // 🔎 Vérification du statut HTTP
            testScript.append("    pm.test(\"Statut ").append(statusCode).append("\", function () {\n")
                    .append("        pm.response.to.have.status(").append(statusCode).append(");\n")
                    .append("    });\n");

            // 🔎 Vérification des champs JSON attendus
            if (!responseBody.isEmpty()) {
                try {
                    JsonNode responseJson = objectMapper.readTree(responseBody);
                    testScript.append("    pm.test(\"Vérifier la structure de la réponse\", function () {\n");

                    Iterator<String> fieldNames = responseJson.fieldNames();
                    while (fieldNames.hasNext()) {
                        String field = fieldNames.next();
                        testScript.append("        pm.expect(pm.response.json()).to.have.property(\"")
                                .append(field).append("\");\n");
                    }

                    testScript.append("    });\n");
                } catch (Exception e) {
                    System.out.println("❌ Impossible de parser la réponse JSON pour " + rawPath);
                }
            }
        }

        testScript.append("});\n");

        // 📌 **Ajout du script dans la requête Postman**
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

    // 📌 **Sauvegarde de la collection**
    private void saveCollectionToResources(JsonNode collectionNode) throws Exception {
        File targetFile = new File(POSTMAN_COLLECTION_PATH);
        try (FileWriter writer = new FileWriter(targetFile)) {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(writer, collectionNode);
        }
    }
}*/
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

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Service
public class PostmanProcessingService {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApiResponseRepository apiResponseRepository;

    @Autowired
    private ApiRepository apiRepository;

    private static final String POSTMAN_COLLECTION_PATH = "src/main/resources/postman_collection.json";

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
    }*/
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

        String path = requestNode.get("url").get("raw").asText();
        String method = requestNode.get("method").asText();

        Api api = apiRepository.findByPathAndMethod(path, method);
        if (api == null) {
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

        // 🔎 Vérification du schéma attendu pour chaque réponse
        for (Map.Entry<Integer, JsonNode> entry : responseSchemas.entrySet()) {
            int statusCode = entry.getKey();
            JsonNode schema = entry.getValue();

            com.example.api_tierces.model.ApiResponse apiResponse = apiResponseRepository.findByApiAndStatus(api, String.valueOf(statusCode));
            if (apiResponse != null) {
                String schemaNameRef = apiResponse.getName_schema();
                Schema schemaEntity = apiResponse.getSchema();

                if (schemaEntity != null) {
                    String schemaContent = schemaEntity.getSchemas();
                    if (schemaContent != null && !schemaContent.isEmpty()) {
                        try {
                            JsonNode expectedSchema = objectMapper.readTree(schemaContent);
                            testScript.append("pm.test(\"Vérification du schéma de réponse pour le statut ").append(statusCode).append("\", function () {\n")
                                    .append("    const jsonResponse = pm.response.json();\n")
                                    .append("    if (pm.response.code === ").append(statusCode).append(") {\n")
                                    .append("        if (Array.isArray(jsonResponse)) {\n")
                                    .append("            pm.expect(jsonResponse).to.be.an('array').and.not.be.empty;\n")
                                    .append("            jsonResponse.forEach(function(item) {\n");

                            for (String field : getSchemaFields(expectedSchema)) {
                                testScript.append("                pm.expect(item).to.have.property(\"").append(field).append("\");\n");
                            }

                            testScript.append("            });\n")
                                    .append("        } else {\n");

                            for (String field : getSchemaFields(expectedSchema)) {
                                testScript.append("            pm.expect(jsonResponse).to.have.property(\"").append(field).append("\");\n");
                            }

                            testScript.append("        }\n    }\n")
                                    .append("});\n");
                        } catch (Exception e) {
                            System.out.println("❌ Impossible de parser le schéma JSON de la base de données");
                        }
                    } else {
                        testScript.append("pm.test(\"Vérification du schéma de réponse pour le statut ").append(statusCode).append("\", function () {\n")
                                .append("    if (pm.response.code === ").append(statusCode).append(") {\n")
                                .append("        pm.expect(pm.response.json()).to.be.empty;\n")
                                .append("    }\n")
                                .append("});\n");
                    }
                } else {
                    testScript.append("pm.test(\"Vérification du schéma de réponse pour le statut ").append(statusCode).append("\", function () {\n")
                            .append("    if (pm.response.code === ").append(statusCode).append(") {\n")
                            .append("        pm.expect(pm.response.json()).to.be.empty;\n")
                            .append("    }\n")
                            .append("});\n");
                }
            }
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
}
