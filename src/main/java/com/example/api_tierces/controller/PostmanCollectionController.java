package com.example.api_tierces.controller;

import com.example.api_tierces.model.*;
import com.example.api_tierces.repository.ApiRepository;
import com.example.api_tierces.repository.ApiParametersRepository;
import com.example.api_tierces.repository.ApiResponseRepository;
import com.example.api_tierces.repository.SchemaRepository;
import com.example.api_tierces.service.PostmanProcessingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Iterator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*@RestController
@RequestMapping("/api/postman")
public class PostmanCollectionController {

    @Autowired
    private ApiRepository apiRepository;

    @Autowired
    private ApiParametersRepository apiParametersRepository;

    @Autowired
    private ApiResponseRepository apiResponseRepository;

    @Autowired
    private SchemaRepository schemaRepository;

    @GetMapping("/collection")
    public Map<String, Object> generatePostmanCollection() {
        List<Api> apis = apiRepository.findAll();
        List<Map<String, Object>> items = new ArrayList<>();

        for (Api api : apis) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", api.getPath());
            item.put("request", createRequest(api));
            item.put("response", createResponses(api));
            items.add(item);
        }

        Map<String, Object> collection = new HashMap<>();
        collection.put("info", createInfo());
        collection.put("item", items);

        // Sauvegarder la collection dans un fichier JSON
        saveCollectionToFile(collection);

        return collection;
    }

    private Map<String, Object> createInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "Generated API Collection");
        info.put("schema", "https://schema.getpostman.com/json/collection/v2.1.0/collection.json");
        return info;
    }

    private Map<String, Object> createRequest(Api api) {
        Map<String, Object> request = new HashMap<>();
        request.put("method", api.getMethod());
        request.put("header", new ArrayList<>());
        request.put("body", createRequestBody(api));
        request.put("url", createUrl(api));
        return request;
    }

    private Map<String, Object> createRequestBody(Api api) {
        Map<String, Object> body = new HashMap<>();
        body.put("mode", "raw");
        body.put("raw", api.getRequest_body());
        return body;
    }

    private Map<String, Object> createUrl(Api api) {
        Map<String, Object> url = new HashMap<>();
        url.put("raw", "{{base_url}}" + api.getPath());
        url.put("host", List.of("{{base_url}}"));
        url.put("path", List.of(api.getPath().split("/")));
        return url;
    }

    private List<Map<String, Object>> createResponses(Api api) {
        List<ApiResponse> apiResponses = apiResponseRepository.findByApi(api);
        List<Map<String, Object>> responses = new ArrayList<>();

        for (ApiResponse apiResponse : apiResponses) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", apiResponse.getStatus());
            response.put("description", apiResponse.getDescription());

            // Add schema body only if the schema is not null
            Schema schema = apiResponse.getSchema();
            if (schema != null) {
                response.put("body", schema.getSchemas());
            } else {
                response.put("body", "No schema defined");
            }

            responses.add(response);
        }

        return responses;
    }

    private void saveCollectionToFile(Map<String, Object> collection) {
        try {
            // Convertir la collection en JSON
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(collection);

            // Définir le chemin du fichier
            Path path = Paths.get("test.json");

            // Écrire le fichier
            Files.write(path, json.getBytes());

            System.out.println("Collection saved to file: " + path.toAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to save collection to file: " + e.getMessage());
        }
    }
}*/
/*@RestController
@RequestMapping("/api/postman")
public class PostmanCollectionController {

    @Autowired
    private ApiRepository apiRepository;

    @Autowired
    private ApiParametersRepository apiParametersRepository;

    @Autowired
    private ApiResponseRepository apiResponseRepository;

    @Autowired
    private SchemaRepository schemaRepository;

    @GetMapping("/collection")
    public Map<String, Object> generatePostmanCollection() {
        List<Api> apis = apiRepository.findAll();
        List<Map<String, Object>> items = new ArrayList<>();

        for (Api api : apis) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", api.getPath());
            item.put("request", createRequest(api));
            item.put("response", createResponses(api));
            items.add(item);
        }

        Map<String, Object> collection = new HashMap<>();
        collection.put("info", createInfo());
        collection.put("item", items);

        // Sauvegarder la collection dans un fichier JSON
        saveCollectionToFile(collection);

        return collection;
    }

    private Map<String, Object> createInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "Generated API Collection");
        info.put("schema", "https://schema.getpostman.com/json/collection/v2.1.0/collection.json");
        return info;
    }

    private Map<String, Object> createRequest(Api api) {
        Map<String, Object> request = new HashMap<>();
        request.put("method", api.getMethod());
        request.put("header", new ArrayList<>());
        request.put("body", createRequestBody(api));
        request.put("url", createUrl(api));
        return request;
    }


    private Map<String, Object> createRequestBody(Api api) {
        Map<String, Object> body = new HashMap<>();
        body.put("mode", "raw");

        try {
            // Check if the request_body is null or empty
            if (api.getRequest_body() == null || api.getRequest_body().isEmpty()) {
                throw new IllegalArgumentException("Request body is null or empty");
            }

            // Parse the request_body as JSON
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode schemaNode = objectMapper.readTree(api.getRequest_body());

            // Extract just the "properties" section
            JsonNode propertiesNode = schemaNode.path("properties");

            // Create a Map to store properties as key-value pairs (without the "properties" wrapper)
            Map<String, Object> propertiesMap = new HashMap<>();
            Iterator<Map.Entry<String, JsonNode>> fields = propertiesNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                propertiesMap.put(field.getKey(), field.getValue());
            }

            // Convert propertiesMap to a JSON string and add it to the request body
            String propertiesJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(propertiesMap);
            body.put("raw", propertiesJson);
        } catch (Exception e) {
            e.printStackTrace();
            // If error occurs, return a default error message
            body.put("raw", "{\"error\": \"Failed to parse request body\"}");
        }

        return body;
    }


    private Map<String, Object> createUrl(Api api) {
        Map<String, Object> url = new HashMap<>();
        url.put("raw", "{{base_url}}" + api.getPath());
        url.put("host", List.of("{{base_url}}"));
        url.put("path", List.of(api.getPath().split("/")));
        return url;
    }

   private List<Map<String, Object>> createResponses(Api api) {
       List<ApiResponse> apiResponses = apiResponseRepository.findByApi(api);
       List<Map<String, Object>> responses = new ArrayList<>();

       for (ApiResponse apiResponse : apiResponses) {
           Map<String, Object> response = new HashMap<>();
           response.put("status", apiResponse.getStatus());
           response.put("description", apiResponse.getDescription());

           // Add schema body only if the schema is not null
           Schema schema = apiResponse.getSchema();
           if (schema != null) {
               try {
                   // Parse the schema as JSON
                   ObjectMapper objectMapper = new ObjectMapper();
                   JsonNode schemaNode = objectMapper.readTree(schema.getSchemas());

                   // Extract just the "properties" section
                   JsonNode propertiesNode = schemaNode.path("properties");

                   // Create a Map to store properties as key-value pairs
                   Map<String, Object> propertiesMap = new HashMap<>();
                   Iterator<Map.Entry<String, JsonNode>> fields = propertiesNode.fields();
                   while (fields.hasNext()) {
                       Map.Entry<String, JsonNode> field = fields.next();
                       propertiesMap.put(field.getKey(), field.getValue());
                   }

                   // Convert propertiesMap to a JSON string and add it to the response body
                   String propertiesJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(propertiesMap);
                   response.put("body", propertiesJson);
               } catch (Exception e) {
                   e.printStackTrace();
                   // In case of error, return a default error message
                   response.put("body", "{\"error\": \"Failed to parse schema\"}");
               }
           } else {
               response.put("body", "No schema defined");
           }

           responses.add(response);
       }

       return responses;
   }

    private void saveCollectionToFile(Map<String, Object> collection) {
        try {
            // Convertir la collection en JSON
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(collection);

            // Définir le chemin du fichier
            Path path = Paths.get("postman_collection.json");

            // Écrire le fichier
            Files.write(path, json.getBytes());

            System.out.println("Collection saved to file: " + path.toAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to save collection to file: " + e.getMessage());
        }
    }
}*/


import com.example.api_tierces.model.Api;
import com.example.api_tierces.model.ApiResponse;
import com.example.api_tierces.model.Schema;
import com.example.api_tierces.repository.ApiParametersRepository;
import com.example.api_tierces.repository.ApiRepository;
import com.example.api_tierces.repository.ApiResponseRepository;
import com.example.api_tierces.repository.SchemaRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
//hedhiiii shihaaaaaa justeeee fel partie request body mata5edhhech b shihhhh yani aka l'imbrique
/*@RestController
@RequestMapping("/api/postman")
public class PostmanCollectionController {

    @Autowired
    private ApiRepository apiRepository;

    @Autowired
    private ApiParametersRepository apiParametersRepository;

    @Autowired
    private ApiResponseRepository apiResponseRepository;

    @Autowired
    private SchemaRepository schemaRepository;

    @Value("${base.url:http://localhost:8084}") // Default value if not in properties
    private String baseUrl;


    @GetMapping("/collection")
    public Map<String, Object> generatePostmanCollection() {
        List<Api> apis = apiRepository.findAll();
        List<Map<String, Object>> items = new ArrayList<>();

        for (Api api : apis) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", api.getPath());
            item.put("request", createRequest(api));
            item.put("response", createResponses(api));
            items.add(item);
        }

        Map<String, Object> collection = new HashMap<>();
        collection.put("info", createInfo());
        collection.put("item", items);

        // Sauvegarder la collection dans un fichier JSON
        saveCollectionToFile(collection);

        return collection;
    }

    private Map<String, Object> createInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "Generated API Collection");
        info.put("schema", "https://schema.getpostman.com/json/collection/v2.1.0/collection.json");
        return info;
    }

    private Map<String, Object> createRequest(Api api) {
        Map<String, Object> request = new HashMap<>();
        request.put("method", api.getMethod());
        request.put("header", new ArrayList<>());
        request.put("body", createRequestBody(api));
        request.put("url", createUrl(api));
        return request;
    }


    private Map<String, Object> createRequestBody(Api api) {
        Map<String, Object> body = new HashMap<>();
        body.put("mode", "raw");
        if (api.getRequest_body() == null || api.getRequest_body().isEmpty()) {
            body.put("raw", ""); // Empty body if no request body
            return body;
        }


        body.put("raw", api.getRequest_body());

        return body;
    }


    private Map<String, Object> createUrl(Api api) {
        Map<String, Object> url = new HashMap<>();
        url.put("raw", baseUrl + api.getPath()); // Use injected base URL
        url.put("host", List.of(baseUrl));  // Use injected base URL
        url.put("path", Arrays.asList(api.getPath().split("/")));
        return url;
    }

    private List<Map<String, Object>> createResponses(Api api) {
        List<ApiResponse> apiResponses = apiResponseRepository.findByApi(api);
        List<Map<String, Object>> responses = new ArrayList<>();

        for (ApiResponse apiResponse : apiResponses) {
            Map<String, Object> response = new HashMap<>();
            response.put("name", apiResponse.getStatus()); // Changed from "status" to "name"
            response.put("originalRequest",createRequest(api)); //include orignal request to response
            Map<String, String> id = new HashMap<>();
            id.put("id", String.valueOf(new Random().nextInt(1000)));//added random id to avoid warning
            response.put("_postman_previewlanguage", "json");

            //response.put("status", apiResponse.getStatus()); // Remove status, since its the id
            //response.put("description", apiResponse.getDescription()); // Remove description, since we only want body

            Schema schema = apiResponse.getSchema();
            if (schema != null) {
                response.put("body", schema.getSchemas());// get schemas direct
            } else {
                response.put("body", "No schema defined");
            }
            responses.add(response);
        }

        return responses;
    }
    private void saveCollectionToFile(Map<String, Object> collection) {
        try {
            // Convertir la collection en JSON
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(collection);

            // Définir le chemin du fichier
            Path path = Paths.get("test1.json");

            // Écrire le fichier
            Files.write(path, json.getBytes());

            System.out.println("Collection saved to file: " + path.toAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to save collection to file: " + e.getMessage());
        }
    }
}*/

