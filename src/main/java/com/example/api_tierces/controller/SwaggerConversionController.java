package com.example.api_tierces.controller;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType; // Attention: MediaType de swagger, pas spring
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
// Utilisation de com.fasterxml.jackson pour générer des exemples JSON à partir de Schema si nécessaire
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.*; // Pour List, Map, Set, Collections, etc.
import java.util.stream.Collectors;
import java.util.TreeMap; // Pour

import com.example.api_tierces.service.PostmanProcessingService;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class SwaggerConversionController {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    @Autowired
    private PostmanProcessingService postmanProcessingService;

    // --- Méthode Principale ---
    @PostMapping(value = "/convert-swagger-to-postman", consumes = "multipart/form-data")
    public ResponseEntity<Resource> convertSwaggerToPostman(@RequestParam("swaggerFile") MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(createErrorResource("Aucun fichier n'a été fourni."));
        }

        OpenAPI openAPI = null;
        try {
            String swaggerContent = new String(file.getBytes(), StandardCharsets.UTF_8);
            openAPI = new OpenAPIV3Parser().readContents(swaggerContent, null, null).getOpenAPI();

            if (openAPI == null) {
                return ResponseEntity.badRequest().body(createErrorResource("Le contenu du fichier fourni n'est pas un document OpenAPI valide ou n'a pas pu être parsé."));
            }

            JSONObject postmanCollection = new JSONObject();
            JSONObject info = new JSONObject();
            info.put("name", openAPI.getInfo() != null && openAPI.getInfo().getTitle() != null ? openAPI.getInfo().getTitle() : "Generated Collection");
            info.put("description", openAPI.getInfo() != null && openAPI.getInfo().getDescription() != null ? openAPI.getInfo().getDescription() : "");
            info.put("schema", "https://schema.getpostman.com/json/collection/v2.1.0/collection.json");
            info.put("_postman_id", UUID.randomUUID().toString());
            postmanCollection.put("info", info);

            JSONArray collectionVariables = new JSONArray();
            String baseUrlVariableKey = "baseUrl";
            String baseUrlVariablePostman = "{{" + baseUrlVariableKey + "}}";

            // Extraire l'URL de base à partir du fichier Swagger
            String defaultBaseUrl = "http://localhost:8080"; // Valeur par défaut

            if (openAPI.getServers() != null && !openAPI.getServers().isEmpty()) {
                Server firstServer = openAPI.getServers().get(0); // Prendre le premier serveur
                if (firstServer.getUrl() != null && !firstServer.getUrl().isBlank()) {
                    defaultBaseUrl = firstServer.getUrl(); // Utiliser l'URL du premier serveur
                }
            }

            // Ajout de l'URL de base sous forme de variable dans la collection Postman
            collectionVariables.put(new JSONObject().put("key", baseUrlVariableKey).put("value", defaultBaseUrl).put("type", "string"));
            postmanCollection.put("variable", collectionVariables);

            JSONArray rootItems = new JSONArray();
            postmanCollection.put("item", rootItems);
            Map<String, JSONObject> folderMap = new HashMap<>();

            if (openAPI.getPaths() != null) {
                List<String> sortedPaths = new ArrayList<>(openAPI.getPaths().keySet());
                Collections.sort(sortedPaths);
                for (String path : sortedPaths) {
                    PathItem pathItem = openAPI.getPaths().get(path);
                    if (pathItem != null) {
                        processPathItem(openAPI, path, pathItem, rootItems, folderMap, baseUrlVariablePostman);
                    }
                }
            }

            // Convertir la collection en String JSON
            String postmanCollectionJson = postmanCollection.toString(2);

            // Appeler le service de post-traitement
            //postmanProcessingService.processPostmanCollection(postmanCollectionJson, openAPI);
            String processingResult = postmanProcessingService.processPostmanCollection(postmanCollectionJson);

            byte[] jsonBytes = postmanCollectionJson.getBytes(StandardCharsets.UTF_8);
            ByteArrayResource resource = new ByteArrayResource(jsonBytes);
            String originalFileName = file.getOriginalFilename();
            String baseName = originalFileName != null ? originalFileName.replaceFirst("[.][^.]+$", "") : "swagger_converted";
            baseName = baseName.replaceAll("[^a-zA-Z0-9.\\-_]+", "_");
            String postmanFileName = baseName + "_postman.json";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + postmanFileName + "\"")
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(resource);

        } catch (Exception e) {
            System.err.println("Erreur lors de la conversion du fichier Swagger: " + (file != null ? file.getOriginalFilename() : "fichier inconnu"));
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResource("Une erreur interne est survenue lors de la conversion: " + e.getMessage()));
        }
    }


