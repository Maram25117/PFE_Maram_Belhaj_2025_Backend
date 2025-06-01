package com.example.api_tierces.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.api_tierces.repository.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.io.File;
import java.io.FileWriter;

import java.util.stream.Collectors;

// ajout des scripts des tests
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

    private static final String POSTMAN_COLLECTION_PATH = "src/main/resources/postman/postman_collection.json";


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


    private void replacePathParameters(JsonNode item) {
        if (item == null || !item.has("request")) {
            return;
        }

        JsonNode requestNode = item.get("request");
        if (!requestNode.has("url") || !requestNode.has("method") || !requestNode.path("url").isObject()) {
            return;
        }

        ObjectNode urlNode = (ObjectNode) requestNode.get("url");
        String method = requestNode.get("method").asText();


        Map<String, String> finalPathVariables = new HashMap<>();
        List<Map.Entry<String, String>> finalQueryParameters = new ArrayList<>();


        if (urlNode.has("variable") && urlNode.get("variable").isArray()) {
            for (JsonNode variable : urlNode.get("variable")) {
                if (variable.isObject() && variable.has("key") && variable.has("value")) {
                    ObjectNode varObj = (ObjectNode) variable;
                    String key = varObj.path("key").asText();
                    String currentValue = varObj.path("value").asText();
                    String finalValue = currentValue;


                    if ("DELETE".equalsIgnoreCase(method) &&
                            (key.equalsIgnoreCase("id") || currentValue.matches("\\d+") || currentValue.contains("<long>") || currentValue.contains("<int>")))
                    {
                        finalValue = "4";
                    }

                    else if (currentValue.startsWith("<") && currentValue.endsWith(">")) {
                        finalValue = getRealValueForType(currentValue);
                    }

                    varObj.put("value", finalValue);
                    finalPathVariables.put(key, finalValue);
                }
            }
        }


        if (urlNode.has("query") && urlNode.get("query").isArray()) {
            for (JsonNode queryParam : urlNode.get("query")) {
                if (queryParam.isObject() && queryParam.has("key") && queryParam.has("value")) {
                    ObjectNode queryObj = (ObjectNode) queryParam;
                    String key = queryObj.path("key").asText();
                    String currentValue = queryObj.path("value").asText();
                    String finalValue = currentValue; // Valeur par défaut


                    if ((currentValue.startsWith("<") && currentValue.endsWith(">")) ||
                            "Aucun exemple disponible".equals(currentValue))
                    {
                        finalValue = getRealValueForType(currentValue);
                    }


                    queryObj.put("value", finalValue);
                    finalQueryParameters.add(new AbstractMap.SimpleEntry<>(key, finalValue));
                }
            }
        }


        StringBuilder reconstructedUrl = new StringBuilder();
        if (urlNode.has("host") && urlNode.get("host").isArray() && !urlNode.get("host").isEmpty()) {
            reconstructedUrl.append(urlNode.get("host").get(0).asText());
        } else {
            reconstructedUrl.append("{{baseUrl}}");
        }


        if (urlNode.has("path") && urlNode.get("path").isArray()) {
            ArrayNode pathSegments = (ArrayNode) urlNode.get("path");
            for (JsonNode segmentNode : pathSegments) {
                String segment = segmentNode.asText();
                if (segment.startsWith(":")) {
                    String varKey = segment.substring(1);
                    reconstructedUrl.append("/").append(finalPathVariables.getOrDefault(varKey, segment));
                } else if (!segment.isEmpty()){
                    reconstructedUrl.append("/").append(segment);
                }
            }

            if (reconstructedUrl.toString().endsWith("{{baseUrl}}/") && pathSegments.isEmpty()){
            } else if (reconstructedUrl.toString().equals("{{baseUrl}}") && !pathSegments.isEmpty()){
                reconstructedUrl.delete(0, reconstructedUrl.length());
                reconstructedUrl.append("{{baseUrl}}");
                String firstSegment = pathSegments.get(0).asText();
                reconstructedUrl.append("/").append(finalPathVariables.getOrDefault(firstSegment.startsWith(":") ? firstSegment.substring(1) : firstSegment, firstSegment));
                for(int i=1; i< pathSegments.size(); i++){
                    String seg = pathSegments.get(i).asText();
                    reconstructedUrl.append("/").append(finalPathVariables.getOrDefault(seg.startsWith(":") ? seg.substring(1) : seg, seg));
                }
            } else if (reconstructedUrl.toString().endsWith("{{baseUrl}}")){
            }

        }


        if (!finalQueryParameters.isEmpty()) {
            reconstructedUrl.append("?");
            String queryPart = finalQueryParameters.stream()
                    .map(entry -> {
                        try {
                            String encodedKey = URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8);
                            String encodedValue = URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8);
                            return encodedKey + "=" + encodedValue;
                        } catch (Exception e) {
                            System.err.println("WARN: Erreur d'encodage URL pour query param: " + entry.getKey() + "=" + entry.getValue() + " - " + e.getMessage());
                            return entry.getKey() + "=" + entry.getValue();
                        }
                    })
                    .collect(Collectors.joining("&"));
            reconstructedUrl.append(queryPart);
        }


        urlNode.put("raw", reconstructedUrl.toString());
    }
    private String getRealValueForType(String typeHint) {
        if (typeHint == null) {
            return "default_value";
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
                if (typeHint.trim().startsWith("<") && typeHint.trim().endsWith(">")) return "unknown_placeholder_value";
                else return typeHint;
        }
    }


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


    private List<String> getSchemaFields(JsonNode schema) {
        List<String> fields = new ArrayList<>();
        schema.fieldNames().forEachRemaining(fields::add);
        return fields;
    }


    private void saveCollectionToResources(JsonNode collectionNode) throws Exception {
        File targetFile = new File(POSTMAN_COLLECTION_PATH);
        try (FileWriter writer = new FileWriter(targetFile)) {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(writer, collectionNode);
        }
    }
}