import com.example.api_tierces.model.Api;
import com.example.api_tierces.model.ApiParameters;
import com.example.api_tierces.repository.ApiRepository;
import com.example.api_tierces.repository.ApiParametersRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.example.api_tierces.model.Api;
import com.example.api_tierces.model.ApiParameters;
import com.example.api_tierces.model.ApiResponse;
import com.example.api_tierces.repository.ApiRepository;
import com.example.api_tierces.repository.ApiParametersRepository;
import com.example.api_tierces.repository.ApiResponseRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.example.api_tierces.model.Api;
import com.example.api_tierces.model.ApiParameters;
import com.example.api_tierces.model.ApiResponse;
import com.example.api_tierces.model.Schema;
import com.example.api_tierces.repository.ApiRepository;
import com.example.api_tierces.repository.ApiParametersRepository;
import com.example.api_tierces.repository.ApiResponseRepository;
import com.example.api_tierces.repository.SchemaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
// hedha yemchi juste ena nsalah fel table api_response aka fazet 200 donc ok w nsalah fazet l parametre w fazet request body
/*@RestController
@RequestMapping("/postman")
public class PostmanCollectionController {

    private final ApiRepository apiRepository;
    private final ApiParametersRepository apiParametersRepository;
    private final ApiResponseRepository apiResponseRepository;
    private final SchemaRepository schemaRepository;
    private final ObjectMapper objectMapper;

    public PostmanCollectionController(ApiRepository apiRepository, ApiParametersRepository apiParametersRepository,
                                       ApiResponseRepository apiResponseRepository, SchemaRepository schemaRepository) {
        this.apiRepository = apiRepository;
        this.apiParametersRepository = apiParametersRepository;
        this.apiResponseRepository = apiResponseRepository;
        this.schemaRepository = schemaRepository;
        this.objectMapper = new ObjectMapper();
    }

    @GetMapping("/generate")
    public String generatePostmanCollection() {
        List<Api> apis = apiRepository.findAll();

        ObjectNode collection = objectMapper.createObjectNode();
        collection.putObject("info")
                .put("name", "API Collection from Database")
                .put("schema", "https://schema.getpostman.com/json/collection/v2.1.0/collection.json");

        ArrayNode items = objectMapper.createArrayNode();

        for (Api api : apis) {
            ObjectNode request = objectMapper.createObjectNode();
            request.put("method", api.getMethod());

            // Construction de l'URL avec path variables
            ObjectNode url = objectMapper.createObjectNode();
            url.put("raw", "{{baseUrl}}" + api.getPath());
            ArrayNode hostArray = objectMapper.createArrayNode().add("{{baseUrl}}");
            url.set("host", hostArray);
            ArrayNode pathArray = objectMapper.createArrayNode();

            // Gestion des path variables
            ArrayNode pathVariables = objectMapper.createArrayNode();
            for (String part : api.getPath().split("/")) {
                if (part.startsWith(":")) { // Si c'est un paramètre de path
                    String paramName = part.substring(1); // Retirer le ":"
                    ApiParameters param = apiParametersRepository.findByApi(api).stream()
                            .filter(p -> p.getName().equals(paramName))
                            .findFirst()
                            .orElse(null);
                    ObjectNode variableObj = objectMapper.createObjectNode();
                    variableObj.put("key", paramName);
                    variableObj.put("value", param != null ? param.getData_type() : "string"); // Utiliser le type stocké
                    variableObj.put("description", param != null ? param.getDescription() : "Path variable");
                    pathVariables.add(variableObj);
                }
                pathArray.add(part);
            }
            url.set("path", pathArray);
            if (!pathVariables.isEmpty()) {
                url.set("variable", pathVariables);
            }

            request.set("url", url);

            // Ajout du body s'il existe
            if (api.getRequest_body() != null && !api.getRequest_body().isEmpty()) {
                ObjectNode body = objectMapper.createObjectNode();
                body.put("mode", "raw");
                body.put("raw", api.getRequest_body());
                request.set("body", body);
            }

            // Récupération des réponses sans duplication du status code
            ArrayNode responses = objectMapper.createArrayNode();
            List<ApiResponse> apiResponses = apiResponseRepository.findByApi(api)
                    .stream()
                    .distinct()
                    .collect(Collectors.toList());

            for (ApiResponse response : apiResponses) {
                ObjectNode responseObj = objectMapper.createObjectNode();
                responseObj.put("name", response.getDescription());
                responseObj.put("status", response.getStatus());
                responseObj.put("code", Integer.parseInt(response.getStatus()));

                // Ajout du schéma de réponse stocké dans la base
                if (response.getSchema() != null) {
                    Schema schema = schemaRepository.findById(response.getSchema().getId()).orElse(null);
                    if (schema != null && schema.getSchemas() != null) {
                        responseObj.put("body", schema.getSchemas()); // Utiliser le schéma au lieu d'un exemple
                    }
                }

                ObjectNode headers = objectMapper.createObjectNode();
                headers.put("key", "Content-Type");
                headers.put("value", "application/json");
                responseObj.set("header", objectMapper.createArrayNode().add(headers));

                responses.add(responseObj);
            }

            ObjectNode requestItem = objectMapper.createObjectNode();
            requestItem.put("name", api.getDescription());
            requestItem.set("request", request);
            requestItem.set("response", responses);

            items.add(requestItem);
        }

        collection.set("item", items);

        // Ajout d'une variable de baseUrl
        ArrayNode variables = objectMapper.createArrayNode();
        ObjectNode baseUrlVariable = objectMapper.createObjectNode();
        baseUrlVariable.put("key", "baseUrl");
        baseUrlVariable.put("value", "http://localhost:8080");
        variables.add(baseUrlVariable);
        collection.set("variable", variables);

        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(collection);
        } catch (Exception e) {
            return "{\"error\":\"Failed to generate Postman collection\"}";
        }
    }
}*/
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/*@RestController
@RequestMapping("/postman")
public class PostmanCollectionController {

    private final ApiRepository apiRepository;
    private final ApiParametersRepository apiParametersRepository;
    private final ApiResponseRepository apiResponseRepository;
    private final SchemaRepository schemaRepository;
    private final ObjectMapper objectMapper;

    public PostmanCollectionController(ApiRepository apiRepository, ApiParametersRepository apiParametersRepository,
                                       ApiResponseRepository apiResponseRepository, SchemaRepository schemaRepository) {
        this.apiRepository = apiRepository;
        this.apiParametersRepository = apiParametersRepository;
        this.apiResponseRepository = apiResponseRepository;
        this.schemaRepository = schemaRepository;
        this.objectMapper = new ObjectMapper();
    }

    @GetMapping("/generate")
    public String generatePostmanCollection() {
        List<Api> apis = apiRepository.findAll();

        ObjectNode collection = objectMapper.createObjectNode();
        collection.putObject("info")
                .put("name", "API Collection from Database")
                .put("schema", "https://schema.getpostman.com/json/collection/v2.1.0/collection.json");

        ArrayNode items = objectMapper.createArrayNode();

        for (Api api : apis) {
            ObjectNode request = objectMapper.createObjectNode();
            request.put("method", api.getMethod());

            // Construction de l'URL avec path variables
            ObjectNode url = objectMapper.createObjectNode();
            url.put("raw", "{{baseUrl}}" + api.getPath());
            ArrayNode hostArray = objectMapper.createArrayNode().add("{{baseUrl}}");
            url.set("host", hostArray);
            ArrayNode pathArray = objectMapper.createArrayNode();

            // Gestion des path variables
            ArrayNode pathVariables = objectMapper.createArrayNode();
            for (String part : api.getPath().split("/")) {
                if (part.startsWith(":")) { // Si c'est un paramètre de path
                    String paramName = part.substring(1); // Retirer le ":"
                    ApiParameters param = apiParametersRepository.findByApi(api).stream()
                            .filter(p -> p.getName().equals(paramName))
                            .findFirst()
                            .orElse(null);
                    ObjectNode variableObj = objectMapper.createObjectNode();
                    variableObj.put("key", paramName);
                    variableObj.put("value", param != null ? param.getData_type() : "string"); // Utiliser le type stocké
                    variableObj.put("description", param != null ? param.getDescription() : "Path variable");
                    pathVariables.add(variableObj);
                }
                pathArray.add(part);
            }
            url.set("path", pathArray);

            // Add path variables to URL object in Postman format
            if (!pathVariables.isEmpty()) {
                url.set("variable", pathVariables);
            }

            request.set("url", url);

            // Ajout du body s'il existe
            if (api.getRequest_body() != null && !api.getRequest_body().isEmpty()) {
                ObjectNode body = objectMapper.createObjectNode();
                body.put("mode", "raw");

                // Extract the desired part of the request body if possible
                try {
                    JsonNode requestBodyNode = objectMapper.readTree(api.getRequest_body());
                    if (requestBodyNode.isObject()) {
                        ObjectNode filteredBody = objectMapper.createObjectNode();
                        if (requestBodyNode.has("id")) filteredBody.put("id", requestBodyNode.get("id").asText("<long>"));
                        if (requestBodyNode.has("nom")) filteredBody.put("nom", requestBodyNode.get("nom").asText("<string>"));
                        if (requestBodyNode.has("prenom")) filteredBody.put("prenom", requestBodyNode.get("prenom").asText("<string>"));
                        if (requestBodyNode.has("accountNumber")) filteredBody.put("accountNumber", requestBodyNode.get("accountNumber").asText("<string>"));
                        if (requestBodyNode.has("total")) filteredBody.put("total", requestBodyNode.get("total").asText("<number>"));
                        if (requestBodyNode.has("currency")) filteredBody.put("currency", requestBodyNode.get("currency").asText("<string>"));

                        body.put("raw", filteredBody.toString());
                    } else {
                        body.put("raw", api.getRequest_body()); // Fallback to the original if not a JSON object
                    }
                } catch (Exception e) {
                    body.put("raw", api.getRequest_body()); // Fallback to the original if parsing fails
                }

                request.set("body", body);
            }

            // Récupération des réponses sans duplication du status code
            ArrayNode responses = objectMapper.createArrayNode();
            List<ApiResponse> apiResponses = apiResponseRepository.findByApi(api)
                    .stream()
                    .distinct()
                    .collect(Collectors.toList());

            for (ApiResponse response : apiResponses) {
                ObjectNode responseObj = objectMapper.createObjectNode();
                responseObj.put("name", response.getDescription());
                responseObj.put("code", Integer.parseInt(response.getStatus()));  // Store the status code
                responseObj.put("status", getStatusText(Integer.parseInt(response.getStatus()))); // Store the status text

                // Ajout du schéma de réponse stocké dans la base
                if (response.getSchema() != null) {
                    Schema schema = schemaRepository.findById(response.getSchema().getId()).orElse(null);
                    if (schema != null && schema.getSchemas() != null) {
                        try{
                            JsonNode schemaNode = objectMapper.readTree(schema.getSchemas());
                            responseObj.put("body", schemaNode.toString()); // Add the parsed schema as "body"
                        }catch (Exception e){
                            responseObj.put("body", schema.getSchemas()); // fallback to original if parsing failed
                        }

                    }
                }

                ObjectNode headers = objectMapper.createObjectNode();
                headers.put("key", "Content-Type");
                headers.put("value", "application/json");
                responseObj.set("header", objectMapper.createArrayNode().add(headers));

                responses.add(responseObj);
            }

            ObjectNode requestItem = objectMapper.createObjectNode();
            requestItem.put("name", api.getDescription());
            requestItem.set("request", request);
            requestItem.set("response", responses);

            items.add(requestItem);
        }

        collection.set("item", items);

        // Ajout d'une variable de baseUrl
        ArrayNode variables = objectMapper.createArrayNode();
        ObjectNode baseUrlVariable = objectMapper.createObjectNode();
        baseUrlVariable.put("key", "baseUrl");
        baseUrlVariable.put("value", "http://localhost:8080");
        variables.add(baseUrlVariable);
        collection.set("variable", variables);

        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(collection);
        } catch (Exception e) {
            return "{\"error\":\"Failed to generate Postman collection\"}";
        }
    }

    private String getStatusText(int statusCode) {
        switch (statusCode) {
            case 200:
                return "OK";
            case 201:
                return "Created";
            case 204:
                return "No Content";
            case 400:
                return "Bad Request";
            case 401:
                return "Unauthorized";
            case 403:
                return "Forbidden";
            case 404:
                return "Not Found";
            case 500:
                return "Internal Server Error";
            default:
                return "Unknown";
        }
    }
}*/
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/*@RestController
@RequestMapping("/postman")
public class PostmanCollectionController {

    private final ApiRepository apiRepository;
    private final ApiParametersRepository apiParametersRepository;
    private final ApiResponseRepository apiResponseRepository;
    private final SchemaRepository schemaRepository;
    private final ObjectMapper objectMapper;

    public PostmanCollectionController(ApiRepository apiRepository, ApiParametersRepository apiParametersRepository,
                                       ApiResponseRepository apiResponseRepository, SchemaRepository schemaRepository) {
        this.apiRepository = apiRepository;
        this.apiParametersRepository = apiParametersRepository;
        this.apiResponseRepository = apiResponseRepository;
        this.schemaRepository = schemaRepository;
        this.objectMapper = new ObjectMapper();
    }

    @GetMapping("/generate")
    public String generatePostmanCollection() {
        List<Api> apis = apiRepository.findAll();

        ObjectNode collection = objectMapper.createObjectNode();
        collection.putObject("info")
                .put("name", "Banking App API")
                .put("description", "API pour la gestion des comptes bancaires et des transactions.")
                .put("schema", "https://schema.getpostman.com/json/collection/v2.1.0/collection.json");

        ArrayNode items = objectMapper.createArrayNode();

        for (Api api : apis) {
            ObjectNode requestItem = objectMapper.createObjectNode();
            requestItem.put("name", api.getDescription());

            ObjectNode request = objectMapper.createObjectNode();
            request.put("method", api.getMethod());

            // URL Handling (including path variables)
            ObjectNode url = objectMapper.createObjectNode();
            ArrayNode hostArray = objectMapper.createArrayNode().add("{{baseUrl}}");
            url.set("host", hostArray);

            ArrayNode pathArray = objectMapper.createArrayNode();
            ArrayNode pathVariablesArray = objectMapper.createArrayNode();
            String rawUrl = "{{baseUrl}}" + api.getPath();
            StringBuilder rawPathBuilder = new StringBuilder();

            //Use regex to find the parameters between {}
            Pattern pattern = Pattern.compile("\\{(.*?)\\}");
            Matcher matcher = pattern.matcher(rawUrl);

            String finalRawUrl = rawUrl.replaceAll("\\{(.*?)\\}", ":$1");

            String[] pathParts = api.getPath().split("/");
            for (String part : pathParts) {
                if (part.startsWith(":")) {
                    String paramName = part.substring(1);
                    ApiParameters apiParam = apiParametersRepository.findByApi(api).stream()
                            .filter(p -> p.getName().equals(paramName))
                            .findFirst().orElse(null);

                    ObjectNode variable = objectMapper.createObjectNode();
                    variable.put("key", paramName);
                    variable.put("value", "<" + (apiParam != null ? apiParam.getData_type() : "string") + ">");  // Set the actual data type
                    variable.put("description", apiParam != null ? apiParam.getDescription() : "");  //Set the parameter description.
                    pathVariablesArray.add(variable);

                } else {
                    pathArray.add(part);
                }
            }
            url.set("path", pathArray);
            url.put("raw", finalRawUrl); //raw url has also the :

            if (pathVariablesArray.size() > 0) {
                url.set("variable", pathVariablesArray);
            }
            request.set("url", url);

            // Body handling.
            if (api.getRequest_body() != null && !api.getRequest_body().isEmpty()) {
                try {
                    JsonNode requestBodyNode = objectMapper.readTree(api.getRequest_body());

                    if (requestBodyNode.isObject()) {
                        ObjectNode processedBody = processJsonNode(requestBodyNode); // Use the processing method
                        ObjectNode body = objectMapper.createObjectNode();
                        body.put("mode", "raw");
                        body.put("raw", processedBody.toString());

                        ObjectNode options = objectMapper.createObjectNode();
                        ObjectNode rawOptions = objectMapper.createObjectNode();
                        rawOptions.put("headerFamily", "json");
                        rawOptions.put("language", "json");
                        options.set("raw", rawOptions);
                        body.set("options", options);

                        request.set("body", body);
                    }else{
                        ObjectNode body = objectMapper.createObjectNode();
                        body.put("mode", "raw");
                        body.put("raw", api.getRequest_body());

                        ObjectNode options = objectMapper.createObjectNode();
                        ObjectNode rawOptions = objectMapper.createObjectNode();
                        rawOptions.put("headerFamily", "json");
                        rawOptions.put("language", "json");
                        options.set("raw", rawOptions);
                        body.set("options", options);

                        request.set("body", body);
                    }

                } catch (Exception e) {
                    System.err.println("Error processing request body: " + e.getMessage());

                }
            }

            //Response handling
            ArrayNode responses = objectMapper.createArrayNode();
            List<ApiResponse> apiResponses = apiResponseRepository.findByApi(api);

            for (ApiResponse apiResponse : apiResponses) {
                ObjectNode response = objectMapper.createObjectNode();
                response.put("name", apiResponse.getDescription());
                response.put("code", Integer.parseInt(apiResponse.getStatus()));  //Ensure Integer
                response.put("status", getStatusText(Integer.parseInt(apiResponse.getStatus()))); //Status text.

                ObjectNode header = objectMapper.createObjectNode();
                header.put("key", "Content-Type");
                header.put("value", "application/json");
                ArrayNode headerArray = objectMapper.createArrayNode();
                headerArray.add(header);
                response.set("header", headerArray); //Set the Header of type json.

                //Response Body Handling
                Schema schema = apiResponse.getSchema();
                if (schema != null && schema.getSchemas() != null && !schema.getSchemas().isEmpty()) {
                    try {
                        JsonNode schemaNode = objectMapper.readTree(schema.getSchemas());
                        response.put("body", processJsonNode(schemaNode).toString());
                    }catch(Exception e){
                        response.put("body", schema.getSchemas());
                    }

                } else {
                    response.put("body", "");
                }

                responses.add(response);
            }

            requestItem.set("request", request);
            requestItem.set("response", responses);
            items.add(requestItem);
        }

        collection.set("item", items);

        //Add base URL variable
        ArrayNode variables = objectMapper.createArrayNode();
        ObjectNode baseUrlVariable = objectMapper.createObjectNode();
        baseUrlVariable.put("key", "baseUrl");
        baseUrlVariable.put("value", "http://localhost:8080");
        variables.add(baseUrlVariable);
        collection.set("variable", variables);

        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(collection);
        } catch (Exception e) {
            return "{\"error\":\"Failed to generate Postman collection\"}";
        }
    }

    // Recursive method to process JsonNode and replace nested schemas
    private ObjectNode processJsonNode(JsonNode node) {
        ObjectNode result = objectMapper.createObjectNode();

        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> {
                String fieldName = entry.getKey();
                JsonNode fieldValue = entry.getValue();

                if (fieldValue.isObject() && fieldValue.has("$ref")) {
                    // Replace $ref with placeholder
                    result.put(fieldName, "<object>");
                } else if (fieldValue.isObject()) {
                    result.set(fieldName, processJsonNode(fieldValue)); // Recursive call for nested objects
                } else if (fieldValue.isTextual()) {
                    result.put(fieldName, "<string>");
                } else if (fieldValue.isNumber()) {
                    result.put(fieldName, "<number>");
                } else if (fieldValue.isBoolean()) {
                    result.put(fieldName, "<boolean>");
                } else if (fieldValue.isInt()) {
                    result.put(fieldName, "<integer>");
                }else {
                    result.set(fieldName, fieldValue);
                }
            });
        }

        return result;
    }

    private String getStatusText(int statusCode) {
        switch (statusCode) {
            case 200:
                return "OK";
            case 201:
                return "Created";
            case 204:
                return "No Content";
            case 400:
                return "Bad Request";
            case 401:
                return "Unauthorized";
            case 403:
                return "Forbidden";
            case 404:
                return "Not Found";
            case 500:
                return "Internal Server Error";
            default:
                return "Unknown";
        }
    }
}*/