private void processPathItem(OpenAPI openAPI, String openApiPath, PathItem pathItem, JSONArray rootItems, Map<String, JSONObject> folderMap, String baseUrlVariable) {
    // 1. Séparer le chemin de base de la query string potentielle
    String basePath = openApiPath;
    String queryStringFromPath = null; // Initialiser à null

    if (openApiPath.contains("?")) {
        System.err.println("WARN: Le chemin OpenAPI '" + openApiPath + "' contient un '?'. Structure non standard.");
        int queryStartIndex = openApiPath.indexOf('?');
        // Réassignation ici -> basePath et queryStringFromPath ne sont pas effectivement finaux
        basePath = openApiPath.substring(0, queryStartIndex);
        queryStringFromPath = openApiPath.substring(queryStartIndex + 1);
    }

    // --- CORRECTION ICI ---
    // Créer des variables finales (ou effectivement finales) pour la lambda
    final String finalBasePath = basePath;
    final String finalQueryStringFromPath = queryStringFromPath;
    // --- FIN CORRECTION ---

    // 2. Créer les dossiers en utilisant UNIQUEMENT basePath (maintenant finalBasePath)
    // Note: findOrCreateFolders n'utilise pas la variable dans une lambda, donc pas besoin de la passer en final ici.
    final JSONArray parentItemArray = findOrCreateFolders(finalBasePath, rootItems, folderMap);

    // 3. Traiter les opérations
    EnumMap<PathItem.HttpMethod, Operation> operations = new EnumMap<>(PathItem.HttpMethod.class);
    if(pathItem.getGet() != null) operations.put(PathItem.HttpMethod.GET, pathItem.getGet());
    if(pathItem.getPost() != null) operations.put(PathItem.HttpMethod.POST, pathItem.getPost());
    if(pathItem.getPut() != null) operations.put(PathItem.HttpMethod.PUT, pathItem.getPut());
    if(pathItem.getPatch() != null) operations.put(PathItem.HttpMethod.PATCH, pathItem.getPatch());
    if(pathItem.getDelete() != null) operations.put(PathItem.HttpMethod.DELETE, pathItem.getDelete());
    if(pathItem.getOptions() != null) operations.put(PathItem.HttpMethod.OPTIONS, pathItem.getOptions());
    if(pathItem.getHead() != null) operations.put(PathItem.HttpMethod.HEAD, pathItem.getHead());
    if(pathItem.getTrace() != null) operations.put(PathItem.HttpMethod.TRACE, pathItem.getTrace());


    // La lambda utilise maintenant les variables finales
    operations.forEach((method, operation) -> {
        // 4. Appeler createPostmanRequestItem avec les variables finales
        JSONObject postmanItem = createPostmanRequestItem(openAPI, finalBasePath, finalQueryStringFromPath, method, operation, baseUrlVariable);
        // parentItemArray est aussi effectivement final car sa référence n'est pas réassignée
        parentItemArray.put(postmanItem);
    });
}

// (findOrCreateFolders reste identique, il utilise déjà le chemin passé)
private JSONArray findOrCreateFolders(String path, JSONArray rootItems, Map<String, JSONObject> folderMap) {
    String cleanedPath = path.replaceAll("^/|/$", "");
    if (cleanedPath.isEmpty()) return rootItems;
    String[] segments = cleanedPath.split("/");
    JSONArray currentLevelItems = rootItems;
    String currentCumulativePath = "";
    for (String segment : segments) {
        if (segment.isEmpty()) continue;
        currentCumulativePath += "/" + segment;
        JSONObject folder = folderMap.get(currentCumulativePath);
        if (folder == null) {
            folder = new JSONObject().put("name", segment).put("item", new JSONArray());
            currentLevelItems.put(folder);
            folderMap.put(currentCumulativePath, folder);
        }
        currentLevelItems = folder.getJSONArray("item");
    }
    return currentLevelItems;
}


