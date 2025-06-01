package com.example.api_tierces.service;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import io.swagger.v3.oas.models.parameters.RequestBody;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import com.example.api_tierces.model.Api;
import com.example.api_tierces.model.ApiChange;
import com.example.api_tierces.model.ApiParameters;
import com.example.api_tierces.model.Schema;
import com.example.api_tierces.repository.ApiChangeRepository;
import com.example.api_tierces.repository.ApiParametersRepository;
import com.example.api_tierces.repository.ApiRepository;
import com.example.api_tierces.repository.ApiResponseRepository;
import com.example.api_tierces.repository.SchemaRepository;
import java.time.LocalDateTime;

@Service
public class FileUploadService implements UploadService {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadService.class);

    private final ApiRepository apiRepository;
    private final ApiResponseRepository apiResponseRepository;
    private final SchemaRepository schemaRepository;
    private final ApiParametersRepository apiParametersRepository;
    private final ApiChangeRepository apiChangeRepository; // Injectez le repository ApiChange

    public FileUploadService(ApiRepository apiRepository,
                             ApiResponseRepository apiResponseRepository,
                             SchemaRepository schemaRepository,
                             ApiParametersRepository apiParametersRepository,
                             ApiChangeRepository apiChangeRepository) {
        this.apiRepository = apiRepository;
        this.apiResponseRepository = apiResponseRepository;
        this.schemaRepository = schemaRepository;
        this.apiParametersRepository = apiParametersRepository;
        this.apiChangeRepository = apiChangeRepository;
    }

    @Transactional
    public String parseSwaggerFileFromUrl(String fileContent) {
        try {
            return parseSwaggerFile(fileContent);
        } catch (Exception e) {
            logger.error("Erreur lors du traitement du fichier Swagger : {}", e.getMessage());
            return "Erreur lors du traitement du fichier Swagger : " + e.getMessage();
        }
    }

    @Transactional
    public String parseSwaggerFile(String fileContent) {
        boolean hasChanges = false; // Indicateur de changement global
        List<String> changesDetected = new ArrayList<>();// Liste de stockage des changements

        try {
            OpenAPIV3Parser parser = new OpenAPIV3Parser();
            OpenAPI openAPI = parser.readContents(fileContent, null, null).getOpenAPI();
            String version = openAPI.getInfo().getVersion(); //extrait la version du fichier swagger

            // Gestion des schémas
            Map<String, io.swagger.v3.oas.models.media.Schema> schemas = openAPI.getComponents().getSchemas();
            Set<String> currentSchemaNames = new HashSet<>();

            if (schemas != null) {
                for (Map.Entry<String, io.swagger.v3.oas.models.media.Schema> entry : schemas.entrySet()) {
                    String schemaName = entry.getKey();
                    io.swagger.v3.oas.models.media.Schema schemaValue = entry.getValue();
                    if (saveSchema(schemaName, schemaValue, openAPI, changesDetected)) {
                        hasChanges = true; // Changement détecté
                    }
                    currentSchemaNames.add(schemaName);
                }
            }

            // Supprimer les schémas obsolètes
            if (deleteObsoleteSchemas(currentSchemaNames, changesDetected)) {
                hasChanges = true; // Changement détecté
            }

            // Gestion des chemins (API)
            Map<String, PathItem> paths = openAPI.getPaths();
            Set<String> currentApis = new HashSet<>(); // Changed this line


            if (paths != null) {
                for (Map.Entry<String, PathItem> pathEntry : paths.entrySet()) {
                    String pathName = pathEntry.getKey();
                    PathItem pathValue = pathEntry.getValue();


                    if (pathValue.getGet() != null) {
                        if (processOperation(pathName, pathValue.getGet(), "GET", version, openAPI, changesDetected)) {
                            hasChanges = true; // Changement détecté
                        }
                        currentApis.add(pathName + ":" + "GET");
                    }
                    if (pathValue.getPost() != null) {
                        if (processOperation(pathName, pathValue.getPost(), "POST", version, openAPI, changesDetected)) {
                            hasChanges = true; // Changement détecté
                        }
                        currentApis.add(pathName + ":" + "POST");
                    }
                    if (pathValue.getPut() != null) {
                        if (processOperation(pathName, pathValue.getPut(), "PUT", version, openAPI, changesDetected)) {
                            hasChanges = true; // Changement détecté
                        }
                        currentApis.add(pathName + ":" + "PUT");
                    }
                    if (pathValue.getDelete() != null) {
                        if (processOperation(pathName, pathValue.getDelete(), "DELETE", version, openAPI, changesDetected)) {
                            hasChanges = true; // Changement détecté
                        }
                        currentApis.add(pathName + ":" + "DELETE");
                    }
                    if (pathValue.getOptions() != null) {
                        if (processOperation(pathName, pathValue.getOptions(), "OPTIONS", version, openAPI, changesDetected)) {
                            hasChanges = true; // Changement détecté
                        }
                        currentApis.add(pathName + ":" + "OPTIONS");
                    }
                    if (pathValue.getHead() != null) {
                        if (processOperation(pathName, pathValue.getHead(), "HEAD", version, openAPI, changesDetected)) {
                            hasChanges = true; // Changement détecté
                        }
                        currentApis.add(pathName + ":" + "HEAD");
                    }
                    if (pathValue.getTrace() != null) {
                        if (processOperation(pathName, pathValue.getTrace(), "TRACE", version, openAPI, changesDetected)) {
                            hasChanges = true; // Changement détecté
                        }
                        currentApis.add(pathName + ":" + "TRACE");
                    }
                }
            }


            // Supprimer les API obsolètes
            if (deleteObsoleteApis(currentApis, changesDetected)) {
                hasChanges = true; // Changement détecté
            }


            // Afficher le message en fonction de hasChanges
            StringBuilder changesString = new StringBuilder();
            for (String change : changesDetected) {
                changesString.append("- ").append(change).append("\n");
            }

            if (hasChanges) {
                logChanges("Des changements ont été détectés et enregistrés.");
                saveApiChange("oui", changesString.toString()); // Enregistre "oui" si des changements ont été détectés
            } else {
                logChanges("Aucun changement réalisé.");
                saveApiChange("non", "Aucun changement."); // Enregistre "non" si aucun changement n'a été détecté
            }

            return "Fichier Swagger analysé et enregistré avec succès !";
        } catch (Exception e) {
            logger.error("Erreur lors du traitement du fichier : {}", e.getMessage());
            return "Erreur lors du traitement du fichier : " + e.getMessage();
        }
    }

    private void saveApiChange(String changement, String donneeChange) {
        if ("oui".equalsIgnoreCase(changement)) {
            ApiChange newApiChange = new ApiChange();
            newApiChange.setChangement(changement);
            newApiChange.setTemps(LocalDateTime.now());
            newApiChange.setDonneeChange(donneeChange);
            apiChangeRepository.save(newApiChange);
        }
    }


    private void logChanges(String message) {
        logger.info(message);
    }

    private boolean hasChanged(Object oldValue, Object newValue) {
        if (oldValue == null && newValue == null) {
            return false;
        }
        if (oldValue == null || newValue == null) {
            return true;
        }
        return !oldValue.equals(newValue);
    }

    private boolean saveSchema(String name, io.swagger.v3.oas.models.media.Schema schema, OpenAPI openAPI, List<String> changesDetected) {
        List<com.example.api_tierces.model.Schema> existingSchemas = schemaRepository.findByName(name);
        com.example.api_tierces.model.Schema schemaToSave;
        boolean hasChanged = false;

        if (existingSchemas != null && !existingSchemas.isEmpty()) {
            // Mettre à jour le schéma existant
            schemaToSave = existingSchemas.get(0);

            // Vérifier si les valeurs ont changé
            if (hasChanged(schemaToSave.getType(), schema.getType())) {
                String message = "Type du schéma '" + name + "' modifié de '" + schemaToSave.getType() + "' à '" + schema.getType() + "'";
                logChanges(message);
                schemaToSave.setType(schema.getType());
                changesDetected.add(message);
                hasChanged = true;
            }

            String newSchemaContent = Json.pretty(schema);
            if (hasChanged(schemaToSave.getSchemas(), newSchemaContent)) {
                String message = "Contenu du schéma '" + name + "' modifié.";
                logChanges(message);
                schemaToSave.setSchemas(newSchemaContent);
                changesDetected.add(message);
                hasChanged = true;
            }

            if (hasChanged) {
                schemaRepository.save(schemaToSave);
            }
        } else {
            // Créer un nouveau schéma
            schemaToSave = new com.example.api_tierces.model.Schema();
            schemaToSave.setName(name);
            schemaToSave.setType(schema.getType());
            schemaToSave.setSchemas(Json.pretty(schema));
            String message = "Ajout d'un nouveau schéma: " + name;
            logChanges(message);
            changesDetected.add(message);
            schemaRepository.save(schemaToSave);
            hasChanged = true;
        }

        return hasChanged;
    }

    private boolean deleteObsoleteSchemas(Set<String> currentSchemaNames, List<String> changesDetected) {
        boolean hasChanged = false;
        List<com.example.api_tierces.model.Schema> existingSchemas = schemaRepository.findAll();

        for (com.example.api_tierces.model.Schema existingSchema : existingSchemas) {
            if (!currentSchemaNames.contains(existingSchema.getName())) {

                List<com.example.api_tierces.model.ApiResponse> responsesReferencingSchema = apiResponseRepository.findBySchema(existingSchema);
                if (!responsesReferencingSchema.isEmpty()) {
                    String message = "Suppression des réponses référençant le schéma obsolète: " + existingSchema.getName();
                    logChanges(message);
                    apiResponseRepository.deleteAll(responsesReferencingSchema);
                    changesDetected.add(message);
                    hasChanged = true;
                }

                String message = "Suppression du schéma obsolète: " + existingSchema.getName();
                logChanges(message);
                schemaRepository.delete(existingSchema);
                changesDetected.add(message);
                hasChanged = true;
            }
        }

        return hasChanged;
    }


    private boolean processOperation(String path, Operation operation, String method, String version, OpenAPI openAPI, List<String> changesDetected) {
        boolean hasChanged = false;

        if (operation != null) {
            // Extraction des tags
            List<String> operationTags = operation.getTags();
            String tag = (operationTags != null && !operationTags.isEmpty()) ? String.join(", ", operationTags) : null;

            Api existingApi = apiRepository.findByPathAndMethod(path, method);
            Api api;

            if (existingApi != null) {
                // Mettre à jour l'API existante
                api = existingApi;

                // Vérifier si les valeurs ont changé
                String newDescription = operation.getDescription() != null ? operation.getDescription() : operation.getSummary();
                if (hasChanged(api.getDescription(), newDescription)) {
                    String message = "Description de l'API '" + path + " (" + method + ")' modifiée de '" + api.getDescription() + "' à '" + newDescription + "'";
                    logChanges(message);
                    api.setDescription(newDescription);
                    changesDetected.add(message);
                    hasChanged = true;
                }
                if (hasChanged(api.getVersion(), version)) {
                    String message = "Version de l'API '" + path + " (" + method + ")' modifiée de '" + api.getVersion() + "' à '" + version + "'";
                    logChanges(message);
                    api.setVersion(version);
                    changesDetected.add(message);
                    hasChanged = true;
                }

                // Mettre à jour le tag s'il a changé
                if (hasChanged(api.getTags(), tag)) {
                    String message = "Tag de l'API '" + path + " (" + method + ")' modifié de '" + api.getTags() + "' à '" + tag + "'";
                    logChanges(message);
                    api.setTags(tag);
                    changesDetected.add(message);
                    hasChanged = true;
                }

                // Gestion du request body
                RequestBody requestBody = operation.getRequestBody();
                if (requestBody != null) {
                    Content content = requestBody.getContent();
                    if (content != null && content.containsKey("application/json")) {
                        MediaType mediaType = content.get("application/json");
                        io.swagger.v3.oas.models.media.Schema schema = mediaType.getSchema();

                        if (schema != null) {
                            // Extraire le contenu du request body
                            String requestBodyContent = extractRequestBodyContent(schema, openAPI);
                            if (hasChanged(api.getRequest_body(), requestBodyContent)) {
                                String message = "Request body de l'API '" + path + " (" + method + ")' modifié.";
                                logChanges(message);
                                api.setRequest_body(requestBodyContent);
                                changesDetected.add(message);
                                hasChanged = true;
                            }


                            // Extraire et enregistrer le nom du schéma
                            String schemaName = extractSchemaRef(schema);
                            if (schemaName != null && !schemaName.isEmpty()) {
                                if (hasChanged(api.getSchema_name(), schemaName)) {
                                    String message = "Schema name de l'API '" + path + " (" + method + ")' modifié de '" + api.getSchema_name() + "' à '" + schemaName + "'";
                                    logChanges(message);
                                    api.setSchema_name(schemaName);
                                    changesDetected.add(message);
                                    hasChanged = true;
                                }

                            }
                        }
                    }
                }


                if (hasChanged) {
                    apiRepository.save(api);
                }
            } else {
                // Créer une nouvelle API
                api = new Api();
                api.setPath(path);
                api.setMethod(method);
                api.setDescription(operation.getDescription() != null ? operation.getDescription() : operation.getSummary());
                api.setVersion(version);
                api.setTags(tag);
                // Gestion du request body
                RequestBody requestBody = operation.getRequestBody();
                if (requestBody != null) {
                    Content content = requestBody.getContent();
                    if (content != null && content.containsKey("application/json")) {
                        MediaType mediaType = content.get("application/json");
                        io.swagger.v3.oas.models.media.Schema schema = mediaType.getSchema();

                        if (schema != null) {
                            // Extraire le contenu du request body
                            String requestBodyContent = extractRequestBodyContent(schema, openAPI);
                            api.setRequest_body(requestBodyContent);

                            // Extraire et enregistrer le nom du schéma
                            String schemaName = extractSchemaRef(schema);
                            if (schemaName != null && !schemaName.isEmpty()) {
                                api.setSchema_name(schemaName);
                            }
                        }
                    }
                }


                String message = "Ajout d'une nouvelle API: " + path + " (" + method + ")";
                logChanges(message);
                changesDetected.add(message);
                apiRepository.save(api);
                hasChanged = true;
            }

            //Sauvegarder l'API
            Api savedApi = apiRepository.save(api);

            // Gestion des paramètres
            hasChanged = processParameters(openAPI, path, operation, method, savedApi, hasChanged, changesDetected);

            // Gestion des réponses
            hasChanged = processResponses(openAPI, path, operation, method, savedApi, hasChanged, changesDetected);


        }

        return hasChanged;
    }

    private boolean processParameters(OpenAPI openAPI, String path, Operation operation, String method, Api savedApi, boolean hasChanged, List<String> changesDetected) {
        List<Parameter> parameters = operation.getParameters();
        Set<String> currentParameterNames = new HashSet<>();
        if (parameters != null) {
            for (Parameter parameter : parameters) {
                currentParameterNames.add(parameter.getName());
                ApiParameters existingParameter = apiParametersRepository.findByApiAndName(savedApi, parameter.getName());

                ApiParameters apiParameters;
                boolean parameterChanged = false;

                if (existingParameter != null) {
                    // Mettre à jour le paramètre existant
                    apiParameters = existingParameter;

                    // Vérifier si les valeurs ont changé
                    if (hasChanged(apiParameters.getTypein(), parameter.getIn() != null ? parameter.getIn().toString() : null)) {
                        String message = "Type du paramètre '" + parameter.getName() + "' de l'API '" + path + " (" + method + ")' modifié de '" + apiParameters.getTypein() + "' à '" + (parameter.getIn() != null ? parameter.getIn().toString() : null) + "'";
                        logChanges(message);
                        apiParameters.setTypein(parameter.getIn() != null ? parameter.getIn().toString() : null);
                        changesDetected.add(message);
                        parameterChanged = true;
                    }
                    if (hasChanged(apiParameters.getExample(), parameter.getExample() != null ? parameter.getExample().toString() : "Aucun exemple disponible")) {
                        String message = "Example du paramètre '" + parameter.getName() + "' de l'API '" + path + " (" + method + ")' modifié de '" + apiParameters.getExample() + "' à '" + (parameter.getExample() != null ? parameter.getExample().toString() : "Aucun exemple disponible") + "'";
                        logChanges(message);
                        apiParameters.setExample(parameter.getExample() != null ? parameter.getExample().toString() : "Aucun exemple disponible");
                        changesDetected.add(message);
                        parameterChanged = true;
                    }
                    if (hasChanged(apiParameters.getData_type(), parameter.getSchema() != null ? parameter.getSchema().getType() : null)) {
                        String message = "Data type du paramètre '" + parameter.getName() + "' de l'API '" + path + " (" + method + ")' modifié de '" + apiParameters.getData_type() + "' à '" + (parameter.getSchema() != null ? parameter.getSchema().getType() : null) + "'";
                        logChanges(message);
                        apiParameters.setData_type(parameter.getSchema() != null ? parameter.getSchema().getType() : null);
                        changesDetected.add(message);
                        parameterChanged = true;
                    }
                    if (hasChanged(apiParameters.getDescription(), parameter.getDescription())) {
                        String message = "Description du paramètre '" + parameter.getName() + "' de l'API '" + path + " (" + method + ")' modifiée de '" + apiParameters.getDescription() + "' à '" + parameter.getDescription() + "'";
                        logChanges(message);
                        apiParameters.setDescription(parameter.getDescription());
                        changesDetected.add(message);
                        parameterChanged = true;
                    }
                    if (hasChanged(apiParameters.getRequired(), parameter.getRequired())) {
                        String message = "Required du paramètre '" + parameter.getName() + "' de l'API '" + path + " (" + method + ")' modifié de '" + apiParameters.getRequired() + "' à '" + parameter.getRequired() + "'";
                        logChanges(message);
                        apiParameters.setRequired(parameter.getRequired());
                        changesDetected.add(message);
                        parameterChanged = true;
                    }

                    if (parameterChanged) {
                        try {
                            apiParametersRepository.save(apiParameters);
                            hasChanged = true;
                        } catch (Exception e) {
                            logger.error("Erreur lors de la sauvegarde du parametre : {}", e.getMessage());
                        }

                    }


                } else {
                    // Créer un nouveau paramètre
                    apiParameters = new ApiParameters();
                    apiParameters.setApi(savedApi);
                    apiParameters.setName(parameter.getName());
                    if (parameter.getIn() != null) {
                        apiParameters.setTypein(parameter.getIn().toString());
                    }
                    if (parameter.getExample() != null) {
                        apiParameters.setExample(parameter.getExample().toString());
                    } else {
                        apiParameters.setExample("Aucun exemple disponible");
                    }
                    if (parameter.getSchema() != null) {
                        apiParameters.setData_type(parameter.getSchema().getType());
                    }
                    if (parameter.getDescription() != null) {
                        apiParameters.setDescription(parameter.getDescription());
                    }
                    apiParameters.setRequired(parameter.getRequired());
                    String message = "Ajout du paramètre '" + parameter.getName() + "' à l'API '" + path + " (" + method + ")'";
                    logChanges(message);
                    changesDetected.add(message);
                    try {
                        apiParametersRepository.save(apiParameters);
                        hasChanged = true;
                    } catch (Exception e) {
                        logger.error("Erreur lors de la sauvegarde du parametre : {}", e.getMessage());
                    }

                }


            }
        }

        //Supprimer les parametres obsolètes
        List<ApiParameters> existingParameters = apiParametersRepository.findByApi(savedApi);
        for (ApiParameters existingParameter : existingParameters) {
            if (!currentParameterNames.contains(existingParameter.getName())) {
                String message = "Suppression du paramètre obsolète '" + existingParameter.getName() + "' de l'API '" + path + " (" + method + ")'";
                logChanges(message);
                changesDetected.add(message);
                try {
                    apiParametersRepository.delete(existingParameter);
                    hasChanged = true;
                } catch (Exception e) {
                    logger.error("Erreur lors de la suppression du parametre : {}", e.getMessage());
                }


            }
        }
        return hasChanged;
    }


    private boolean processResponses(OpenAPI openAPI, String path, Operation operation, String method, Api savedApi, boolean hasChanged, List<String> changesDetected) {
        if (operation.getResponses() != null) {
            Set<String> currentStatuses = new HashSet<>();

            for (Map.Entry<String, io.swagger.v3.oas.models.responses.ApiResponse> entry : operation.getResponses().entrySet()) {
                String status = entry.getKey();
                io.swagger.v3.oas.models.responses.ApiResponse responseValue = entry.getValue();
                currentStatuses.add(status);

                com.example.api_tierces.model.ApiResponse existingResponse = apiResponseRepository.findByApiAndStatus(savedApi, status);
                com.example.api_tierces.model.ApiResponse apiResponse;
                boolean responseChanged = false;

                if (existingResponse != null) {
                    // Mettre à jour la réponse existante
                    apiResponse = existingResponse;

                    // Vérifier si les valeurs ont changé
                    if (hasChanged(apiResponse.getDescription(), responseValue.getDescription())) {
                        String message = "Description de la réponse '" + status + "' de l'API '" + path + " (" + method + ")' modifiée de '" + apiResponse.getDescription() + "' à '" + responseValue.getDescription() + "'";
                        logChanges(message);
                        apiResponse.setDescription(responseValue.getDescription());
                        changesDetected.add(message);
                        responseChanged = true;
                    }

                    // Extraire le nom du schéma et récupérer l'entité Schema correspondante
                    String schemaNameRef = null;
                    Schema schemaEntity = null;
                    if (responseValue.getContent() != null && responseValue.getContent().containsKey("application/json")) {
                        MediaType mediaType = responseValue.getContent().get("application/json");
                        if (mediaType != null) {
                            io.swagger.v3.oas.models.media.Schema schema = mediaType.getSchema();
                            if (schema != null) {
                                schemaNameRef = extractSchemaRef(schema);
                                if (schemaNameRef != null) {
                                    // Récupérer le schéma correspondant à partir de la base de données
                                    schemaEntity = schemaRepository.findByName(schemaNameRef).stream().findFirst().orElse(null);
                                    if (schemaEntity == null) {
                                        logger.warn("Le schéma '{}' n'existe pas dans la base de données.", schemaNameRef);
                                    }
                                }
                            }
                        }
                    }

                    // Mettre à jour le nom du schéma et la référence au schéma
                    if (hasChanged(apiResponse.getName_schema(), schemaNameRef)) {
                        String message = "Name Schema de la réponse '" + status + "' de l'API '" + path + " (" + method + ")' modifié de '" + apiResponse.getName_schema() + "' à '" + schemaNameRef + "'";
                        logChanges(message);
                        apiResponse.setName_schema(schemaNameRef);
                        apiResponse.setSchema(schemaEntity); // Lier l'entité Schema
                        changesDetected.add(message);
                        responseChanged = true;
                    }

                    if (responseChanged) {
                        try {
                            apiResponseRepository.save(apiResponse);
                            hasChanged = true;
                        } catch (Exception e) {
                            logger.error("Erreur lors de la sauvegarde de la réponse : {}", e.getMessage());
                        }
                    }

                } else {
                    // Créer une nouvelle réponse
                    apiResponse = new com.example.api_tierces.model.ApiResponse();
                    apiResponse.setApi(savedApi);
                    apiResponse.setStatus(status);
                    apiResponse.setDescription(responseValue.getDescription());

                    // Extraire le nom du schéma et récupérer l'entité Schema correspondante
                    String schemaNameRef = null;
                    Schema schemaEntity = null;
                    if (responseValue.getContent() != null && responseValue.getContent().containsKey("application/json")) {
                        MediaType mediaType = responseValue.getContent().get("application/json");
                        if (mediaType != null) {
                            io.swagger.v3.oas.models.media.Schema schema = mediaType.getSchema();
                            if (schema != null) {
                                schemaNameRef = extractSchemaRef(schema);
                                if (schemaNameRef != null) {
                                    // Récupérer le schéma correspondant à partir de la base de données
                                    schemaEntity = schemaRepository.findByName(schemaNameRef).stream().findFirst().orElse(null);
                                    if (schemaEntity == null) {
                                        logger.warn("Le schéma '{}' n'existe pas dans la base de données.", schemaNameRef);
                                    }
                                }
                            }
                        }
                    }

                    // Lier le schéma à la réponse
                    apiResponse.setName_schema(schemaNameRef);
                    apiResponse.setSchema(schemaEntity); // Lier l'entité Schema

                    String message = "Ajout de la réponse '" + status + "' à l'API '" + path + " (" + method + ")'";
                    logChanges(message);
                    changesDetected.add(message);
                    try {
                        apiResponseRepository.save(apiResponse);
                        hasChanged = true;
                    } catch (Exception e) {
                        logger.error("Erreur lors de la sauvegarde de la réponse : {}", e.getMessage());
                    }
                }
            }

            // Supprimer les réponses obsolètes
            List<com.example.api_tierces.model.ApiResponse> existingResponses = apiResponseRepository.findByApi(savedApi);
            for (com.example.api_tierces.model.ApiResponse existingResponse : existingResponses) {
                if (!currentStatuses.contains(existingResponse.getStatus())) {
                    String message = "Suppression de la réponse obsolète '" + existingResponse.getStatus() + "' de l'API '" + path + " (" + method + ")'";
                    logChanges(message);
                    changesDetected.add(message);
                    try {
                        apiResponseRepository.delete(existingResponse);
                        hasChanged = true;
                    } catch (Exception e) {
                        logger.error("Erreur lors de la suppression de la réponse : {}", e.getMessage());
                    }
                }
            }
        }

        return hasChanged;
    }

    private boolean deleteObsoleteApis(Set<String> currentApis, List<String> changesDetected) {
        boolean hasChanged = false;
        List<Api> existingApis = apiRepository.findAll();

        for (Api existingApi : existingApis) {
            // Concaténer path et method pour avoir un identifiant unique
            String apiIdentifier = existingApi.getPath() + ":" + existingApi.getMethod();

            // Vérifier si cette API existe toujours dans Swagger
            if (currentApis.contains(apiIdentifier)) {
                logger.info("L'API existe toujours : " + apiIdentifier);
                continue; // Ne pas supprimer cette API
            }

            logger.info("Suppression de l'API obsolète : " + apiIdentifier);

            // 1. Supprimer les paramètres associés à cette API
            List<ApiParameters> parametersReferencingApi = apiParametersRepository.findByApi(existingApi);
            if (!parametersReferencingApi.isEmpty()) {
                String message = "Suppression des paramètres de l'API obsolète: " + apiIdentifier;
                logChanges(message);
                apiParametersRepository.deleteAll(parametersReferencingApi);
                changesDetected.add(message);
            }

            // 2. Supprimer les réponses associées à l'API
            List<com.example.api_tierces.model.ApiResponse> responsesReferencingApi = apiResponseRepository.findByApi(existingApi);
            if (!responsesReferencingApi.isEmpty()) {
                String message = "Suppression des réponses de l'API obsolète: " + apiIdentifier;
                logChanges(message);
                apiResponseRepository.deleteAll(responsesReferencingApi);
                changesDetected.add(message);
            }

            // 3. Supprimer l'API elle-même
            String message = "Suppression de l'API obsolète: " + apiIdentifier;
            logChanges(message);
            try {
                apiRepository.delete(existingApi);
                changesDetected.add(message);
                hasChanged = true;
            } catch (Exception e) {
                logger.error("Erreur lors de la suppression de l'API : {}", e.getMessage());
            }
        }

        return hasChanged;
    }


    private String extractRequestBodyContent(io.swagger.v3.oas.models.media.Schema schema, OpenAPI openAPI) {
        try {
            if (schema.get$ref() != null) {
                String ref = schema.get$ref();
                String refName = ref.replace("#/components/schemas/", "");
                Map<String, io.swagger.v3.oas.models.media.Schema> schemas = openAPI.getComponents().getSchemas();

                if (schemas != null && schemas.containsKey(refName)) {
                    io.swagger.v3.oas.models.media.Schema resolvedSchema = schemas.get(refName);
                    return Json.pretty(resolvedSchema);
                } else {
                    return "Schema reference not found: " + ref;
                }
            } else {
                return Json.pretty(schema);
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la sérialisation du schéma : {}", e.getMessage());
            return "Erreur de sérialisation";
        }
    }

    private String extractSchemaRef(io.swagger.v3.oas.models.media.Schema schema) {
        if (schema.get$ref() != null) {
            return schema.get$ref().replace("#/components/schemas/", "");
        }
        return null;
    }


}