import com.example.api_tierces.model.*;
import com.example.api_tierces.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/*@RestController
@RequestMapping("/postman")
public class PostmanCollectionController {

    @Autowired
    private ApiRepository apiRepository;

    @Autowired
    private ApiParametersRepository apiParametersRepository;

    @Autowired
    private ApiResponseRepository apiResponseRepository;

    @Autowired
    private SchemaRepository schemaRepository;

    @Autowired
    private ObjectMapper objectMapper; // Pour manipuler les JSON

    @GetMapping("/collection")
    public ResponseEntity<Map<String, Object>> generatePostmanCollection() {
        // Créer la structure de base de la collection Postman
        Map<String, Object> collection = new HashMap<>();
        collection.put("info", createInfo());
        collection.put("item", createItems());

        // Ajouter les variables d'environnement
        collection.put("variable", createEnvironmentVariables());

        return ResponseEntity.ok(collection);
    }

    private Map<String, Object> createInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "Banking App API");
        info.put("description", "API pour la gestion des comptes bancaires et des transactions.");
        info.put("schema", "https://schema.getpostman.com/json/collection/v2.1.0/collection.json");
        return info;
    }

    private List<Map<String, Object>> createItems() {
        List<Map<String, Object>> items = new ArrayList<>();

        // Récupérer toutes les APIs
        List<Api> apis = apiRepository.findAll();

        for (Api api : apis) {
            Map<String, Object> apiItem = new HashMap<>();
            apiItem.put("name", api.getPath());

            // Créer les requêtes pour chaque API
            List<Map<String, Object>> requests = new ArrayList<>();
            Map<String, Object> request = new HashMap<>();
            request.put("name", api.getDescription());
            request.put("request", createRequest(api));
            requests.add(request);

            apiItem.put("item", requests);
            items.add(apiItem);
        }

        return items;
    }

    private Map<String, Object> createRequest(Api api) {
        Map<String, Object> request = new HashMap<>();
        request.put("method", api.getMethod());

        // Ajouter les en-têtes
        List<Map<String, String>> headers = new ArrayList<>();
        headers.add(createHeader("Accept", "application/json"));
        request.put("header", headers);

        // Ajouter l'URL avec les paramètres de chemin et les query parameters
        Map<String, Object> url = createUrl(api);
        request.put("url", url);

        // Ajouter le corps de la requête si nécessaire
        if (api.getRequest_body() != null && !api.getRequest_body().isEmpty()) {
            request.put("body", createRequestBody(api));
        }

        // Ajouter les réponses
        List<Map<String, Object>> responses = createResponses(api);
        request.put("response", responses);

        return request;
    }

    private Map<String, String> createHeader(String key, String value) {
        Map<String, String> header = new HashMap<>();
        header.put("key", key);
        header.put("value", value);
        return header;
    }

    private Map<String, Object> createUrl(Api api) {
        Map<String, Object> url = new HashMap<>();
        String rawUrl = "{{baseUrl}}" + transformPathVariables(api.getPath());
        url.put("raw", rawUrl);
        url.put("host", List.of("{{baseUrl}}"));

        // Découper le chemin en segments
        String[] pathSegments = api.getPath().split("/");
        url.put("path", Arrays.asList(pathSegments));

        // Ajouter les query parameters s'ils existent
        List<Map<String, Object>> queryParams = createQueryParameters(api);
        if (!queryParams.isEmpty()) {
            url.put("query", queryParams);
        }

        return url;
    }

    private String transformPathVariables(String path) {
        // Remplacer les {id} par :id pour correspondre au format Postman
        return path.replaceAll("\\{(.*?)\\}", ":$1");
    }

    private List<Map<String, Object>> createQueryParameters(Api api) {
        List<ApiParameters> parameters = apiParametersRepository.findByApiId(api.getId());
        return parameters.stream()
                .filter(param -> "query".equals(param.getTypein())) // Filtrer les query parameters
                .map(this::createQueryParameter)
                .collect(Collectors.toList());
    }

    private Map<String, Object> createQueryParameter(ApiParameters parameter) {
        Map<String, Object> queryParam = new HashMap<>();
        queryParam.put("key", parameter.getName());
        queryParam.put("value", parameter.getExample()); // Utiliser la valeur de l'exemple
        queryParam.put("description", parameter.getDescription());
        return queryParam;
    }

    private Map<String, Object> createRequestBody(Api api) {
        Map<String, Object> body = new HashMap<>();
        body.put("mode", "raw");

        // Transformer le JSON Schema en format simplifié
        String simplifiedBody = transformJsonSchemaToSimplifiedFormat(api.getRequest_body());
        body.put("raw", simplifiedBody);

        body.put("options", Map.of("raw", Map.of("language", "json")));
        return body;
    }

    private String transformJsonSchemaToSimplifiedFormat(String jsonSchema) {
        try {
            // Convertir le JSON Schema en Map
            Map<String, Object> schemaMap = objectMapper.readValue(jsonSchema, Map.class);

            // Extraire les propriétés
            Map<String, Object> properties = (Map<String, Object>) schemaMap.get("properties");

            // Créer un nouveau format simplifié
            Map<String, String> simplifiedMap = new HashMap<>();
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                String key = entry.getKey();
                Map<String, String> valueMap = (Map<String, String>) entry.getValue();
                simplifiedMap.put(key, "<" + valueMap.get("type") + ">");
            }

            // Convertir en JSON
            return objectMapper.writeValueAsString(simplifiedMap);
        } catch (Exception e) {
            e.printStackTrace();
            return "{}"; // Retourner un objet vide en cas d'erreur
        }
    }

    private List<Map<String, Object>> createResponses(Api api) {
        List<ApiResponse> apiResponses = apiResponseRepository.findByApiId(api.getId());
        return apiResponses.stream().map(this::createResponse).collect(Collectors.toList());
    }

    private Map<String, Object> createResponse(ApiResponse apiResponse) {
        Map<String, Object> response = new HashMap<>();
        response.put("name", apiResponse.getDescription());
        response.put("status", apiResponse.getStatus());
        response.put("code", Integer.parseInt(apiResponse.getStatus().split(" ")[0]));

        // Ajouter le corps de la réponse si un schéma est associé
        if (apiResponse.getSchema() != null) {
            String simplifiedBody = transformJsonSchemaToSimplifiedFormat(apiResponse.getSchema().getSchemas());
            response.put("body", simplifiedBody);
        }

        return response;
    }

    private List<Map<String, String>> createEnvironmentVariables() {
        List<Map<String, String>> variables = new ArrayList<>();

        // Ajouter uniquement la variable baseUrl
        variables.add(createEnvironmentVariable("baseUrl", "http://localhost:8080", "URL de base de l'API"));

        return variables;
    }

    private Map<String, String> createEnvironmentVariable(String key, String value, String description) {
        Map<String, String> variable = new HashMap<>();
        variable.put("key", key);
        variable.put("value", value);
        variable.put("description", description);
        return variable;
    }
}*/
// adheyaaaaa yemchiiiii les problémes fazet request body imbriqué , ou fazet vertical ou fazet 200 200
/*@RestController
@RequestMapping("/postman")
public class PostmanCollectionController {

    @Autowired
    private ApiRepository apiRepository;

    @Autowired
    private ApiParametersRepository apiParametersRepository;

    @Autowired
    private ApiResponseRepository apiResponseRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/collection")
    public ResponseEntity<Map<String, Object>> generatePostmanCollection() {
        Map<String, Object> collection = new HashMap<>();
        collection.put("info", createInfo());
        collection.put("item", createItems());
        collection.put("variable", createEnvironmentVariables());
        return ResponseEntity.ok(collection);
    }

    private Map<String, Object> createInfo() {
        return Map.of(
                "name", "Banking App API",
                "description", "API pour la gestion des comptes bancaires et des transactions.",
                "schema", "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
        );
    }

    private List<Map<String, Object>> createItems() {
        return apiRepository.findAll().stream().map(this::createApiItem).collect(Collectors.toList());
    }

    private Map<String, Object> createApiItem(Api api) {
        Map<String, Object> apiItem = new HashMap<>();
        apiItem.put("name", api.getPath());

        List<Map<String, Object>> requests = new ArrayList<>();
        Map<String, Object> request = new HashMap<>();
        request.put("name", api.getDescription());
        request.put("request", createRequest(api));
        request.put("response", createResponses(api));
        requests.add(request);

        apiItem.put("item", requests);
        return apiItem;
    }

    private Map<String, Object> createRequest(Api api) {
        Map<String, Object> request = new HashMap<>();
        request.put("method", api.getMethod());
        request.put("header", List.of(createHeader("Accept", "application/json")));
        request.put("url", createUrl(api));

        if (api.getRequest_body() != null && !api.getRequest_body().isEmpty()) {
            request.put("body", createRequestBody(api));
        }
        return request;
    }

    private Map<String, Object> createUrl(Api api) {
        String rawUrl = "{{baseUrl}}" + transformPathVariables(api.getPath());
        List<String> pathSegments = Arrays.asList(rawUrl.replace("{{baseUrl}}/", "").split("/"));

        Map<String, Object> url = new HashMap<>();
        url.put("raw", rawUrl);
        url.put("host", List.of("{{baseUrl}}"));
        url.put("path", pathSegments);
        url.put("variable", createPathVariables(api));
        url.put("query", createQueryParameters(api));
        return url;
    }

    private String transformPathVariables(String path) {
        return path.replaceAll("\\{(.*?)\\}", ":$1");
    }

    private List<Map<String, Object>> createPathVariables(Api api) {
        return apiParametersRepository.findByApiId(api.getId()).stream()
                .filter(param -> "path".equals(param.getTypein())) // Sélectionne uniquement les path variables
                .map(param -> Map.of(
                        "key", (Object) param.getName(),
                        "value", (Object) ("<" + (param.getData_type() != null ? param.getData_type() : "string") + ">"), // Correction ici
                        "description", (Object) param.getDescription()
                ))
                .collect(Collectors.toList());
    }



    private List<Map<String, Object>> createQueryParameters(Api api) {
        return apiParametersRepository.findByApiId(api.getId()).stream()
                .filter(param -> "query".equals(param.getTypein())) // Sélectionne uniquement les query parameters
                .map(param -> Map.of(
                        "key", param.getName(),
                        "value", (Object) param.getExample(), // Assure la compatibilité avec Map<String, Object>
                        "description", param.getDescription()
                ))
                .collect(Collectors.toList());
    }


    private Map<String, Object> createRequestBody(Api api) {
        return Map.of(
                "mode", "raw",
                "raw", transformJsonSchemaToSimplifiedFormat(api.getRequest_body()),
                "options", Map.of("raw", Map.of("language", "json"))
        );
    }

    private List<Map<String, Object>> createResponses(Api api) {
        return apiResponseRepository.findByApiId(api.getId()).stream()
                .map(this::createResponse)
                .collect(Collectors.toList());
    }

    private Map<String, Object> createResponse(ApiResponse apiResponse) {
        String responseBody = "{}"; // Valeur par défaut si aucun schéma n'est défini

        if (apiResponse.getSchema() != null && apiResponse.getSchema().getSchemas() != null) {
            responseBody = transformJsonSchemaToSimplifiedFormat(apiResponse.getSchema().getSchemas());
        }

        return Map.of(
                "name", apiResponse.getDescription(),
                "status", apiResponse.getStatus(),
                "code", Integer.parseInt(apiResponse.getStatus().split(" ")[0]),
                "header", List.of(createHeader("Content-Type", "application/json")),
                "body", responseBody
        );
    }


    private String transformJsonSchemaToSimplifiedFormat(String jsonSchema) {
        try {
            Map<String, Object> schemaMap = objectMapper.readValue(jsonSchema, Map.class);
            Map<String, Object> properties = (Map<String, Object>) schemaMap.get("properties");
            Map<String, String> simplifiedMap = new HashMap<>();

            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                Map<String, String> valueMap = (Map<String, String>) entry.getValue();
                simplifiedMap.put(entry.getKey(), "<" + valueMap.get("type") + ">");
            }
            return objectMapper.writeValueAsString(simplifiedMap);
        } catch (Exception e) {
            return "{}";
        }
    }

    private List<Map<String, String>> createEnvironmentVariables() {
        return List.of(Map.of("key", "baseUrl", "value", "http://localhost:8080", "description", "URL de base de l'API"));
    }

    private Map<String, String> createHeader(String key, String value) {
        return Map.of("key", key, "value", value);
    }
}*/
//hedhaaa jawouuu behy juste probleme fel request body w 200 200
/*@RestController
@RequestMapping("/postman")
public class PostmanCollectionController {

    @Autowired
    private ApiRepository apiRepository;

    @Autowired
    private ApiParametersRepository apiParametersRepository;

    @Autowired
    private ApiResponseRepository apiResponseRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/collection")
    public ResponseEntity<Map<String, Object>> generatePostmanCollection() {
        Map<String, Object> collection = new HashMap<>();
        collection.put("info", createInfo());
        collection.put("item", createItems());
        collection.put("variable", createEnvironmentVariables());
        return ResponseEntity.ok(collection);
    }

    private Map<String, Object> createInfo() {
        return Map.of(
                "name", "Banking App API",
                "description", "API pour la gestion des comptes bancaires et des transactions.",
                "schema", "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
        );
    }

    private List<Map<String, Object>> createItems() {
        return apiRepository.findAll().stream().map(this::createApiItem).collect(Collectors.toList());
    }

    private Map<String, Object> createApiItem(Api api) {
        Map<String, Object> apiItem = new HashMap<>();
        apiItem.put("name", api.getPath());

        List<Map<String, Object>> requests = new ArrayList<>();
        Map<String, Object> request = new HashMap<>();
        request.put("name", api.getDescription());
        request.put("request", createRequest(api));
        request.put("response", createResponses(api));
        requests.add(request);

        apiItem.put("item", requests);
        return apiItem;
    }

    private Map<String, Object> createRequest(Api api) {
        Map<String, Object> request = new HashMap<>();
        request.put("method", api.getMethod());
        request.put("header", List.of(createHeader("Accept", "application/json")));
        request.put("url", createUrl(api));

        if (api.getRequest_body() != null && !api.getRequest_body().isEmpty()) {
            request.put("body", createRequestBody(api));
        }
        return request;
    }

    private Map<String, Object> createUrl(Api api) {
        String rawUrl = "{{baseUrl}}" + transformPathVariables(api.getPath());
        List<String> pathSegments = Arrays.asList(rawUrl.replace("{{baseUrl}}/", "").split("/"));

        Map<String, Object> url = new HashMap<>();
        url.put("raw", rawUrl);
        url.put("host", List.of("{{baseUrl}}"));
        url.put("path", pathSegments);
        url.put("variable", createPathVariables(api));
        url.put("query", createQueryParameters(api));
        return url;
    }

    private String transformPathVariables(String path) {
        return path.replaceAll("\\{(.*?)\\}", ":$1");
    }

    private List<Map<String, Object>> createPathVariables(Api api) {
        return apiParametersRepository.findByApiId(api.getId()).stream()
                .filter(param -> "path".equals(param.getTypein())) // Sélectionne uniquement les path variables
                .map(param -> Map.of(
                        "key", (Object) param.getName(),
                        "value", (Object) ("<" + (param.getData_type() != null ? param.getData_type() : "string") + ">"), // Correction ici
                        "description", (Object) param.getDescription()
                ))
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> createQueryParameters(Api api) {
        return apiParametersRepository.findByApiId(api.getId()).stream()
                .filter(param -> "query".equals(param.getTypein())) // Sélectionne uniquement les query parameters
                .map(param -> Map.of(
                        "key", param.getName(),
                        "value", (Object) param.getExample(), // Assure la compatibilité avec Map<String, Object>
                        "description", param.getDescription()
                ))
                .collect(Collectors.toList());
    }

    private Map<String, Object> createRequestBody(Api api) {
        return Map.of(
                "mode", "raw",
                "raw", transformJsonSchemaToSimplifiedFormat(api.getRequest_body()),
                "options", Map.of("raw", Map.of("language", "json"))
        );
    }

    private List<Map<String, Object>> createResponses(Api api) {
        return apiResponseRepository.findByApiId(api.getId()).stream()
                .map(this::createResponse)
                .collect(Collectors.toList());
    }

    private Map<String, Object> createResponse(ApiResponse apiResponse) {
        String responseBody = "{}"; // Valeur par défaut si aucun schéma n'est défini

        if (apiResponse.getSchema() != null && apiResponse.getSchema().getSchemas() != null) {
            responseBody = transformJsonSchemaToSimplifiedFormat(apiResponse.getSchema().getSchemas());
        }

        return Map.of(
                "name", apiResponse.getDescription(),
                "status", apiResponse.getStatus(),
                "code", Integer.parseInt(apiResponse.getStatus().split(" ")[0]),
                "header", List.of(createHeader("Content-Type", "application/json")),
                "body", responseBody
        );
    }

    private String transformJsonSchemaToSimplifiedFormat(String jsonSchema) {
        try {
            JsonNode schemaNode = objectMapper.readTree(jsonSchema);
            JsonNode propertiesNode = schemaNode.get("properties");

            if (propertiesNode != null) {
                return objectMapper.writeValueAsString(processJsonNode(propertiesNode));
            }

            return "{}";
        } catch (Exception e) {
            return "{}";
        }
    }

    private ObjectNode processJsonNode(JsonNode node) {
        ObjectNode result = objectMapper.createObjectNode();

        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> {
                String fieldName = entry.getKey();
                JsonNode fieldValue = entry.getValue();

                if (fieldValue.isObject() && fieldValue.has("$ref")) {
                    // Remplacer la référence par un placeholder
                    result.put(fieldName, "<object>");
                } else if (fieldValue.isObject()) {
                    // Appel récursif pour les objets imbriqués
                    result.set(fieldName, processJsonNode(fieldValue));
                } else if (fieldValue.isTextual()) {
                    result.put(fieldName, "<string>");
                } else if (fieldValue.isNumber()) {
                    result.put(fieldName, "<number>");
                } else if (fieldValue.isBoolean()) {
                    result.put(fieldName, "<boolean>");
                } else if (fieldValue.isInt()) {
                    result.put(fieldName, "<integer>");
                } else {
                    result.set(fieldName, fieldValue); // Pour gérer les autres types de données
                }
            });
        }
        return result;
    }

    private List<Map<String, String>> createEnvironmentVariables() {
        return List.of(Map.of("key", "baseUrl", "value", "http://localhost:8080", "description", "URL de base de l'API"));
    }

    private Map<String, String> createHeader(String key, String value) {
        return Map.of("key", key, "value", value);
    }
}*/
/*@RestController
@RequestMapping("/postman")
public class PostmanCollectionController {

    @Autowired
    private ApiRepository apiRepository;

    @Autowired
    private ApiParametersRepository apiParametersRepository;

    @Autowired
    private ApiResponseRepository apiResponseRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/collection")
    public ResponseEntity<Map<String, Object>> generatePostmanCollection() {
        Map<String, Object> collection = new HashMap<>();
        collection.put("info", createInfo());
        collection.put("item", createItems());
        collection.put("variable", createEnvironmentVariables());
        return ResponseEntity.ok(collection);
    }

    private Map<String, Object> createInfo() {
        return Map.of(
                "name", "Banking App API",
                "description", "API pour la gestion des comptes bancaires et des transactions.",
                "schema", "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
        );
    }

    private List<Map<String, Object>> createItems() {
        return apiRepository.findAll().stream().map(this::createApiItem).collect(Collectors.toList());
    }

    private Map<String, Object> createApiItem(Api api) {
        Map<String, Object> apiItem = new HashMap<>();
        apiItem.put("name", api.getPath());

        List<Map<String, Object>> requests = new ArrayList<>();
        Map<String, Object> request = new HashMap<>();
        request.put("name", api.getDescription());
        request.put("request", createRequest(api));
        request.put("response", createResponses(api));
        requests.add(request);

        apiItem.put("item", requests);
        return apiItem;
    }

    private Map<String, Object> createRequest(Api api) {
        Map<String, Object> request = new HashMap<>();
        request.put("method", api.getMethod());
        request.put("header", List.of(createHeader("Accept", "application/json")));
        request.put("url", createUrl(api));

        if (api.getRequest_body() != null && !api.getRequest_body().isEmpty()) {
            request.put("body", createRequestBody(api));
        }
        return request;
    }

    private Map<String, Object> createUrl(Api api) {
        String rawUrl = "{{baseUrl}}" + transformPathVariables(api.getPath());
        List<String> pathSegments = Arrays.asList(rawUrl.replace("{{baseUrl}}/", "").split("/"));

        Map<String, Object> url = new HashMap<>();
        url.put("raw", rawUrl);
        url.put("host", List.of("{{baseUrl}}"));
        url.put("path", pathSegments);
        url.put("variable", createPathVariables(api));
        url.put("query", createQueryParameters(api));
        return url;
    }

    private String transformPathVariables(String path) {
        return path.replaceAll("\\{(.*?)\\}", ":$1");
    }

    private List<Map<String, Object>> createPathVariables(Api api) {
        return apiParametersRepository.findByApiId(api.getId()).stream()
                .filter(param -> "path".equals(param.getTypein())) // Sélectionne uniquement les path variables
                .map(param -> Map.of(
                        "key", (Object) param.getName(),
                        "value", (Object) ("<" + (param.getData_type() != null ? param.getData_type() : "string") + ">"),
                        "description", (Object) param.getDescription()
                ))
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> createQueryParameters(Api api) {
        return apiParametersRepository.findByApiId(api.getId()).stream()
                .filter(param -> "query".equals(param.getTypein())) // Sélectionne uniquement les query parameters
                .map(param -> Map.of(
                        "key", param.getName(),
                        "value", (Object) param.getExample(),
                        "description", param.getDescription()
                ))
                .collect(Collectors.toList());
    }

    private Map<String, Object> createRequestBody(Api api) {
        return Map.of(
                "mode", "raw",
                "raw", transformJsonSchemaToSimplifiedFormat(api.getRequest_body()),
                "options", Map.of("raw", Map.of("language", "json"))
        );
    }

    private List<Map<String, Object>> createResponses(Api api) {
        return apiResponseRepository.findByApiId(api.getId()).stream()
                .map(this::createResponse)
                .collect(Collectors.toList());
    }

    private Map<String, Object> createResponse(ApiResponse apiResponse) {
        String responseBody = "{}"; // Valeur par défaut si aucun schéma n'est défini

        if (apiResponse.getSchema() != null && apiResponse.getSchema().getSchemas() != null) {
            responseBody = transformJsonSchemaToSimplifiedFormat(apiResponse.getSchema().getSchemas());
        }

        return Map.of(
                "name", apiResponse.getDescription(),
                "status", apiResponse.getStatus(),
                "code", Integer.parseInt(apiResponse.getStatus().split(" ")[0]),
                "header", List.of(createHeader("Content-Type", "application/json")),
                "body", responseBody
        );
    }

    private String transformJsonSchemaToSimplifiedFormat(String jsonSchema) {
        try {
            JsonNode schemaNode = objectMapper.readTree(jsonSchema);
            JsonNode propertiesNode = schemaNode.get("properties");

            if (propertiesNode != null) {
                return objectMapper.writeValueAsString(processJsonNode(propertiesNode));
            }

            return "{}";
        } catch (Exception e) {
            return "{}";
        }
    }

    private ObjectNode processJsonNode(JsonNode node) {
        ObjectNode result = objectMapper.createObjectNode();

        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> {
                String fieldName = entry.getKey();
                JsonNode fieldValue = entry.getValue();

                // Si le champ est "id", ne garder que le type sans format
                if ("id".equals(fieldName)) {
                    if (fieldValue.isObject()) {
                        JsonNode typeNode = fieldValue.get("type");
                        if (typeNode != null) {
                            result.put(fieldName, typeNode.asText()); // Conserver uniquement le type
                        }
                    }
                } else {
                    // Si le champ est un objet imbriqué, le traiter récursivement
                    if (fieldValue.isObject()) {
                        if (fieldValue.has("properties")) {
                            // Si le champ contient un schéma imbriqué avec des propriétés, traiter son contenu
                            JsonNode propertiesNode = fieldValue.get("properties");
                            result.set(fieldName, processJsonNode(propertiesNode));
                        } else {
                            // Sinon, garder le champ en tant qu'objet générique
                            result.put(fieldName, "<object>");
                        }
                    } else if (fieldValue.isTextual()) {
                        result.put(fieldName, "<string>");
                    } else if (fieldValue.isNumber()) {
                        result.put(fieldName, "<number>");
                    } else if (fieldValue.isBoolean()) {
                        result.put(fieldName, "<boolean>");
                    } else if (fieldValue.isInt()) {
                        result.put(fieldName, "<integer>");
                    } else if (fieldValue.isArray()) {
                        result.put(fieldName, "<array>");
                    } else if (fieldValue.isNull()) {
                        result.put(fieldName, "<null>");
                    }
                }
            });
        }
        return result;
    }



    private List<Map<String, String>> createEnvironmentVariables() {
        return List.of(Map.of("key", "baseUrl", "value", "http://localhost:8080", "description", "URL de base de l'API"));
    }

    private Map<String, String> createHeader(String key, String value) {
        return Map.of("key", key, "value", value);
    }
}*/
/*@RestController
@RequestMapping("/postman")
public class PostmanCollectionController {

    @Autowired
    private ApiRepository apiRepository;

    @Autowired
    private ApiParametersRepository apiParametersRepository;

    @Autowired
    private ApiResponseRepository apiResponseRepository;

    @Autowired
    private SchemaRepository schemaRepository; // Ajout de SchemaRepository pour récupérer les schémas depuis la base de données

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/collection")
    public ResponseEntity<Map<String, Object>> generatePostmanCollection() {
        Map<String, Object> collection = new HashMap<>();
        collection.put("info", createInfo());
        collection.put("item", createItems());
        collection.put("variable", createEnvironmentVariables());
        return ResponseEntity.ok(collection);
    }

    private Map<String, Object> createInfo() {
        return Map.of(
                "name", "Banking App API",
                "description", "API pour la gestion des comptes bancaires et des transactions.",
                "schema", "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
        );
    }

    private List<Map<String, Object>> createItems() {
        return apiRepository.findAll().stream().map(this::createApiItem).collect(Collectors.toList());
    }

    private Map<String, Object> createApiItem(Api api) {
        Map<String, Object> apiItem = new HashMap<>();
        apiItem.put("name", api.getPath());

        List<Map<String, Object>> requests = new ArrayList<>();
        Map<String, Object> request = new HashMap<>();
        request.put("name", api.getDescription());
        request.put("request", createRequest(api));
        request.put("response", createResponses(api));
        requests.add(request);

        apiItem.put("item", requests);
        return apiItem;
    }

    private Map<String, Object> createRequest(Api api) {
        Map<String, Object> request = new HashMap<>();
        request.put("method", api.getMethod());
        request.put("header", List.of(createHeader("Accept", "application/json")));
        request.put("url", createUrl(api));

        if (api.getRequest_body() != null && !api.getRequest_body().isEmpty()) {
            request.put("body", createRequestBody(api));
        }
        return request;
    }

    private Map<String, Object> createUrl(Api api) {
        String rawUrl = "{{baseUrl}}" + transformPathVariables(api.getPath());
        List<String> pathSegments = Arrays.asList(rawUrl.replace("{{baseUrl}}/", "").split("/"));

        Map<String, Object> url = new HashMap<>();
        url.put("raw", rawUrl);
        url.put("host", List.of("{{baseUrl}}"));
        url.put("path", pathSegments);
        url.put("variable", createPathVariables(api));
        url.put("query", createQueryParameters(api));
        return url;
    }

    private String transformPathVariables(String path) {
        return path.replaceAll("\\{(.*?)\\}", ":$1");
    }

    private List<Map<String, Object>> createPathVariables(Api api) {
        return apiParametersRepository.findByApiId(api.getId()).stream()
                .filter(param -> "path".equals(param.getTypein())) // Sélectionne uniquement les path variables
                .map(param -> Map.of(
                        "key", (Object) param.getName(),
                        "value", (Object) ("<" + (param.getData_type() != null ? param.getData_type() : "string") + ">"),
                        "description", (Object) param.getDescription()
                ))
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> createQueryParameters(Api api) {
        return apiParametersRepository.findByApiId(api.getId()).stream()
                .filter(param -> "query".equals(param.getTypein())) // Sélectionne uniquement les query parameters
                .map(param -> Map.of(
                        "key", param.getName(),
                        "value", (Object) param.getExample(),
                        "description", param.getDescription()
                ))
                .collect(Collectors.toList());
    }

    private Map<String, Object> createRequestBody(Api api) {
        return Map.of(
                "mode", "raw",
                "raw", transformJsonSchemaToSimplifiedFormat(api.getRequest_body()),
                "options", Map.of("raw", Map.of("language", "json"))
        );
    }

    private List<Map<String, Object>> createResponses(Api api) {
        return apiResponseRepository.findByApiId(api.getId()).stream()
                .map(this::createResponse)
                .collect(Collectors.toList());
    }

    private Map<String, Object> createResponse(ApiResponse apiResponse) {
        String responseBody = "{}"; // Valeur par défaut si aucun schéma n'est défini

        if (apiResponse.getSchema() != null && apiResponse.getSchema().getSchemas() != null) {
            responseBody = transformJsonSchemaToSimplifiedFormat(apiResponse.getSchema().getSchemas());
        }

        return Map.of(
                "name", apiResponse.getDescription(),
                "status", apiResponse.getStatus(),
                "code", Integer.parseInt(apiResponse.getStatus().split(" ")[0]),
                "header", List.of(createHeader("Content-Type", "application/json")),
                "body", responseBody
        );
    }

    private String transformJsonSchemaToSimplifiedFormat(String jsonSchema) {
        try {
            JsonNode schemaNode = objectMapper.readTree(jsonSchema);
            JsonNode propertiesNode = schemaNode.get("properties");

            if (propertiesNode != null) {
                return objectMapper.writeValueAsString(processJsonNode(propertiesNode));
            }

            return "{}";
        } catch (Exception e) {
            return "{}";
        }
    }

    private ObjectNode processJsonNode(JsonNode node) {
        ObjectNode result = objectMapper.createObjectNode();

        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> {
                String fieldName = entry.getKey();
                JsonNode fieldValue = entry.getValue();

                // Si le champ est "id", ne garder que le type sans format
                if ("id".equals(fieldName)) {
                    if (fieldValue.isObject()) {
                        JsonNode typeNode = fieldValue.get("type");
                        if (typeNode != null) {
                            result.put(fieldName, typeNode.asText()); // Conserver uniquement le type
                        }
                    }
                } else {
                    // Si le champ est un objet imbriqué, le traiter récursivement
                    if (fieldValue.isObject()) {
                        if (fieldValue.has("properties")) {
                            // Si le champ contient un schéma imbriqué avec des propriétés, traiter son contenu
                            JsonNode propertiesNode = fieldValue.get("properties");
                            result.set(fieldName, processJsonNode(propertiesNode));
                        } else {
                            // Sinon, garder le champ en tant qu'objet générique
                            result.put(fieldName, "<object>");
                        }
                    } else if (fieldValue.isTextual()) {
                        result.put(fieldName, "<string>");
                    } else if (fieldValue.isNumber()) {
                        result.put(fieldName, "<number>");
                    } else if (fieldValue.isBoolean()) {
                        result.put(fieldName, "<boolean>");
                    } else if (fieldValue.isInt()) {
                        result.put(fieldName, "<integer>");
                    } else if (fieldValue.isArray()) {
                        result.put(fieldName, "<array>");
                    } else if (fieldValue.isNull()) {
                        result.put(fieldName, "<null>");
                    }
                }
            });
        }
        return result;
    }

    private List<Map<String, String>> createEnvironmentVariables() {
        return List.of(Map.of("key", "baseUrl", "value", "http://localhost:8080", "description", "URL de base de l'API"));
    }

    private Map<String, String> createHeader(String key, String value) {
        return Map.of("key", key, "value", value);
    }

    // Méthode pour récupérer le schéma complet par référence
    private JsonNode getSchemaByRef(String ref) {
        // Cette méthode doit interroger votre base de données pour obtenir le schéma complet référencé par $ref
        // Utilisation de SchemaRepository pour récupérer le schéma depuis la base de données
        List<Schema> schemas = schemaRepository.findByName(ref);

        if (!schemas.isEmpty()) {
            String schemaContent = schemas.get(0).getSchemas(); // Prendre le premier schéma trouvé
            try {
                return objectMapper.readTree(schemaContent);
            } catch (Exception e) {
                return null; // Retourne null si une erreur de parsing survient
            }
        }

        return null; // Retourne null si le schéma n'est pas trouvé
    }

}*/
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.example.api_tierces.model.Api;
import com.example.api_tierces.model.ApiResponse;
import com.example.api_tierces.model.Schema;
import com.example.api_tierces.repository.ApiParametersRepository;
import com.example.api_tierces.repository.ApiRepository;
import com.example.api_tierces.repository.ApiResponseRepository;
import com.example.api_tierces.repository.SchemaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/*@RestController
@RequestMapping("/postman")
public class PostmanCollectionController {

    @Autowired
    private ApiRepository apiRepository;

    @Autowired
    private ApiParametersRepository apiParametersRepository;

    @Autowired
    private ApiResponseRepository apiResponseRepository;

    @Autowired
    private SchemaRepository schemaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/collection")
    public ResponseEntity<Map<String, Object>> generatePostmanCollection() {
        Map<String, Object> collection = new HashMap<>();
        collection.put("info", createInfo());
        collection.put("item", createItems());
        collection.put("variable", createEnvironmentVariables());
        return ResponseEntity.ok(collection);
    }

    private Map<String, Object> createInfo() {
        return Map.of(
                "name", "Banking App API",
                "description", "API pour la gestion des comptes bancaires et des transactions.",
                "schema", "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
        );
    }

    private List<Map<String, Object>> createItems() {
        return apiRepository.findAll().stream().map(this::createApiItem).collect(Collectors.toList());
    }

    private Map<String, Object> createApiItem(Api api) {
        Map<String, Object> apiItem = new HashMap<>();
        apiItem.put("name", api.getPath());

        List<Map<String, Object>> requests = new ArrayList<>();
        Map<String, Object> request = new HashMap<>();
        request.put("name", api.getDescription());
        request.put("request", createRequest(api));
        request.put("response", createResponses(api));
        requests.add(request);

        apiItem.put("item", requests);
        return apiItem;
    }

    private Map<String, Object> createRequest(Api api) {
        Map<String, Object> request = new HashMap<>();
        request.put("method", api.getMethod());
        request.put("header", List.of(createHeader("Accept", "application/json")));
        request.put("url", createUrl(api));

        if (api.getRequest_body() != null && !api.getRequest_body().isEmpty()) {
            request.put("body", createRequestBody(api));
        }
        return request;
    }

    private Map<String, Object> createUrl(Api api) {
        String rawUrl = "{{baseUrl}}" + transformPathVariables(api.getPath());
        List<String> pathSegments = Arrays.asList(rawUrl.replace("{{baseUrl}}/", "").split("/"));

        Map<String, Object> url = new HashMap<>();
        url.put("raw", rawUrl);
        url.put("host", List.of("{{baseUrl}}"));
        url.put("path", pathSegments);
        url.put("variable", createPathVariables(api));
        url.put("query", createQueryParameters(api));
        return url;
    }

    private String transformPathVariables(String path) {
        return path.replaceAll("\\{(.*?)\\}", ":$1");
    }

    private List<Map<String, Object>> createPathVariables(Api api) {
        return apiParametersRepository.findByApiId(api.getId()).stream()
                .filter(param -> "path".equals(param.getTypein()))
                .map(param -> Map.of(
                        "key", (Object) param.getName(),
                        "value", (Object) ("<" + (param.getData_type() != null ? param.getData_type() : "string") + ">"),
                        "description", (Object) param.getDescription()
                ))
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> createQueryParameters(Api api) {
        return apiParametersRepository.findByApiId(api.getId()).stream()
                .filter(param -> "query".equals(param.getTypein()))
                .map(param -> Map.of(
                        "key", param.getName(),
                        "value", (Object) param.getExample(),
                        "description", param.getDescription()
                ))
                .collect(Collectors.toList());
    }

    private Map<String, Object> createRequestBody(Api api) {
        Map<String, Object> body = new HashMap<>();
        body.put("mode", "raw");
        String rawBody = api.getRequest_body();

        try {
            JsonNode requestBodyNode = objectMapper.readTree(api.getRequest_body());
            rawBody = transformJsonSchemaToSimplifiedFormat(requestBodyNode);
        } catch (Exception e) {
            System.err.println("Error processing request body: " + e.getMessage());
        }

        body.put("raw", rawBody);
        Map<String, Object> options = new HashMap<>();
        Map<String, Object> rawOptions = new HashMap<>();
        rawOptions.put("language", "json");
        options.put("raw", rawOptions);
        body.put("options", options);

        return body;
    }

    private List<Map<String, Object>> createResponses(Api api) {
        return apiResponseRepository.findByApiId(api.getId()).stream()
                .map(this::createResponse)
                .collect(Collectors.toList());
    }

    private Map<String, Object> createResponse(ApiResponse apiResponse) {
        Map<String, Object> response = new HashMap<>();
        response.put("name", apiResponse.getDescription());
        response.put("status", apiResponse.getStatus());
        response.put("code", Integer.parseInt(apiResponse.getStatus().split(" ")[0]));
        response.put("header", List.of(createHeader("Content-Type", "application/json")));

        String responseBody = "{}";
        if (apiResponse.getSchema() != null) {
            try {
                JsonNode schemaNode = objectMapper.readTree(apiResponse.getSchema().getSchemas());
                responseBody = transformJsonSchemaToSimplifiedFormat(schemaNode);
            } catch (Exception e) {
                System.err.println("Error processing response schema: " + e.getMessage());
                responseBody = "{}";
            }
        }

        response.put("body", responseBody);
        return response;
    }

    private String transformJsonSchemaToSimplifiedFormat(JsonNode schemaNode) {
        try {
            if (schemaNode.has("$ref")) {
                JsonNode resolvedSchema = resolveRef(schemaNode.get("$ref").asText());
                if (resolvedSchema != null) {
                    return transformJsonSchemaToSimplifiedFormat(resolvedSchema);  // Recursive call
                } else {
                    return "<object>"; // or some other indicator if the ref couldn't be resolved
                }
            } else if (schemaNode.has("properties")) {
                return objectMapper.writeValueAsString(processJsonNode(schemaNode.get("properties")));
            } else {
                return "{}";
            }
        } catch (Exception e) {
            return "{}";
        }
    }


   private ObjectNode processJsonNode(JsonNode node) {
       ObjectNode result = objectMapper.createObjectNode();

       if (node.isObject()) {
           node.fields().forEachRemaining(entry -> {
               String fieldName = entry.getKey();
               JsonNode fieldValue = entry.getValue();
               String type = null;

               if (fieldValue.has("type")) {
                   type = fieldValue.get("type").asText();
               } else if (fieldValue.has("$ref")) {
                   JsonNode resolvedSchema = resolveRef(fieldValue.get("$ref").asText());
                   if (resolvedSchema != null && resolvedSchema.has("properties")) {
                       result.set(fieldName, processJsonNode(resolvedSchema.get("properties"))); // Process the properties
                       return;
                   } else {
                       result.put(fieldName, "<object>");
                       return;
                   }
               }

               if (type != null) {
                   switch (type) {
                       case "string":
                           result.put(fieldName, "<string>");
                           break;
                       case "number":
                           result.put(fieldName, "<number>");
                           break;
                       case "integer":
                           result.put(fieldName, "<integer>");
                           break;
                       case "long":
                           result.put(fieldName, "<long>");
                           break;
                       case "boolean":
                           result.put(fieldName, "<boolean>");
                           break;
                       case "array":
                           result.put(fieldName, "<array>");
                           break;
                       case "date-time":
                           result.put(fieldName, "<dateTime>");
                           break;
                       case "object":
                           if(fieldValue.has("properties")) {
                               result.set(fieldName, processJsonNode(fieldValue.get("properties"))); // Recursively process nested object
                           } else {
                               result.put(fieldName, "<object>");
                           }
                           break;
                       default:
                           result.put(fieldName, "<object>");
                           break;
                   }
               } else {
                   result.put(fieldName, "<object>"); // Default if no type found
               }
           });
       }

       return result;
   }

    private List<Map<String, String>> createEnvironmentVariables() {
        return List.of(Map.of("key", "baseUrl", "value", "http://localhost:8080", "description", "URL de base de l'API"));
    }

    private Map<String, String> createHeader(String key, String value) {
        return Map.of("key", key, "value", value);
    }

    private JsonNode resolveRef(String ref) {
        // Extract schema name from ref (assuming ref is like "#/components/schemas/SchemaName")
        String[] parts = ref.split("/");
        String schemaName = parts[parts.length - 1];

        List<Schema> schemas = schemaRepository.findByName(schemaName);
        if (!schemas.isEmpty()) {
            try {
                return objectMapper.readTree(schemas.get(0).getSchemas());
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                System.err.println("Error processing JSON from schema " + schemaName + ": " + e.getMessage());
                return null; // Or handle the error as appropriate for your application
            } catch (java.io.IOException e) {
                System.err.println("IO Error reading JSON from schema " + schemaName + ": " + e.getMessage());
                return null; // Or handle the error as appropriate for your application
            }
        }
        return null;
    }

}*/
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.example.api_tierces.model.Api;
import com.example.api_tierces.model.ApiResponse;
import com.example.api_tierces.model.Schema;
import com.example.api_tierces.repository.ApiParametersRepository;
import com.example.api_tierces.repository.ApiRepository;
import com.example.api_tierces.repository.ApiResponseRepository;
import com.example.api_tierces.repository.SchemaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