// ---- CORRECTION : Signature pour accepter basePath et queryStringFromPath ----
private JSONObject createPostmanRequestItem(OpenAPI openAPI, String basePath, String queryStringFromPath, PathItem.HttpMethod method, Operation operation, String baseUrlVariable) {
    JSONObject item = new JSONObject();
    // Utiliser basePath pour le nom fallback si nécessaire
    String itemName = operation.getSummary() != null && !operation.getSummary().isBlank() ? operation.getSummary()
            : (operation.getOperationId() != null && !operation.getOperationId().isBlank() ? operation.getOperationId()
            : method.name() + " " + basePath); // Nom basé sur basePath
    item.put("name", itemName);

    JSONObject request = new JSONObject();
    request.put("method", method.name().toUpperCase());
    // ... (description) ...
    if (operation.getDescription() != null && !operation.getDescription().isBlank()) { request.put("description", operation.getDescription()); }
    else if (operation.getSummary() != null && !operation.getSummary().isBlank()) { request.put("description", operation.getSummary()); }


    JSONArray headers = extractHeaders(operation);
    request.put("header", headers);

    JSONObject body = extractBody(openAPI, operation);
    if (body != null) { /* ... (ajout body + content-type) ... */
        request.put("body", body);
        addContentTypeHeaderFromBody(headers, body);
    }

    // ---- CORRECTION : Passer basePath ET queryStringFromPath à buildUrlObject ----
    JSONObject url = buildUrlObject(basePath, queryStringFromPath, operation, baseUrlVariable);
    request.put("url", url);

    item.put("request", request);

    // ---- CORRECTION : Passer basePath ET queryStringFromPath à extractResponses (pour originalRequest) ----
    JSONArray responses = extractResponses(openAPI, operation, basePath, queryStringFromPath, method, baseUrlVariable);
    item.put("response", responses);

    return item;
}