/*@RestController
@RequestMapping("/postman")
public class PostmanCollectionController {

    @Autowired
    private ApiRepository apiRepository;

    @Autowired
    private ApiParametersRepository apiParametersRepository;

    @Autowired
    private ApiResponseRepository apiResponseRepository;

    @Autowired
    private SchemaRepository schemaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/collection")
    public ResponseEntity<Map<String, Object>> generatePostmanCollection() {
        Map<String, Object> collection = new HashMap<>();
        collection.put("info", createInfo());
        collection.put("item", createItems());
        collection.put("variable", createEnvironmentVariables());
        return ResponseEntity.ok(collection);
    }

    private Map<String, Object> createInfo() {
        return Map.of(
                "name", "Banking App API",
                "description", "API pour la gestion des comptes bancaires et des transactions.",
                "schema", "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
        );
    }

    private List<Map<String, Object>> createItems() {
        return apiRepository.findAll().stream().map(this::createApiItem).collect(Collectors.toList());
    }

    private Map<String, Object> createApiItem(Api api) {
        Map<String, Object> apiItem = new HashMap<>();
        apiItem.put("name", api.getPath());

        List<Map<String, Object>> requests = new ArrayList<>();
        Map<String, Object> request = new HashMap<>();
        request.put("name", api.getDescription());
        request.put("request", createRequest(api));
        request.put("response", createResponses(api));
        requests.add(request);

        apiItem.put("item", requests);
        return apiItem;
    }

    private Map<String, Object> createRequest(Api api) {
        Map<String, Object> request = new HashMap<>();
        request.put("method", api.getMethod());
        request.put("header", List.of(createHeader("Accept", "application/json")));
        request.put("url", createUrl(api));

        if (api.getRequest_body() != null && !api.getRequest_body().isEmpty()) {
            request.put("body", createRequestBody(api));
        }
        return request;
    }

    private Map<String, Object> createUrl(Api api) {
        String rawUrl = "{{baseUrl}}" + transformPathVariables(api.getPath());
        List<String> pathSegments = Arrays.asList(rawUrl.replace("{{baseUrl}}/", "").split("/"));

        Map<String, Object> url = new HashMap<>();
        url.put("raw", rawUrl);
        url.put("host", List.of("{{baseUrl}}"));
        url.put("path", pathSegments);
        url.put("variable", createPathVariables(api));
        url.put("query", createQueryParameters(api));
        return url;
    }

    private String transformPathVariables(String path) {
        return path.replaceAll("\\{(.*?)\\}", ":$1");
    }

    private List<Map<String, Object>> createPathVariables(Api api) {
        return apiParametersRepository.findByApiId(api.getId()).stream()
                .filter(param -> "path".equals(param.getTypein()))
                .map(param -> Map.of(
                        "key", (Object) param.getName(),
                        "value", (Object) ("<" + (param.getData_type() != null ? param.getData_type() : "string") + ">"),
                        "description", (Object) param.getDescription()
                ))
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> createQueryParameters(Api api) {
        return apiParametersRepository.findByApiId(api.getId()).stream()
                .filter(param -> "query".equals(param.getTypein()))
                .map(param -> Map.of(
                        "key", param.getName(),
                        "value", (Object) param.getExample(),
                        "description", param.getDescription()
                ))
                .collect(Collectors.toList());
    }

    private Map<String, Object> createRequestBody(Api api) {
        Map<String, Object> body = new HashMap<>();
        body.put("mode", "raw");

        try {
            JsonNode requestBodyNode = objectMapper.readTree(api.getRequest_body());
            String rawBody = transformJsonSchemaToSimplifiedFormat(requestBodyNode, true);
            body.put("raw", rawBody); // Put transformed raw body

        } catch (Exception e) {
            System.err.println("Error processing request body: " + e.getMessage());
            body.put("raw", api.getRequest_body()); // Fallback
        }

        Map<String, Object> options = new HashMap<>();
        Map<String, Object> rawOptions = new HashMap<>();
        rawOptions.put("language", "json");
        options.put("raw", rawOptions);
        body.put("options", options);

        return body;
    }

    private List<Map<String, Object>> createResponses(Api api) {
        return apiResponseRepository.findByApiId(api.getId()).stream()
                .map(this::createResponse)
                .collect(Collectors.toList());
    }

    private Map<String, Object> createResponse(ApiResponse apiResponse) {
        Map<String, Object> response = new HashMap<>();
        response.put("name", apiResponse.getDescription());

        // Set the status text using getPostmanStatusText()
        int statusCode = Integer.parseInt(apiResponse.getStatus().split(" ")[0]);
        String statusText = getPostmanStatusText(statusCode);
        response.put("status", statusText);

        //Put the status code in the code
        response.put("code", statusCode);
        response.put("header", List.of(createHeader("Content-Type", "application/json")));

        String responseBody = "{}";
        if (apiResponse.getSchema() != null) {
            try {
                JsonNode schemaNode = objectMapper.readTree(apiResponse.getSchema().getSchemas());
                responseBody = transformJsonSchemaToSimplifiedFormat(schemaNode, true);
            } catch (Exception e) {
                System.err.println("Error processing response schema: " + e.getMessage());
                responseBody = "{}";
            }
        }

        response.put("body", responseBody);
        return response;
    }

    private String transformJsonSchemaToSimplifiedFormat(JsonNode schemaNode, boolean prettyPrint) {
        try {
            if (schemaNode.has("$ref")) {
                JsonNode resolvedSchema = resolveRef(schemaNode.get("$ref").asText());
                if (resolvedSchema != null) {
                    return transformJsonSchemaToSimplifiedFormat(resolvedSchema, prettyPrint);  // Recursive call
                } else {
                    return "<object>"; // or some other indicator if the ref couldn't be resolved
                }
            } else if (schemaNode.has("properties")) {
                ObjectNode processedNode = processJsonNode(schemaNode.get("properties"));
                if (prettyPrint) {
                    return prettyPrintJson(processedNode);
                } else {
                    return objectMapper.writeValueAsString(processedNode);
                }
            } else {
                return "{}";
            }
        } catch (Exception e) {
            return "{}";
        }
    }
    private String prettyPrintJson(JsonNode jsonNode) throws JsonProcessingException {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
    }

    private ObjectNode processJsonNode(JsonNode node) {
        ObjectNode result = objectMapper.createObjectNode();

        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> {
                String fieldName = entry.getKey();
                JsonNode fieldValue = entry.getValue();
                String type = null;

                if (fieldValue.has("type")) {
                    type = fieldValue.get("type").asText();
                } else if (fieldValue.has("$ref")) {
                    JsonNode resolvedSchema = resolveRef(fieldValue.get("$ref").asText());
                    if (resolvedSchema != null && resolvedSchema.has("properties")) {
                        result.set(fieldName, processJsonNode(resolvedSchema.get("properties"))); // Process the properties
                        return;
                    } else {
                        result.put(fieldName, "<object>");
                        return;
                    }
                }

                if (type != null) {
                    switch (type) {
                        case "string":
                            result.put(fieldName, "<string>");
                            break;
                        case "number":
                            result.put(fieldName, "<number>");
                            break;
                        case "integer":
                            result.put(fieldName, "<long>"); // Force integer to long
                            break;
                        case "long":
                            result.put(fieldName, "<long>");
                            break;
                        case "boolean":
                            result.put(fieldName, "<boolean>");
                            break;
                        case "array":
                            result.put(fieldName, "<array>");
                            break;
                        case "date-time":
                            result.put(fieldName, "<dateTime>");
                            break;
                        case "object":
                            if (fieldValue.has("properties")) {
                                result.set(fieldName, processJsonNode(fieldValue.get("properties"))); // Recursively process nested object
                            } else {
                                result.put(fieldName, "<object>");
                            }
                            break;
                        default:
                            result.put(fieldName, "<object>");
                            break;
                    }
                } else {
                    result.put(fieldName, "<object>"); // Default if no type found
                }
            });
        }

        return result;
    }
    private String getPostmanStatusText(int statusCode) {
        switch (statusCode) {
            case 200:
                return "OK";
            case 201:
                return "Created";
            case 204:
                return "No Content";
            case 400:
                return "Bad Request";
            case 401:
                return "Unauthorized";
            case 403:
                return "Forbidden";
            case 404:
                return "Not Found";
            case 500:
                return "Internal Server Error";
            default:
                return "Unknown Status";
        }
    }

    private List<Map<String, String>> createEnvironmentVariables() {
        return List.of(Map.of("key", "baseUrl", "value", "http://localhost:8080", "description", "URL de base de l'API"));
    }

    private Map<String, String> createHeader(String key, String value) {
        return Map.of("key", key, "value", value);
    }

    private JsonNode resolveRef(String ref) {
        // Extract schema name from ref (assuming ref is like "#/components/schemas/SchemaName")
        String[] parts = ref.split("/");
        String schemaName = parts[parts.length - 1];

        List<Schema> schemas = schemaRepository.findByName(schemaName);
        if (!schemas.isEmpty()) {
            try {
                return objectMapper.readTree(schemas.get(0).getSchemas());
            } catch (JsonProcessingException e) {
                System.err.println("Error processing JSON from schema " + schemaName + ": " + e.getMessage());
                return null; // Or handle the error as appropriate for your application
            } catch (IOException e) {
                System.err.println("IO Error reading JSON from schema " + schemaName + ": " + e.getMessage());
                return null; // Or handle the error as appropriate for your application
            }
        }
        return null;
    }
    // l codeeeee hedhaaaaaa cvnnnnn , juste nsalah partie request body bech mayhezech m3ah partie schema ref
}*/
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.example.api_tierces.model.Api;
import com.example.api_tierces.model.ApiResponse;
import com.example.api_tierces.model.Schema;
import com.example.api_tierces.repository.ApiParametersRepository;
import com.example.api_tierces.repository.ApiRepository;
import com.example.api_tierces.repository.ApiResponseRepository;
import com.example.api_tierces.repository.SchemaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/*@RestController
@RequestMapping("/postman")
public class PostmanCollectionController {

    @Autowired
    private ApiRepository apiRepository;

    @Autowired
    private ApiParametersRepository apiParametersRepository;

    @Autowired
    private ApiResponseRepository apiResponseRepository;

    @Autowired
    private SchemaRepository schemaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/collection")
    public ResponseEntity<Map<String, Object>> generatePostmanCollection() {
        Map<String, Object> collection = new HashMap<>();
        collection.put("info", createInfo());
        collection.put("item", createItems());
        collection.put("variable", createEnvironmentVariables());
        return ResponseEntity.ok(collection);
    }

    private Map<String, Object> createInfo() {
        return Map.of(
                "name", "Banking App API",
                "description", "API pour la gestion des comptes bancaires et des transactions.",
                "schema", "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
        );
    }

    private List<Map<String, Object>> createItems() {
        return apiRepository.findAll().stream().map(this::createApiItem).collect(Collectors.toList());
    }

    private Map<String, Object> createApiItem(Api api) {
        Map<String, Object> apiItem = new HashMap<>();
        apiItem.put("name", api.getPath());

        List<Map<String, Object>> requests = new ArrayList<>();
        Map<String, Object> request = new HashMap<>();
        request.put("name", api.getDescription());
        request.put("request", createRequest(api));
        request.put("response", createResponses(api));
        requests.add(request);

        apiItem.put("item", requests);
        return apiItem;
    }

    private Map<String, Object> createRequest(Api api) {
        Map<String, Object> request = new HashMap<>();
        request.put("method", api.getMethod());
        request.put("header", List.of(createHeader("Accept", "application/json")));
        request.put("url", createUrl(api));

        if (api.getRequest_body() != null && !api.getRequest_body().isEmpty()) {
            request.put("body", createRequestBody(api));
        }
        return request;
    }

    private Map<String, Object> createUrl(Api api) {
        String rawUrl = "{{baseUrl}}" + transformPathVariables(api.getPath());
        List<String> pathSegments = Arrays.asList(rawUrl.replace("{{baseUrl}}/", "").split("/"));

        Map<String, Object> url = new HashMap<>();
        url.put("raw", rawUrl);
        url.put("host", List.of("{{baseUrl}}"));
        url.put("path", pathSegments);
        url.put("variable", createPathVariables(api));
        url.put("query", createQueryParameters(api));
        return url;
    }

    private String transformPathVariables(String path) {
        return path.replaceAll("\\{(.*?)\\}", ":$1");
    }

    private List<Map<String, Object>> createPathVariables(Api api) {
        return apiParametersRepository.findByApiId(api.getId()).stream()
                .filter(param -> "path".equals(param.getTypein()))
                .map(param -> {
                    String dataType = param.getData_type() != null ? param.getData_type() : "string";
                    if ("integer".equals(dataType)) {
                        dataType = "long"; // Change integer to long
                    }
                    String finalDataType = dataType;
                    return Map.of(
                            "key", (Object) param.getName(),
                            "value", (Object) ("<" + finalDataType + ">"),
                            "description", (Object) param.getDescription()
                    );
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> createQueryParameters(Api api) {
        return apiParametersRepository.findByApiId(api.getId()).stream()
                .filter(param -> "query".equals(param.getTypein()))
                .map(param -> Map.of(
                        "key", param.getName(),
                        "value", (Object) param.getExample(),
                        "description", param.getDescription()
                ))
                .collect(Collectors.toList());
    }

    private Map<String, Object> createRequestBody(Api api) {
        Map<String, Object> body = new HashMap<>();
        body.put("mode", "raw");

        try {
            JsonNode requestBodyNode = objectMapper.readTree(api.getRequest_body());
            String rawBody = transformJsonSchemaToSimplifiedFormat(requestBodyNode, true);
            body.put("raw", rawBody); // Put transformed raw body

        } catch (Exception e) {
            System.err.println("Error processing request body: " + e.getMessage());
            body.put("raw", api.getRequest_body()); // Fallback
        }

        Map<String, Object> options = new HashMap<>();
        Map<String, Object> rawOptions = new HashMap<>();
        rawOptions.put("language", "json");
        options.put("raw", rawOptions);
        body.put("options", options);

        return body;
    }

    private List<Map<String, Object>> createResponses(Api api) {
        return apiResponseRepository.findByApiId(api.getId()).stream()
                .map(this::createResponse)
                .collect(Collectors.toList());
    }

    private Map<String, Object> createResponse(ApiResponse apiResponse) {
        Map<String, Object> response = new HashMap<>();
        response.put("name", apiResponse.getDescription());

        // Set the status text using getPostmanStatusText()
        int statusCode = Integer.parseInt(apiResponse.getStatus().split(" ")[0]);
        String statusText = getPostmanStatusText(statusCode);
        response.put("status", statusText);

        //Put the status code in the code
        response.put("code", statusCode);
        response.put("header", List.of(createHeader("Content-Type", "application/json")));

        String responseBody = "{}";
        if (apiResponse.getSchema() != null) {
            try {
                JsonNode schemaNode = objectMapper.readTree(apiResponse.getSchema().getSchemas());
                responseBody = transformJsonSchemaToSimplifiedFormat(schemaNode, true);
            } catch (Exception e) {
                System.err.println("Error processing response schema: " + e.getMessage());
                responseBody = "{}";
            }
        }

        response.put("body", responseBody);
        return response;
    }

    private String transformJsonSchemaToSimplifiedFormat(JsonNode schemaNode, boolean prettyPrint) {
        try {
            if (schemaNode.has("$ref")) {
                JsonNode resolvedSchema = resolveRef(schemaNode.get("$ref").asText());
                if (resolvedSchema != null) {
                    return transformJsonSchemaToSimplifiedFormat(resolvedSchema, prettyPrint);  // Recursive call
                } else {
                    return "<object>"; // or some other indicator if the ref couldn't be resolved
                }
            } else if (schemaNode.has("properties")) {
                ObjectNode processedNode = processJsonNode(schemaNode.get("properties"));
                if (prettyPrint) {
                    return prettyPrintJson(processedNode);
                } else {
                    return objectMapper.writeValueAsString(processedNode);
                }
            } else {
                return "{}";
            }
        } catch (Exception e) {
            return "{}";
        }
    }
    private String prettyPrintJson(JsonNode jsonNode) throws JsonProcessingException {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
    }

    private ObjectNode processJsonNode(JsonNode node) {
        ObjectNode result = objectMapper.createObjectNode();

        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> {
                String fieldName = entry.getKey();
                JsonNode fieldValue = entry.getValue();
                String type = null;

                if (fieldValue.has("type")) {
                    type = fieldValue.get("type").asText();
                } else if (fieldValue.has("$ref")) {
                    JsonNode resolvedSchema = resolveRef(fieldValue.get("$ref").asText());
                    if (resolvedSchema != null && resolvedSchema.has("properties")) {
                        result.set(fieldName, processJsonNode(resolvedSchema.get("properties"))); // Process the properties
                        return;
                    } else {
                        result.put(fieldName, "<object>");
                        return;
                    }
                }

                if (type != null) {
                    switch (type) {
                        case "string":
                            result.put(fieldName, "<string>");
                            break;
                        case "number":
                            result.put(fieldName, "<number>");
                            break;
                        case "integer":
                            result.put(fieldName, "<long>"); // Force integer to long
                            break;
                        case "long":
                            result.put(fieldName, "<long>");
                            break;
                        case "boolean":
                            result.put(fieldName, "<boolean>");
                            break;
                        case "array":
                            result.put(fieldName, "<array>");
                            break;
                        case "date-time":
                            result.put(fieldName, "<dateTime>");
                            break;
                        case "object":
                            if (fieldValue.has("properties")) {
                                result.set(fieldName, processJsonNode(fieldValue.get("properties"))); // Recursively process nested object
                            } else {
                                result.put(fieldName, "<object>");
                            }
                            break;
                        default:
                            result.put(fieldName, "<object>");
                            break;
                    }
                } else {
                    result.put(fieldName, "<object>"); // Default if no type found
                }
            });
        }

        return result;
    }
    private String getPostmanStatusText(int statusCode) {
        switch (statusCode) {
            case 200:
                return "OK";
            case 201:
                return "Created";
            case 204:
                return "No Content";
            case 400:
                return "Bad Request";
            case 401:
                return "Unauthorized";
            case 403:
                return "Forbidden";
            case 404:
                return "Not Found";
            case 500:
                return "Internal Server Error";
            default:
                return "Unknown Status";
        }
    }

    private List<Map<String, String>> createEnvironmentVariables() {
        return List.of(Map.of("key", "baseUrl", "value", "http://localhost:8080", "description", "URL de base de l'API"));
    }

    private Map<String, String> createHeader(String key, String value) {
        return Map.of("key", key, "value", value);
    }

    private JsonNode resolveRef(String ref) {
        // Extract schema name from ref (assuming ref is like "#/components/schemas/SchemaName")
        String[] parts = ref.split("/");
        String schemaName = parts[parts.length - 1];

        List<Schema> schemas = schemaRepository.findByName(schemaName);
        if (!schemas.isEmpty()) {
            try {
                return objectMapper.readTree(schemas.get(0).getSchemas());
            } catch (JsonProcessingException e) {
                System.err.println("Error processing JSON from schema " + schemaName + ": " + e.getMessage());
                return null; // Or handle the error as appropriate for your application
            } catch (IOException e) {
                System.err.println("IO Error reading JSON from schema " + schemaName + ": " + e.getMessage());
                return null; // Or handle the error as appropriate for your application
            }
        }
        return null;
    }
}*/
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.example.api_tierces.model.Api;
import com.example.api_tierces.model.ApiResponse;
import com.example.api_tierces.model.Schema;
import com.example.api_tierces.repository.ApiParametersRepository;
import com.example.api_tierces.repository.ApiRepository;
import com.example.api_tierces.repository.ApiResponseRepository;
import com.example.api_tierces.repository.SchemaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
//hedhaaaaaa l code compleetttt finaleeeeee juste fama mochkla lazemni nsalahha fel banka bech ta9bel ba5lef depot w retrait fih zeda fazet date lezem ne5edh l format nhotha date-time
/*@RestController
@RequestMapping("/postman")
public class PostmanCollectionController {

    @Autowired
    private ApiRepository apiRepository;

    @Autowired
    private ApiParametersRepository apiParametersRepository;

    @Autowired
    private ApiResponseRepository apiResponseRepository;

    @Autowired
    private SchemaRepository schemaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/collection")
    public ResponseEntity<Map<String, Object>> generatePostmanCollection() {
        Map<String, Object> collection = new HashMap<>();
        collection.put("info", createInfo());
        collection.put("item", createItems());
        collection.put("variable", createEnvironmentVariables());
        return ResponseEntity.ok(collection);
    }

    private Map<String, Object> createInfo() {
        return Map.of(
                "name", "Banking App API",
                "description", "API pour la gestion des comptes bancaires et des transactions.",
                "schema", "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
        );
    }

    private List<Map<String, Object>> createItems() {
        return apiRepository.findAll().stream().map(this::createApiItem).collect(Collectors.toList());
    }

    private Map<String, Object> createApiItem(Api api) {
        Map<String, Object> apiItem = new HashMap<>();
        apiItem.put("name", api.getPath());

        List<Map<String, Object>> requests = new ArrayList<>();
        Map<String, Object> request = new HashMap<>();
        request.put("name", api.getDescription());
        request.put("request", createRequest(api));
        request.put("response", createResponses(api));
        requests.add(request);

        apiItem.put("item", requests);
        return apiItem;
    }

    private Map<String, Object> createRequest(Api api) {
        Map<String, Object> request = new HashMap<>();
        request.put("method", api.getMethod());
        request.put("header", List.of(createHeader("Accept", "application/json")));
        request.put("url", createUrl(api));

        if (api.getRequest_body() != null && !api.getRequest_body().isEmpty()) {
            request.put("body", createRequestBody(api));
        }
        return request;
    }

    private Map<String, Object> createUrl(Api api) {
        String rawUrl = "{{baseUrl}}" + transformPathVariables(api.getPath());
        List<String> pathSegments = Arrays.asList(rawUrl.replace("{{baseUrl}}/", "").split("/"));

        Map<String, Object> url = new HashMap<>();
        url.put("raw", rawUrl);
        url.put("host", List.of("{{baseUrl}}"));
        url.put("path", pathSegments);
        url.put("variable", createPathVariables(api));
        url.put("query", createQueryParameters(api));
        return url;
    }

    private String transformPathVariables(String path) {
        return path.replaceAll("\\{(.*?)\\}", ":$1");
    }

    private List<Map<String, Object>> createPathVariables(Api api) {
        return apiParametersRepository.findByApiId(api.getId()).stream()
                .filter(param -> "path".equals(param.getTypein()))
                .map(param -> {
                    String dataType = param.getData_type() != null ? param.getData_type() : "string";
                    if ("integer".equals(dataType)) {
                        dataType = "long"; // Change integer to long
                    }
                    String finalDataType = dataType;
                    return Map.of(
                            "key", (Object) param.getName(),
                            "value", (Object) ("<" + finalDataType + ">"),
                            "description", (Object) param.getDescription()
                    );
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> createQueryParameters(Api api) {
        return apiParametersRepository.findByApiId(api.getId()).stream()
                .filter(param -> "query".equals(param.getTypein()))
                .map(param -> Map.of(
                        "key", param.getName(),
                        "value", (Object) param.getExample(),
                        "description", param.getDescription()
                ))
                .collect(Collectors.toList());
    }

    private Map<String, Object> createRequestBody(Api api) {
        Map<String, Object> body = new HashMap<>();
        body.put("mode", "raw");

        try {
            JsonNode requestBodyNode = objectMapper.readTree(api.getRequest_body());
            String rawBody = transformJsonSchemaToSimplifiedFormatForRequestBody(requestBodyNode, true);
            body.put("raw", rawBody); // Put transformed raw body

        } catch (Exception e) {
            System.err.println("Error processing request body: " + e.getMessage());
            body.put("raw", api.getRequest_body()); // Fallback
        }

        Map<String, Object> options = new HashMap<>();
        Map<String, Object> rawOptions = new HashMap<>();
        rawOptions.put("language", "json");
        options.put("raw", rawOptions);
        body.put("options", options);

        return body;
    }

    private List<Map<String, Object>> createResponses(Api api) {
        return apiResponseRepository.findByApiId(api.getId()).stream()
                .map(this::createResponse)
                .collect(Collectors.toList());
    }

    private Map<String, Object> createResponse(ApiResponse apiResponse) {
        Map<String, Object> response = new HashMap<>();
        response.put("name", apiResponse.getDescription());

        // Set the status text using getPostmanStatusText()
        int statusCode = Integer.parseInt(apiResponse.getStatus().split(" ")[0]);
        String statusText = getPostmanStatusText(statusCode);
        response.put("status", statusText);

        //Put the status code in the code
        response.put("code", statusCode);
        response.put("header", List.of(createHeader("Content-Type", "application/json")));

        String responseBody = "{}";
        if (apiResponse.getSchema() != null) {
            try {
                JsonNode schemaNode = objectMapper.readTree(apiResponse.getSchema().getSchemas());
                responseBody = transformJsonSchemaToSimplifiedFormat(schemaNode, true);
            } catch (Exception e) {
                System.err.println("Error processing response schema: " + e.getMessage());
                responseBody = "{}";
            }
        }

        response.put("body", responseBody);
        return response;
    }

    // Separate method for request body transformation
    private String transformJsonSchemaToSimplifiedFormatForRequestBody(JsonNode schemaNode, boolean prettyPrint) {
        try {
            if (schemaNode.has("properties")) {
                ObjectNode processedNode = processJsonNodeForRequestBody(schemaNode.get("properties"));
                if (prettyPrint) {
                    return prettyPrintJson(processedNode);
                } else {
                    return objectMapper.writeValueAsString(processedNode);
                }
            } else {
                return "{}";
            }
        } catch (Exception e) {
            return "{}";
        }
    }

    private String transformJsonSchemaToSimplifiedFormat(JsonNode schemaNode, boolean prettyPrint) {
        try {
            if (schemaNode.has("$ref")) {
                JsonNode resolvedSchema = resolveRef(schemaNode.get("$ref").asText());
                if (resolvedSchema != null) {
                    return transformJsonSchemaToSimplifiedFormat(resolvedSchema, prettyPrint);  // Recursive call
                } else {
                    return "<object>"; // or some other indicator if the ref couldn't be resolved
                }
            } else if (schemaNode.has("properties")) {
                ObjectNode processedNode = processJsonNode(schemaNode.get("properties"));
                if (prettyPrint) {
                    return prettyPrintJson(processedNode);
                } else {
                    return objectMapper.writeValueAsString(processedNode);
                }
            } else {
                return "{}";
            }
        } catch (Exception e) {
            return "{}";
        }
    }
    private String prettyPrintJson(JsonNode jsonNode) throws JsonProcessingException {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
    }

    private ObjectNode processJsonNodeForRequestBody(JsonNode node) {
        ObjectNode result = objectMapper.createObjectNode();

        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> {
                String fieldName = entry.getKey();
                JsonNode fieldValue = entry.getValue();
                String type = null;

                if (fieldValue.has("type")) {
                    type = fieldValue.get("type").asText();
                } else if (fieldValue.has("$ref")) {
                    // Ignore $ref for request body
                    return;
                }

                if (type != null) {
                    switch (type) {
                        case "string":
                            result.put(fieldName, "<string>");
                            break;
                        case "number":
                            result.put(fieldName, "<number>");
                            break;
                        case "integer":
                            result.put(fieldName, "<long>"); // Force integer to long
                            break;
                        case "long":
                            result.put(fieldName, "<long>");
                            break;
                        case "boolean":
                            result.put(fieldName, "<boolean>");
                            break;
                        case "array":
                            result.put(fieldName, "<array>");
                            break;
                        case "date-time":
                            result.put(fieldName, "<dateTime>");
                            break;
                        case "object":
                            if (fieldValue.has("properties")) {
                                result.set(fieldName, processJsonNodeForRequestBody(fieldValue.get("properties"))); // Recursively process nested object
                            } else {
                                result.put(fieldName, "<object>");
                            }
                            break;
                        default:
                            result.put(fieldName, "<object>");
                            break;
                    }
                } else {
                    result.put(fieldName, "<object>"); // Default if no type found
                }
            });
        }

        return result;
    }


    private ObjectNode processJsonNode(JsonNode node) {
        ObjectNode result = objectMapper.createObjectNode();

        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> {
                String fieldName = entry.getKey();
                JsonNode fieldValue = entry.getValue();
                String type = null;

                if (fieldValue.has("type")) {
                    type = fieldValue.get("type").asText();
                } else if (fieldValue.has("$ref")) {
                    JsonNode resolvedSchema = resolveRef(fieldValue.get("$ref").asText());
                    if (resolvedSchema != null && resolvedSchema.has("properties")) {
                        result.set(fieldName, processJsonNode(resolvedSchema.get("properties"))); // Process the properties
                        return;
                    } else {
                        result.put(fieldName, "<object>");
                        return;
                    }
                }

                if (type != null) {
                    switch (type) {
                        case "string":
                            result.put(fieldName, "<string>");
                            break;
                        case "number":
                            result.put(fieldName, "<number>");
                            break;
                        case "integer":
                            result.put(fieldName, "<long>"); // Force integer to long
                            break;
                        case "long":
                            result.put(fieldName, "<long>");
                            break;
                        case "boolean":
                            result.put(fieldName, "<boolean>");
                            break;
                        case "array":
                            result.put(fieldName, "<array>");
                            break;
                        case "date-time":
                            result.put(fieldName, "<dateTime>");
                            break;
                        case "object":
                            if (fieldValue.has("properties")) {
                                result.set(fieldName, processJsonNode(fieldValue.get("properties"))); // Recursively process nested object
                            } else {
                                result.put(fieldName, "<object>");
                            }
                            break;
                        default:
                            result.put(fieldName, "<object>");
                            break;
                    }
                } else {
                    result.put(fieldName, "<object>"); // Default if no type found
                }
            });
        }

        return result;
    }
    private String getPostmanStatusText(int statusCode) {
        switch (statusCode) {
            case 200:
                return "OK";
            case 201:
                return "Created";
            case 204:
                return "No Content";
            case 400:
                return "Bad Request";
            case 401:
                return "Unauthorized";
            case 403:
                return "Forbidden";
            case 404:
                return "Not Found";
            case 500:
                return "Internal Server Error";
            default:
                return "Unknown Status";
        }
    }

    private List<Map<String, String>> createEnvironmentVariables() {
        return List.of(Map.of("key", "baseUrl", "value", "http://localhost:8084", "description", "URL de base de l'API"));
    }

    private Map<String, String> createHeader(String key, String value) {
        return Map.of("key", key, "value", value);
    }

    private JsonNode resolveRef(String ref) {
        // Extract schema name from ref (assuming ref is like "#/components/schemas/SchemaName")
        String[] parts = ref.split("/");
        String schemaName = parts[parts.length - 1];

        List<Schema> schemas = schemaRepository.findByName(schemaName);
        if (!schemas.isEmpty()) {
            try {
                return objectMapper.readTree(schemas.get(0).getSchemas());
            } catch (JsonProcessingException e) {
                System.err.println("Error processing JSON from schema " + schemaName + ": " + e.getMessage());
                return null; // Or handle the error as appropriate for your application
            } catch (IOException e) {
                System.err.println("IO Error reading JSON from schema " + schemaName + ": " + e.getMessage());
                return null; // Or handle the error as appropriate for your application
            }
        }
        return null;
    }
}*/
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.example.api_tierces.model.Api;
import com.example.api_tierces.model.ApiResponse;
import com.example.api_tierces.model.Schema;
import com.example.api_tierces.repository.ApiParametersRepository;
import com.example.api_tierces.repository.ApiRepository;
import com.example.api_tierces.repository.ApiResponseRepository;
import com.example.api_tierces.repository.SchemaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
//hedhaaaa l codeeee cv belgdeeeee
/*@RestController
@RequestMapping("/postman")
public class PostmanCollectionController {

    @Autowired
    private ApiRepository apiRepository;

    @Autowired
    private ApiParametersRepository apiParametersRepository;

    @Autowired
    private ApiResponseRepository apiResponseRepository;

    @Autowired
    private SchemaRepository schemaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/collection")
    public ResponseEntity<Map<String, Object>> generatePostmanCollection() {
        Map<String, Object> collection = new HashMap<>();
        collection.put("info", createInfo());
        collection.put("item", createItems());
        collection.put("variable", createEnvironmentVariables());
        return ResponseEntity.ok(collection);
    }

    private Map<String, Object> createInfo() {
        return Map.of(
                "name", "Banking App API",
                "description", "API pour la gestion des comptes bancaires et des transactions.",
                "schema", "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
        );
    }

    private List<Map<String, Object>> createItems() {
        return apiRepository.findAll().stream().map(this::createApiItem).collect(Collectors.toList());
    }

    private Map<String, Object> createApiItem(Api api) {
        Map<String, Object> apiItem = new HashMap<>();
        apiItem.put("name", api.getPath());

        List<Map<String, Object>> requests = new ArrayList<>();
        Map<String, Object> request = new HashMap<>();
        request.put("name", api.getDescription());
        request.put("request", createRequest(api));
        request.put("response", createResponses(api));
        requests.add(request);

        apiItem.put("item", requests);
        return apiItem;
    }

    private Map<String, Object> createRequest(Api api) {
        Map<String, Object> request = new HashMap<>();
        request.put("method", api.getMethod());
        request.put("header", List.of(createHeader("Accept", "application/json")));
        request.put("url", createUrl(api));

        if (api.getRequest_body() != null && !api.getRequest_body().isEmpty()) {
            request.put("body", createRequestBody(api));
        }
        return request;
    }

    private Map<String, Object> createUrl(Api api) {
        String rawUrl = "{{baseUrl}}" + transformPathVariables(api.getPath());
        List<String> pathSegments = Arrays.asList(rawUrl.replace("{{baseUrl}}/", "").split("/"));

        Map<String, Object> url = new HashMap<>();
        url.put("raw", rawUrl);
        url.put("host", List.of("{{baseUrl}}"));
        url.put("path", pathSegments);
        url.put("variable", createPathVariables(api));
        url.put("query", createQueryParameters(api));
        return url;
    }

    private String transformPathVariables(String path) {
        return path.replaceAll("\\{(.*?)\\}", ":$1");
    }

    private List<Map<String, Object>> createPathVariables(Api api) {
        return apiParametersRepository.findByApiId(api.getId()).stream()
                .filter(param -> "path".equals(param.getTypein()))
                .map(param -> {
                    String dataType = param.getData_type() != null ? param.getData_type() : "string";
                    if ("integer".equals(dataType)) {
                        dataType = "long"; // Change integer to long
                    }
                    String finalDataType = dataType;
                    return Map.of(
                            "key", (Object) param.getName(),
                            "value", (Object) ("<" + finalDataType + ">"),
                            "description", (Object) param.getDescription()
                    );
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> createQueryParameters(Api api) {
        return apiParametersRepository.findByApiId(api.getId()).stream()
                .filter(param -> "query".equals(param.getTypein()))
                .map(param -> Map.of(
                        "key", param.getName(),
                        "value", (Object) param.getExample(),
                        "description", param.getDescription()
                ))
                .collect(Collectors.toList());
    }

    private Map<String, Object> createRequestBody(Api api) {
        Map<String, Object> body = new HashMap<>();
        body.put("mode", "raw");

        try {
            JsonNode requestBodyNode = objectMapper.readTree(api.getRequest_body());
            String rawBody = transformJsonSchemaToSimplifiedFormatForRequestBody(requestBodyNode, true);
            body.put("raw", rawBody); // Put transformed raw body

        } catch (Exception e) {
            System.err.println("Error processing request body: " + e.getMessage());
            body.put("raw", api.getRequest_body()); // Fallback
        }

        Map<String, Object> options = new HashMap<>();
        Map<String, Object> rawOptions = new HashMap<>();
        rawOptions.put("language", "json");
        options.put("raw", rawOptions);
        body.put("options", options);

        return body;
    }

    private List<Map<String, Object>> createResponses(Api api) {
        return apiResponseRepository.findByApiId(api.getId()).stream()
                .map(this::createResponse)
                .collect(Collectors.toList());
    }

    private Map<String, Object> createResponse(ApiResponse apiResponse) {
        Map<String, Object> response = new HashMap<>();
        response.put("name", apiResponse.getDescription());

        // Set the status text using getPostmanStatusText()
        int statusCode = Integer.parseInt(apiResponse.getStatus().split(" ")[0]);
        String statusText = getPostmanStatusText(statusCode);
        response.put("status", statusText);

        //Put the status code in the code
        response.put("code", statusCode);
        response.put("header", List.of(createHeader("Content-Type", "application/json")));

        String responseBody = "{}";
        if (apiResponse.getSchema() != null) {
            try {
                JsonNode schemaNode = objectMapper.readTree(apiResponse.getSchema().getSchemas());
                responseBody = transformJsonSchemaToSimplifiedFormat(schemaNode, true);
            } catch (Exception e) {
                System.err.println("Error processing response schema: " + e.getMessage());
                responseBody = "{}";
            }
        }

        response.put("body", responseBody);
        return response;
    }

    // Separate method for request body transformation
    private String transformJsonSchemaToSimplifiedFormatForRequestBody(JsonNode schemaNode, boolean prettyPrint) {
        try {
            if (schemaNode.has("properties")) {
                ObjectNode processedNode = processJsonNodeForRequestBody(schemaNode.get("properties"));
                if (prettyPrint) {
                    return prettyPrintJson(processedNode);
                } else {
                    return objectMapper.writeValueAsString(processedNode);
                }
            } else {
                return "{}";
            }
        } catch (Exception e) {
            return "{}";
        }
    }

    private String transformJsonSchemaToSimplifiedFormat(JsonNode schemaNode, boolean prettyPrint) {
        try {
            if (schemaNode.has("$ref")) {
                JsonNode resolvedSchema = resolveRef(schemaNode.get("$ref").asText());
                if (resolvedSchema != null) {
                    return transformJsonSchemaToSimplifiedFormat(resolvedSchema, prettyPrint);  // Recursive call
                } else {
                    return "<object>"; // or some other indicator if the ref couldn't be resolved
                }
            } else if (schemaNode.has("properties")) {
                ObjectNode processedNode = processJsonNode(schemaNode.get("properties"));
                if (prettyPrint) {
                    return prettyPrintJson(processedNode);
                } else {
                    return objectMapper.writeValueAsString(processedNode);
                }
            } else {
                return "{}";
            }
        } catch (Exception e) {
            return "{}";
        }
    }
    private String prettyPrintJson(JsonNode jsonNode) throws JsonProcessingException {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
    }

    private ObjectNode processJsonNodeForRequestBody(JsonNode node) {
        ObjectNode result = objectMapper.createObjectNode();

        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> {
                String fieldName = entry.getKey();
                JsonNode fieldValue = entry.getValue();
                String type = null;
                String format = null;

                if (fieldValue.has("type")) {
                    type = fieldValue.get("type").asText();
                }

                if (fieldValue.has("format")) {
                    format = fieldValue.get("format").asText();
                }

                if (fieldValue.has("$ref")) {
                    // Ignore $ref for request body
                    return;
                }

                if (type != null) {
                    switch (type) {
                        case "string":
                            if ("date-time".equals(format)) {
                                result.put(fieldName, "<dateTime>");
                            } else {
                                result.put(fieldName, "<string>");
                            }
                            break;
                        case "number":
                            result.put(fieldName, "<number>");
                            break;
                        case "integer":
                            result.put(fieldName, "<long>"); // Force integer to long
                            break;
                        case "long":
                            result.put(fieldName, "<long>");
                            break;
                        case "boolean":
                            result.put(fieldName, "<boolean>");
                            break;
                        case "array":
                            result.put(fieldName, "<array>");
                            break;
                        case "object":
                            if (fieldValue.has("properties")) {
                                result.set(fieldName, processJsonNodeForRequestBody(fieldValue.get("properties"))); // Recursively process nested object
                            } else {
                                result.put(fieldName, "<object>");
                            }
                            break;
                        default:
                            result.put(fieldName, "<object>");
                            break;
                    }
                } else {
                    result.put(fieldName, "<object>"); // Default if no type found
                }
            });
        }

        return result;
    }


    private ObjectNode processJsonNode(JsonNode node) {
        ObjectNode result = objectMapper.createObjectNode();

        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> {
                String fieldName = entry.getKey();
                JsonNode fieldValue = entry.getValue();
                String type = null;
                String format = null;

                if (fieldValue.has("type")) {
                    type = fieldValue.get("type").asText();
                }

                if (fieldValue.has("format")) {
                    format = fieldValue.get("format").asText();
                }

                if (fieldValue.has("$ref")) {
                    JsonNode resolvedSchema = resolveRef(fieldValue.get("$ref").asText());
                    if (resolvedSchema != null && resolvedSchema.has("properties")) {
                        result.set(fieldName, processJsonNode(resolvedSchema.get("properties"))); // Process the properties
                        return;
                    } else {
                        result.put(fieldName, "<object>");
                        return;
                    }
                }

                if (type != null) {
                    switch (type) {
                        case "string":
                            if ("date-time".equals(format)) {
                                result.put(fieldName, "<dateTime>");
                            } else {
                                result.put(fieldName, "<string>");
                            }
                            break;
                        case "number":
                            result.put(fieldName, "<number>");
                            break;
                        case "integer":
                            result.put(fieldName, "<long>"); // Force integer to long
                            break;
                        case "long":
                            result.put(fieldName, "<long>");
                            break;
                        case "boolean":
                            result.put(fieldName, "<boolean>");
                            break;
                        case "array":
                            result.put(fieldName, "<array>");
                            break;

                        case "object":
                            if (fieldValue.has("properties")) {
                                result.set(fieldName, processJsonNode(fieldValue.get("properties"))); // Recursively process nested object
                            } else {
                                result.put(fieldName, "<object>");
                            }
                            break;
                        default:
                            result.put(fieldName, "<object>");
                            break;
                    }
                } else {
                    result.put(fieldName, "<object>"); // Default if no type found
                }
            });
        }

        return result;
    }
    private String getPostmanStatusText(int statusCode) {
        switch (statusCode) {
            case 200:
                return "OK";
            case 201:
                return "Created";
            case 204:
                return "No Content";
            case 400:
                return "Bad Request";
            case 401:
                return "Unauthorized";
            case 403:
                return "Forbidden";
            case 404:
                return "Not Found";
            case 500:
                return "Internal Server Error";
            default:
                return "Unknown Status";
        }
    }

    private List<Map<String, String>> createEnvironmentVariables() {
        return List.of(Map.of("key", "baseUrl", "value", "http://localhost:8084", "description", "URL de base de l'API"));
    }

    private Map<String, String> createHeader(String key, String value) {
        return Map.of("key", key, "value", value);
    }

    private JsonNode resolveRef(String ref) {
        // Extract schema name from ref (assuming ref is like "#/components/schemas/SchemaName")
        String[] parts = ref.split("/");
        String schemaName = parts[parts.length - 1];

        List<Schema> schemas = schemaRepository.findByName(schemaName);
        if (!schemas.isEmpty()) {
            try {
                return objectMapper.readTree(schemas.get(0).getSchemas());
            } catch (JsonProcessingException e) {
                System.err.println("Error processing JSON from schema " + schemaName + ": " + e.getMessage());
                return null; // Or handle the error as appropriate for your application
            } catch (IOException e) {
                System.err.println("IO Error reading JSON from schema " + schemaName + ": " + e.getMessage());
                return null; // Or handle the error as appropriate for your application
            }
        }
        return null;
    }
}*/


@Tag(name = "A-Postman-Collection , Création d'une Collection Postman")
@RestController
@RequestMapping("/postman")
public class PostmanCollectionController {

    @Autowired
    private ApiRepository apiRepository;

    @Autowired
    private ApiParametersRepository apiParametersRepository;

    @Autowired
    private ApiResponseRepository apiResponseRepository;

    @Autowired
    private SchemaRepository schemaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PostmanProcessingService postmanProcessingService;

    @Operation(summary = "Création d'une collection Postman", description = "Création d'une collection Postman a partir des données stockés dans les tables")
    @GetMapping("/collection")
    //@Scheduled(cron = "0 0 * * * ?")
    public ResponseEntity<String> generateAndProcessPostmanCollection() {
        Map<String, Object> collection = new HashMap<>();
        collection.put("info", createInfo());
        collection.put("item", createItems());
        collection.put("variable", createEnvironmentVariables());

        try {
            String postmanCollectionJson = objectMapper.writeValueAsString(collection);
            String processingResult = postmanProcessingService.processPostmanCollection(postmanCollectionJson);
            return ResponseEntity.ok(processingResult);
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la génération de la collection Postman : " + e.getMessage());
        }
    }

    // Les méthodes suivantes sont identiques à celles du premier code fourni
    private Map<String, Object> createInfo() {
        return Map.of(
                "name", "Banking App API",
                "description", "API pour la gestion des comptes bancaires et des transactions.",
                "schema", "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
        );
    }