// ---- CORRECTION : buildUrlObject accepte basePath et queryStringFromPath ----
private JSONObject buildUrlObject(String basePath, String queryStringFromPath, Operation operation, String baseUrlVariable) {
    JSONObject url = new JSONObject();
    url.put("host", new JSONArray().put(baseUrlVariable));

    // 1. Traiter les Path Variables (UNIQUEMENT DANS basePath)
    String processedPathForRaw = basePath.replaceAll("/+$", ""); // Enlever slash final
    JSONArray pathVariablesArray = new JSONArray();
    List<String> finalPathSegmentsList = new ArrayList<>();

    String[] segments = basePath.replaceFirst("^/", "").split("/");
    for (String segment : segments) {
        if (segment.isEmpty()) continue;
        if (segment.startsWith("{") && segment.endsWith("}")) {
            String varName = segment.substring(1, segment.length() - 1);
            processedPathForRaw = processedPathForRaw.replace("{" + varName + "}", ":" + varName);
            finalPathSegmentsList.add(":" + varName);
            Parameter pathParam = findParameter(operation, varName, "path");
            String paramType = "string"; String description = "";
            if (pathParam != null) { /* ... (récupérer type/desc) ... */
                description = pathParam.getDescription() != null ? pathParam.getDescription() : "";
                if(pathParam.getSchema() != null && pathParam.getSchema().getType() != null) paramType = pathParam.getSchema().getType();
            }
            pathVariablesArray.put(new JSONObject().put("key", varName).put("value", "<" + paramType + ">").put("description", description));
        } else {
            finalPathSegmentsList.add(segment);
        }
    }
    url.put("path", new JSONArray(finalPathSegmentsList));
    if (pathVariablesArray.length() > 0) {
        url.put("variable", pathVariablesArray);
    }

    // 2. Traiter les Query Parameters : COMBINER ceux de l'operation ET ceux parsés de queryStringFromPath
    JSONArray queryParamsJsonArray = new JSONArray();
    List<Map.Entry<String, String>> queryParamsForRawList = new ArrayList<>();
    Set<String> processedQueryKeys = new HashSet<>(); // Pour éviter doublons

    // 2a. Depuis operation.getParameters()
    if (operation.getParameters() != null) {
        for (Parameter param : operation.getParameters()) {
            if ("query".equalsIgnoreCase(param.getIn())) {
                String paramKey = param.getName();
                if (processedQueryKeys.add(paramKey)) { // Ajouter seulement si pas déjà traité
                    String paramValue = getParameterExampleValue(param);
                    String paramDescription = param.getDescription() != null ? param.getDescription() : "";
                    queryParamsJsonArray.put(new JSONObject().put("key", paramKey).put("value", paramValue).put("description", paramDescription));
                    queryParamsForRawList.add(new AbstractMap.SimpleEntry<>(paramKey, paramValue));
                }
            }
        }
    }

    // 2b. Depuis queryStringFromPath (si non nul et si pas déjà traité)
    if (queryStringFromPath != null && !queryStringFromPath.isEmpty()) {
        String[] pairs = queryStringFromPath.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length > 0 && !keyValue[0].isEmpty()) {
                String key = urlDecode(keyValue[0]); // Décoder la clé
                if (processedQueryKeys.add(key)) { // Ajouter seulement si pas déjà dans operation.params
                    String value = keyValue.length > 1 ? urlDecode(keyValue[1]) : ""; // Décoder la valeur
                    // Essayer de deviner un placeholder à partir de la valeur si elle en a l'air
                    String placeholderValue = value.startsWith("{") && value.endsWith("}") ? "<" + value.substring(1, value.length()-1) + ">" : value;
                    // Créer un JSONObject pour url.query
                    queryParamsJsonArray.put(new JSONObject()
                            .put("key", key)
                            .put("value", placeholderValue) // Mettre le placeholder deviné ou la valeur
                            .put("description", "(From path)")); // Indiquer l'origine
                    // Ajouter à la liste pour url.raw
                    queryParamsForRawList.add(new AbstractMap.SimpleEntry<>(key, placeholderValue));
                }
            }
        }
    }

    // Ajouter le tableau query s'il contient des éléments
    if(queryParamsJsonArray.length() > 0) {
        url.put("query", queryParamsJsonArray);
    }

    // 3. Reconstruire l'URL 'raw' finale
    StringBuilder finalRawUrl = new StringBuilder();
    finalRawUrl.append(baseUrlVariable);
    // Ajouter le chemin (gestion du slash)
    if (!processedPathForRaw.isEmpty() && !processedPathForRaw.equals("/")) {
        if (!processedPathForRaw.startsWith("/")) { finalRawUrl.append("/"); }
        finalRawUrl.append(processedPathForRaw);
    } else if (processedPathForRaw.equals("/")) {
        finalRawUrl.append("/");
    }
    // Ajouter la query string
    if (!queryParamsForRawList.isEmpty()) {
        finalRawUrl.append("?");
        String queryString = queryParamsForRawList.stream()
                .map(entry -> urlEncode(entry.getKey()) + "=" + urlEncode(entry.getValue()))
                .collect(Collectors.joining("&"));
        finalRawUrl.append(queryString);
    }
    url.put("raw", finalRawUrl.toString());

    return url;
}

// Ajouter une méthode de décodage URL simple
private String urlDecode(String value) {
    try {
        // Utiliser le décodeur standard Java
        return java.net.URLDecoder.decode(value, StandardCharsets.UTF_8.name());
    } catch (Exception e) {
        System.err.println("WARN: Failed to URL decode: " + value);
        return value; // Retourner la valeur originale en cas d'erreur
    }
}