    private List<Map<String, Object>> createItems() {
        return apiRepository.findAll().stream().map(this::createApiItem).collect(Collectors.toList());
    }

    private Map<String, Object> createApiItem(Api api) {
        Map<String, Object> apiItem = new HashMap<>();
        apiItem.put("name", api.getPath());

        List<Map<String, Object>> requests = new ArrayList<>();
        Map<String, Object> request = new HashMap<>();
        request.put("name", api.getDescription());
        request.put("request", createRequest(api));
        request.put("response", createResponses(api));
        requests.add(request);

        apiItem.put("item", requests);
        return apiItem;
    }

    private Map<String, Object> createRequest(Api api) {
        Map<String, Object> request = new HashMap<>();
        request.put("method", api.getMethod());
        request.put("header", List.of(createHeader("Accept", "application/json")));
        request.put("url", createUrl(api));

        if (api.getRequest_body() != null && !api.getRequest_body().isEmpty()) {
            request.put("body", createRequestBody(api));
        }
        return request;
    }

    private Map<String, Object> createUrl(Api api) {
        String rawUrl = "{{baseUrl}}" + transformPathVariables(api.getPath());
        List<String> pathSegments = Arrays.asList(rawUrl.replace("{{baseUrl}}/", "").split("/"));

        Map<String, Object> url = new HashMap<>();
        url.put("raw", rawUrl);
        url.put("host", List.of("{{baseUrl}}"));
        url.put("path", pathSegments);
        url.put("variable", createPathVariables(api));
        url.put("query", createQueryParameters(api));
        return url;
    }

    private String transformPathVariables(String path) {
        return path.replaceAll("\\{(.*?)\\}", ":$1");
    }


    private List<Map<String, Object>> createPathVariables(Api api) {
        return apiParametersRepository.findByApiId(api.getId()).stream()
                .filter(param -> "path".equals(param.getTypein()))
                .map(param -> {
                    if (param == null) {
                        // Handle the null parameter case
                        System.err.println("Null parameter encountered for API ID: " + api.getId());
                        return Collections.emptyMap(); // Return an empty map to avoid NullPointerException
                    }

                    String name = param.getName();
                    String description = param.getDescription();
                    String dataType = param.getData_type() != null ? param.getData_type() : "string";
                    if ("integer".equals(dataType)) {
                        dataType = "long"; // Change integer to long
                    }
                    String finalDataType = dataType;

                    if (name == null) {
                        System.err.println("Null name for parameter: " + param);
                        return Collections.emptyMap();
                    }

                    // Use a mutable HashMap with explicit String key type
                    Map<String, Object> paramMap = new HashMap<>();
                    paramMap.put("key", name);
                    paramMap.put("value", "<" + finalDataType + ">");

                    // Add the description only if it's not null
                    if (description != null) {
                        paramMap.put("description", description);
                    }

                    return paramMap;
                })
                .filter(map -> !map.isEmpty()) // Remove empty maps from the list
                .map(map -> (Map<String, Object>) map) // Explicitly cast the Map
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> createQueryParameters(Api api) {
        return apiParametersRepository.findByApiId(api.getId()).stream()
                .filter(param -> "query".equals(param.getTypein()))
                .map(param -> Map.of(
                        "key", param.getName(),
                        "value", (Object) param.getExample(),
                        "description", param.getDescription()
                ))
                .collect(Collectors.toList());
    }

    private Map<String, Object> createRequestBody(Api api) {
        Map<String, Object> body = new HashMap<>();
        body.put("mode", "raw");

        try {
            JsonNode requestBodyNode = objectMapper.readTree(api.getRequest_body());
            String rawBody = transformJsonSchemaToSimplifiedFormatForRequestBody(requestBodyNode, true);
            body.put("raw", rawBody); // Put transformed raw body

        } catch (Exception e) {
            System.err.println("Error processing request body: " + e.getMessage());
            body.put("raw", api.getRequest_body()); // Fallback
        }

        Map<String, Object> options = new HashMap<>();
        Map<String, Object> rawOptions = new HashMap<>();
        rawOptions.put("language", "json");
        options.put("raw", rawOptions);
        body.put("options", options);

        return body;
    }

    private List<Map<String, Object>> createResponses(Api api) {
        return apiResponseRepository.findByApiId(api.getId()).stream()
                .map(this::createResponse)
                .collect(Collectors.toList());
    }

    private Map<String, Object> createResponse(ApiResponse apiResponse) {
        Map<String, Object> response = new HashMap<>();
        response.put("name", apiResponse.getDescription());

        // Set the status text using getPostmanStatusText()
        int statusCode = Integer.parseInt(apiResponse.getStatus().split(" ")[0]);
        String statusText = getPostmanStatusText(statusCode);
        response.put("status", statusText);

        //Put the status code in the code
        response.put("code", statusCode);
        response.put("header", List.of(createHeader("Content-Type", "application/json")));

        String responseBody = "{}";
        if (apiResponse.getSchema() != null) {
            try {
                JsonNode schemaNode = objectMapper.readTree(apiResponse.getSchema().getSchemas());
                responseBody = transformJsonSchemaToSimplifiedFormat(schemaNode, true);
            } catch (Exception e) {
                System.err.println("Error processing response schema: " + e.getMessage());
                responseBody = "{}";
            }
        }

        response.put("body", responseBody);
        return response;
    }

    // Separate method for request body transformation
    private String transformJsonSchemaToSimplifiedFormatForRequestBody(JsonNode schemaNode, boolean prettyPrint) {
        try {
            if (schemaNode.has("properties")) {
                ObjectNode processedNode = processJsonNodeForRequestBody(schemaNode.get("properties"));
                if (prettyPrint) {
                    return prettyPrintJson(processedNode);
                } else {
                    return objectMapper.writeValueAsString(processedNode);
                }
            } else {
                return "{}";
            }
        } catch (Exception e) {
            return "{}";
        }
    }

    private String transformJsonSchemaToSimplifiedFormat(JsonNode schemaNode, boolean prettyPrint) {
        try {
            if (schemaNode.has("$ref")) {
                JsonNode resolvedSchema = resolveRef(schemaNode.get("$ref").asText());
                if (resolvedSchema != null) {
                    return transformJsonSchemaToSimplifiedFormat(resolvedSchema, prettyPrint);  // Recursive call
                } else {
                    return "<object>"; // or some other indicator if the ref couldn't be resolved
                }
            } else if (schemaNode.has("properties")) {
                ObjectNode processedNode = processJsonNode(schemaNode.get("properties"));
                if (prettyPrint) {
                    return prettyPrintJson(processedNode);
                } else {
                    return objectMapper.writeValueAsString(processedNode);
                }
            } else {
                return "{}";
            }
        } catch (Exception e) {
            return "{}";
        }
    }
    private String prettyPrintJson(JsonNode jsonNode) throws JsonProcessingException {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
    }

    private ObjectNode processJsonNodeForRequestBody(JsonNode node) {
        ObjectNode result = objectMapper.createObjectNode();

        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> {
                String fieldName = entry.getKey();
                JsonNode fieldValue = entry.getValue();
                String type = null;
                String format = null;

                if (fieldValue.has("type")) {
                    type = fieldValue.get("type").asText();
                }

                if (fieldValue.has("format")) {
                    format = fieldValue.get("format").asText();
                }

                if (fieldValue.has("$ref")) {
                    // Ignore $ref for request body
                    return;
                }

                if (type != null) {
                    switch (type) {
                        case "string":
                            if ("date-time".equals(format)) {
                                result.put(fieldName, "<dateTime>");
                            } else {
                                result.put(fieldName, "<string>");
                            }
                            break;
                        case "number":
                            result.put(fieldName, "<number>");
                            break;
                        case "integer":
                            result.put(fieldName, "<long>"); // Force integer to long
                            break;
                        case "long":
                            result.put(fieldName, "<long>");
                            break;
                        case "boolean":
                            result.put(fieldName, "<boolean>");
                            break;
                        case "array":
                            result.put(fieldName, "<array>");
                            break;
                        case "object":
                            if (fieldValue.has("properties")) {
                                result.set(fieldName, processJsonNodeForRequestBody(fieldValue.get("properties"))); // Recursively process nested object
                            } else {
                                result.put(fieldName, "<object>");
                            }
                            break;
                        default:
                            result.put(fieldName, "<object>");
                            break;
                    }
                } else {
                    result.put(fieldName, "<object>"); // Default if no type found
                }
            });
        }