// ---- CORRECTION : extractResponses doit aussi recevoir basePath et queryStringFromPath ----
private JSONArray extractResponses(OpenAPI openAPI, Operation operation, String basePath, String queryStringFromPath, PathItem.HttpMethod method, String baseUrlVariable) {
    JSONArray responses = new JSONArray();
    if (operation.getResponses() == null) return responses;
    ApiResponses apiResponses = operation.getResponses();
    List<String> sortedCodes = new ArrayList<>(apiResponses.keySet());
    // ... (tri des codes) ...
    sortedCodes.sort((c1, c2) -> { if ("default".equals(c1)) return 1; if ("default".equals(c2)) return -1; try { return Integer.compare(Integer.parseInt(c1), Integer.parseInt(c2)); } catch (NumberFormatException ex) { return c1.compareTo(c2); } });

    for(String code : sortedCodes) {
        ApiResponse apiResponse = apiResponses.get(code); if (apiResponse == null) continue;
        JSONObject postmanResponse = new JSONObject();
        // ... (name, status, code, previewlanguage, headers, cookies) ...
        String responseName = apiResponse.getDescription() != null && !apiResponse.getDescription().isBlank() ? apiResponse.getDescription() : getStatusText(code);
        postmanResponse.put("name", code + ": " + responseName);
        // ---- CORRECTION : Passer basePath et queryStringFromPath à buildOriginalRequest ----
        postmanResponse.put("originalRequest", buildOriginalRequest(operation, basePath, queryStringFromPath, method, baseUrlVariable));
        postmanResponse.put("status", getStatusText(code));
        try { postmanResponse.put("code", Integer.parseInt(code.equals("default") ? "200" : code)); } catch (NumberFormatException ex) { postmanResponse.put("code", 200); System.err.println("WARN: Could not parse response code '" + code + "', using 200."); }
        postmanResponse.put("_postman_previewlanguage", findPreviewLanguage(apiResponse.getContent()));
        JSONArray responseHeaders = new JSONArray();
        if (apiResponse.getHeaders() != null) { apiResponse.getHeaders().forEach((key, header) -> { responseHeaders.put(new JSONObject().put("key", key).put("value", "").put("description", header.getDescription() != null ? header.getDescription() : "")); }); }
        addContentTypeHeaderFromContent(responseHeaders, apiResponse.getContent()); postmanResponse.put("header", responseHeaders);
        postmanResponse.put("cookie", new JSONArray());


        String responseBodyContent = "";
        if (apiResponse.getContent() != null && !apiResponse.getContent().isEmpty()) {
            MediaType mediaType = apiResponse.getContent().get("application/json"); if (mediaType == null) mediaType = apiResponse.getContent().values().iterator().next();
            responseBodyContent = getExampleBodyContent(mediaType, openAPI);
        }
        postmanResponse.put("body", responseBodyContent);
        responses.put(postmanResponse);
    }
    return responses;
}

// ---- CORRECTION : buildOriginalRequest doit aussi recevoir basePath et queryStringFromPath ----
private JSONObject buildOriginalRequest(Operation operation, String basePath, String queryStringFromPath, PathItem.HttpMethod method, String baseUrlVariable) {
    JSONObject originalReq = new JSONObject();
    originalReq.put("method", method.name().toUpperCase());
    originalReq.put("header", new JSONArray()); // Vide suffit

    JSONObject url = new JSONObject();
    // Reconstruire l'URL brute minimale pour originalRequest
    StringBuilder rawUrlMinimal = new StringBuilder(baseUrlVariable);
    String processedPathForRaw = basePath.replaceAll("/+$", "");
    if (operation.getParameters() != null) { // Appliquer les :var pour la cohérence
        for (Parameter param : operation.getParameters()) {
            if ("path".equalsIgnoreCase(param.getIn())) {
                processedPathForRaw = processedPathForRaw.replace("{" + param.getName() + "}", ":" + param.getName());
            }
        }
    }
    if (!processedPathForRaw.isEmpty() && !processedPathForRaw.equals("/")) {
        if (!processedPathForRaw.startsWith("/")) { rawUrlMinimal.append("/"); }
        rawUrlMinimal.append(processedPathForRaw);
    } else if (processedPathForRaw.equals("/")) {
        rawUrlMinimal.append("/");
    }
    // Ajouter la query string si elle vient du path original
    if (queryStringFromPath != null && !queryStringFromPath.isEmpty()) {
        rawUrlMinimal.append("?").append(queryStringFromPath); // Utiliser la version brute ici
    }
    // Ajouter aussi les query params de l'opération ? C'est redondant mais pour être sûr:
    // else if (operation.getParameters() != null) { ... reconstruire query string ... }

    url.put("raw", rawUrlMinimal.toString());
    originalReq.put("url", url);
    return originalReq;
}