        return result;
    }


    private ObjectNode processJsonNode(JsonNode node) {
        ObjectNode result = objectMapper.createObjectNode();

        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> {
                String fieldName = entry.getKey();
                JsonNode fieldValue = entry.getValue();
                String type = null;
                String format = null;

                if (fieldValue.has("type")) {
                    type = fieldValue.get("type").asText();
                }

                if (fieldValue.has("format")) {
                    format = fieldValue.get("format").asText();
                }

                if (fieldValue.has("$ref")) {
                    JsonNode resolvedSchema = resolveRef(fieldValue.get("$ref").asText());
                    if (resolvedSchema != null && resolvedSchema.has("properties")) {
                        result.set(fieldName, processJsonNode(resolvedSchema.get("properties"))); // Process the properties
                        return;
                    } else {
                        result.put(fieldName, "<object>");
                        return;
                    }
                }

                if (type != null) {
                    switch (type) {
                        case "string":
                            if ("date-time".equals(format)) {
                                result.put(fieldName, "<dateTime>");
                            } else {
                                result.put(fieldName, "<string>");
                            }
                            break;
                        case "number":
                            result.put(fieldName, "<number>");
                            break;
                        case "integer":
                            result.put(fieldName, "<long>"); // Force integer to long
                            break;
                        case "long":
                            result.put(fieldName, "<long>");
                            break;
                        case "boolean":
                            result.put(fieldName, "<boolean>");
                            break;
                        case "array":
                            result.put(fieldName, "<array>");
                            break;

                        case "object":
                            if (fieldValue.has("properties")) {
                                result.set(fieldName, processJsonNode(fieldValue.get("properties"))); // Recursively process nested object
                            } else {
                                result.put(fieldName, "<object>");
                            }
                            break;
                        default:
                            result.put(fieldName, "<object>");
                            break;
                    }
                } else {
                    result.put(fieldName, "<object>"); // Default if no type found
                }
            });
        }

        return result;
    }
    private String getPostmanStatusText(int statusCode) {
        switch (statusCode) {
            case 200:
                return "OK";
            case 201:
                return "Created";
            case 204:
                return "No Content";
            case 400:
                return "Bad Request";
            case 401:
                return "Unauthorized";
            case 403:
                return "Forbidden";
            case 404:
                return "Not Found";
            case 500:
                return "Internal Server Error";
            default:
                return "Unknown Status";
        }
    }

    private List<Map<String, String>> createEnvironmentVariables() {
        return List.of(Map.of("key", "baseUrl", "value", "http://localhost:8084", "description", "URL de base de l'API"));
    }

    private Map<String, String> createHeader(String key, String value) {
        return Map.of("key", key, "value", value);
    }

    private JsonNode resolveRef(String ref) {
        // Extract schema name from ref (assuming ref is like "#/components/schemas/SchemaName")
        String[] parts = ref.split("/");
        String schemaName = parts[parts.length - 1];

        List<Schema> schemas = schemaRepository.findByName(schemaName);
        if (!schemas.isEmpty()) {
            try {
                return objectMapper.readTree(schemas.get(0).getSchemas());
            } catch (JsonProcessingException e) {
                System.err.println("Error processing JSON from schema " + schemaName + ": " + e.getMessage());
                return null; // Or handle the error as appropriate for your application
            } catch (IOException e) {
                System.err.println("IO Error reading JSON from schema " + schemaName + ": " + e.getMessage());
                return null; // Or handle the error as appropriate for your application
            }
        }
        return null;
    }
}