// (Les autres méthodes helpers restent identiques : findParameter, urlEncode (amélioré), extractHeaders, etc...)
// Assurez-vous que les méthodes getParameterExampleValue, findAcceptHeaderValue, extractBody,
// addContentTypeHeaderFromBody, getExampleBodyContent, generatePlaceholderStructure, findSchemaByRef,
// addContentTypeHeaderFromContent, findPreviewLanguage, getStatusText, createErrorResource
// sont bien présentes et inchangées par rapport à la version précédente qui gérait les $ref.
private Parameter findParameter(Operation operation, String name, String inType) { if (operation.getParameters() == null) return null; for (Parameter param : operation.getParameters()) { if (inType.equalsIgnoreCase(param.getIn()) && name.equals(param.getName())) return param; } return null; }
// Utiliser URLEncoder standard maintenant
// private String urlEncode(String value) { return value.replace(" ", "%20").replace(":", "%3A").replace("/", "%2F").replace("?", "%3F").replace("&", "%26").replace("=", "%3D"); }
private String urlEncode(String value) { try { return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8.name()).replace("+", "%20"); } catch (Exception e) { System.err.println("WARN: Failed to URL encode: " + value); return value; } }
private JSONArray extractHeaders(Operation operation) { JSONArray headers = new JSONArray(); Set<String> headerKeys = new HashSet<>(); if (operation.getParameters() != null) { for (Parameter param : operation.getParameters()) { if ("header".equalsIgnoreCase(param.getIn())) { String key = param.getName(); if (headerKeys.add(key.toLowerCase())) { headers.put(new JSONObject().put("key", key).put("value", getParameterExampleValue(param)).put("description", param.getDescription() != null ? param.getDescription() : "")); } } } } String acceptHeader = findAcceptHeaderValue(operation.getResponses()); if (acceptHeader != null && headerKeys.add("accept")) { headers.put(new JSONObject().put("key", "Accept").put("value", acceptHeader).put("description", "Accepted response media type")); } return headers; }
private String getParameterExampleValue(Parameter param) { if (param.getExample() != null) return param.getExample().toString(); if (param.getExamples() != null && !param.getExamples().isEmpty()) { Example example = param.getExamples().values().iterator().next(); if (example.getValue() != null) return example.getValue().toString(); } if (param.getSchema() != null) { Schema<?> schema = param.getSchema(); if (schema.getExample() != null) return schema.getExample().toString(); if (schema.getDefault() != null) return schema.getDefault().toString(); String type = schema.getType() != null ? schema.getType() : "string"; String format = schema.getFormat(); if ("string".equals(type)) return format != null ? "<" + format + ">" : "<string>"; if ("integer".equals(type)) return format != null ? "<" + format + ">" : "<integer>"; if ("number".equals(type)) return format != null ? "<" + format + ">" : "<number>"; if ("boolean".equals(type)) return "<boolean>"; if ("array".equals(type)) return "[<array>]"; if ("object".equals(type)) return "{<object>}"; return "<" + type + ">"; } return ""; }
private String findAcceptHeaderValue(ApiResponses responses) { if (responses == null) return "application/json"; for (String code : responses.keySet()) { if (code.startsWith("2")) { ApiResponse response = responses.get(code); if (response != null && response.getContent() != null) { if (response.getContent().containsKey("application/json")) return "application/json"; Optional<String> firstType = response.getContent().keySet().stream().findFirst(); if (firstType.isPresent()) return firstType.get(); } } } ApiResponse defaultResponse = responses.get("default"); if (defaultResponse != null && defaultResponse.getContent() != null) { if (defaultResponse.getContent().containsKey("application/json")) return "application/json"; Optional<String> firstType = defaultResponse.getContent().keySet().stream().findFirst(); if (firstType.isPresent()) return firstType.get(); } return "application/json"; }
private JSONObject extractBody(OpenAPI openAPI, Operation operation) { RequestBody requestBody = operation.getRequestBody(); if (requestBody == null || requestBody.getContent() == null || requestBody.getContent().isEmpty()) return null; Content content = requestBody.getContent(); MediaType mediaType = content.get("application/json"); String contentType = "application/json"; if (mediaType == null) { Map.Entry<String, MediaType> firstEntry = content.entrySet().iterator().next(); contentType = firstEntry.getKey(); mediaType = firstEntry.getValue(); } JSONObject body = new JSONObject(); String language = "text"; String rawBodyContent = ""; if (contentType.toLowerCase().contains("json")) { body.put("mode", "raw"); language = "json"; rawBodyContent = mediaType != null ? getExampleBodyContent(mediaType, openAPI) : "{}"; } else if (contentType.toLowerCase().contains("xml")) { body.put("mode", "raw"); language = "xml"; rawBodyContent = mediaType != null ? getExampleBodyContent(mediaType, openAPI) : "<root/>"; } else if (contentType.startsWith("text/")) { body.put("mode", "raw"); language = "text"; rawBodyContent = mediaType != null ? getExampleBodyContent(mediaType, openAPI) : "<text content>"; } else { body.put("mode", "raw"); language = "text"; rawBodyContent = "/* Body content for type: " + contentType + " */"; } body.put("raw", rawBodyContent); if ("raw".equals(body.optString("mode"))) { JSONObject options = new JSONObject().put("raw", new JSONObject().put("language", language)); body.put("options", options); } return body; }
private void addContentTypeHeaderFromBody(JSONArray headers, JSONObject body) { if (body == null) return; String contentType = null; String mode = body.optString("mode", "raw"); if ("raw".equals(mode)) { JSONObject options = body.optJSONObject("options"); JSONObject rawOptions = options != null ? options.optJSONObject("raw") : null; if (rawOptions != null && rawOptions.has("language")) { String language = rawOptions.getString("language"); switch (language.toLowerCase()) { case "json": contentType = "application/json"; break; case "xml": contentType = "application/xml"; break; case "text": contentType = "text/plain"; break; } } else { contentType = "application/json"; } } else if ("urlencoded".equals(mode)) { contentType = "application/x-www-form-urlencoded"; } if (contentType != null) { boolean exists = false; for (int i = 0; i < headers.length(); i++) { if ("content-type".equalsIgnoreCase(headers.getJSONObject(i).optString("key"))) { exists = true; break; } } if (!exists) { headers.put(new JSONObject().put("key", "Content-Type").put("value", contentType)); } } }
private String getExampleBodyContent(MediaType mediaType, OpenAPI openAPI) { if (mediaType.getExample() != null && mediaType.getExample() instanceof String) return (String) mediaType.getExample(); if (mediaType.getExamples() != null && !mediaType.getExamples().isEmpty()) { for (Example example : mediaType.getExamples().values()) { Object value = example.getValue(); if (value != null && value instanceof String) return (String) value; } } Schema<?> schema = mediaType.getSchema(); if (schema != null && schema.getExample() != null && schema.getExample() instanceof String) return (String) schema.getExample(); if (schema != null) { try { Object generatedStructure = generatePlaceholderStructure(schema, openAPI, new HashSet<>()); if (generatedStructure instanceof JSONObject) return ((JSONObject) generatedStructure).toString(2); if (generatedStructure instanceof JSONArray) return ((JSONArray) generatedStructure).toString(2); if (generatedStructure != null) { try { return OBJECT_MAPPER.writeValueAsString(generatedStructure); } catch (JsonProcessingException e) { System.err.println("WARN: Could not serialize simple generated structure: " + e.getMessage()); } } } catch (StackOverflowError e) { System.err.println("ERROR: StackOverflowError during placeholder generation. Schema: " + (schema.get$ref() != null ? schema.get$ref() : schema.getType())); return new JSONObject().put("error", "Could not generate example due to circular reference or excessive depth").toString(2); } catch (Exception e) { System.err.println("WARN: Unexpected error generating placeholder structure: " + e.getMessage()); e.printStackTrace(); } } System.err.println("WARN: No explicit example string found and placeholder generation failed or schema missing."); return "{}"; }
private Object generatePlaceholderStructure(Schema<?> schema, OpenAPI openAPI, Set<String> processingRefs) { if (schema == null) return JSONObject.NULL; if (schema.get$ref() != null) { String ref = schema.get$ref(); if (processingRefs.contains(ref)) return "<Circular Reference: " + ref.substring(ref.lastIndexOf('/') + 1) + ">"; Schema<?> resolvedSchema = findSchemaByRef(ref, openAPI); if (resolvedSchema != null) { Set<String> nextProcessingRefs = new HashSet<>(processingRefs); nextProcessingRefs.add(ref); return generatePlaceholderStructure(resolvedSchema, openAPI, nextProcessingRefs); } else { System.err.println("WARN: Could not resolve schema reference: " + ref); return "<Unresolved Reference: " + ref.substring(ref.lastIndexOf('/') + 1) + ">"; } } if (schema.getDefault() != null) return schema.getDefault(); if (schema.getEnum() != null && !schema.getEnum().isEmpty()) return schema.getEnum().get(0); String type = schema.getType(); if (type == null) { if (schema.getProperties() != null && !schema.getProperties().isEmpty()) type = "object"; else if (schema.getItems() != null) type = "array"; else if (schema.getOneOf() != null || schema.getAnyOf() != null || schema.getAllOf() != null) { if(schema.getOneOf() != null) return "<oneOf schema>"; if(schema.getAnyOf() != null) return "<anyOf schema>"; if(schema.getAllOf() != null) return "<allOf schema>"; } else return JSONObject.NULL; } switch (type) { case "object": JSONObject obj = new JSONObject(); if (schema.getProperties() != null) { Map<String, Schema> sortedProperties = new TreeMap<>(schema.getProperties()); sortedProperties.forEach((key, propSchema) -> { obj.put(key, generatePlaceholderStructure(propSchema, openAPI, processingRefs)); }); } return obj; case "array": JSONArray arr = new JSONArray(); if (schema.getItems() != null) arr.put(generatePlaceholderStructure(schema.getItems(), openAPI, processingRefs)); return arr; case "string": String format = schema.getFormat(); if (format != null) { switch (format) { case "date": return "<date>"; case "date-time": return "<dateTime>"; case "password": return "<password>"; case "byte": return "<byte>"; case "binary": return "<binary>"; case "email": return "<email>"; case "uuid": return "<uuid>"; case "uri": return "<uri>"; case "url": return "<url>"; case "hostname": return "<hostname>"; default: return "<string (" + format + ")>"; } } return "<string>"; case "integer": String intFormat = schema.getFormat(); if ("int64".equals(intFormat)) return "<long>"; return "<integer>"; case "number": String numFormat = schema.getFormat(); if ("float".equals(numFormat)) return "<float>"; if ("double".equals(numFormat)) return "<double>"; return "<number>"; case "boolean": return "<boolean>"; case "null": return JSONObject.NULL; default: System.err.println("WARN: Unknown schema type: " + type); return "<" + type + ">"; } }
private Schema<?> findSchemaByRef(String ref, OpenAPI openAPI) { if (ref == null || openAPI == null || openAPI.getComponents() == null || openAPI.getComponents().getSchemas() == null) return null; final String prefix = "#/components/schemas/"; if (ref.startsWith(prefix)) { String schemaName = ref.substring(prefix.length()); return openAPI.getComponents().getSchemas().get(schemaName); } System.err.println("WARN: Unsupported reference format encountered: " + ref); return null; }
private void addContentTypeHeaderFromContent(JSONArray headers, Content content) { if (content == null || content.isEmpty()) return; String contentType = null; if (content.containsKey("application/json")) contentType = "application/json"; else contentType = content.keySet().iterator().next(); boolean exists = false; for (int i = 0; i < headers.length(); i++) { if ("content-type".equalsIgnoreCase(headers.getJSONObject(i).optString("key"))) { exists = true; break; } } if (!exists && contentType != null) headers.put(new JSONObject().put("key", "Content-Type").put("value", contentType)); }
private String findPreviewLanguage(Content content) { if (content == null || content.isEmpty()) return "text"; if (content.containsKey("application/json")) return "json"; if (content.containsKey("application/xml")) return "xml"; if (content.containsKey("text/html")) return "html"; if (content.containsKey("application/javascript")) return "javascript"; if (content.containsKey("text/plain")) return "text"; String firstKey = content.keySet().iterator().next().toLowerCase(); if (firstKey.contains("json")) return "json"; if (firstKey.contains("xml")) return "xml"; if (firstKey.contains("html")) return "html"; if (firstKey.contains("javascript")) return "javascript"; return "text"; }
private String getStatusText(String code) { if ("default".equalsIgnoreCase(code)) return "Default Response"; try { int statusCode = Integer.parseInt(code); HttpStatus status = HttpStatus.resolve(statusCode); if (status != null) return status.getReasonPhrase(); } catch (NumberFormatException ex) { /* ignore */ } return "Status " + code; }
private Resource createErrorResource(String message) { JSONObject errorJson = new JSONObject().put("error", message); byte[] errorBytes = errorJson.toString(2).getBytes(StandardCharsets.UTF_8); return new ByteArrayResource(errorBytes) { @Override public String getFilename() { return "error.json"; } }; }


// Fin de la classe
}
