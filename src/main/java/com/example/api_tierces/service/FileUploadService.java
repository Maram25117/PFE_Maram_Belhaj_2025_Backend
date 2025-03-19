/*package com.example.api_tierces.service;


import com.example.api_tierces.model.*;
import com.example.api_tierces.repository.*;
import io.swagger.v3.core.util.Json; //Ce module appartient également à Swagger OpenAPI et est utilisé pour manipuler et sérialiser/désérialiser des objets en JSON dans la documentation OpenAPI.
import io.swagger.v3.oas.models.OpenAPI; //représente l'éléments d’une API
import io.swagger.v3.oas.models.Operation; //représente l'éléments d’une API
import io.swagger.v3.oas.models.PathItem; //représente l'éléments d’une API
import io.swagger.v3.oas.models.media.Content; //utilisé pour définir le contenu des réponses d'une API dans la documentation OpenAPI.Il permet de spécifier les types de médias (application/json, application/xml, etc.) renvoyés par une API.
import io.swagger.v3.oas.models.media.MediaType; //utilisé pour gérer les requêtes et réponses.
import io.swagger.v3.oas.models.parameters.Parameter; //utilisé pour gérer les requêtes et réponses.
import io.swagger.v3.parser.OpenAPIV3Parser; //permet de lire un fichier Swagger
import org.springframework.stereotype.Service; //Annotation de Spring Boot qui marque une classe comme un service
import io.swagger.v3.oas.models.parameters.RequestBody; //utilisé pour gérer les requêtes et réponses.
import org.springframework.transaction.annotation.Transactional; // pour gérer les transactions dans une application Spring Boot.

//Importe des classes utilitaires Java pour gérer les collections (List, Map, ArrayList, HashMap) et des opérations sur les objets.
import java.util.List;
import java.util.Map;
import java.util.Iterator;

@Service
public class FileUploadService implements UploadService {
    //Déclare des repositories pour gérer l’accès aux données des API, des réponses, des schémas et des paramètres.
    private final ApiRepository apiRepository;
    private final ApiResponseRepository apiResponseRepository;
    private final SchemaRepository schemaRepository;
    private final ApiParametersRepository apiParametersRepository;

    public FileUploadService(ApiRepository apiRepository,
                             ApiResponseRepository apiResponseRepository,
                             SchemaRepository schemaRepository,
                             ApiParametersRepository apiParametersRepository) {
        this.apiRepository = apiRepository;
        this.apiResponseRepository = apiResponseRepository;
        this.schemaRepository = schemaRepository;
        this.apiParametersRepository = apiParametersRepository;
    }

    @Transactional
    //analyse et enregistrement du fichier Swagger
    //Prend en paramètre le contenu d’un fichier Swagger sous forme de String
    public String parseSwaggerFile(String fileContent) {
        try { //Ce bloc permet de gérer les exceptions qui pourraient survenir lors de l'analyse du fichier.
            //permettant d'analyser un fichier Swagger en un objet OpenAPI.
            OpenAPIV3Parser parser = new OpenAPIV3Parser(); //Utilise OpenAPIV3Parser pour lire le fichier Swagger et le convertir en un objet OpenAPI.
            OpenAPI openAPI = parser.readContents(fileContent, null, null).getOpenAPI(); //readContents analyse le contenu du fichier et retourne un objet SwaggerParseResult.,.getOpenAPI() extrait l'objet OpenAPI qui contient toutes les informations du fichier.
            String version = openAPI.getInfo().getVersion(); //Récupère la version de l'API depuis le fichier Swagger.,openAPI.getInfo() récupère les métadonnées de l'API.,.getVersion() retourne la version définie dans le fichier Swagger.
            //openAPI est une instance de io.swagger.v3.oas.models.OpenAPI, qui représente toute la documentation OpenAPI de l’application.
            //Info → Métadonnées de l’API (titre, description, version…) , Paths → Liste des routes , Components → Modèles de données réutilisables (schemas, security, etc.)
            Map<String, io.swagger.v3.oas.models.media.Schema> schemas = openAPI.getComponents().getSchemas();
            //openAPI.getComponents().getSchemas() retourne un dictionnaire (Map) de schémas définis dans le fichier Swagger.
            //schemas contient les structures de données utilisées par l'API.
            if (schemas != null) {
                for (Iterator<Map.Entry<String, io.swagger.v3.oas.models.media.Schema>> it = schemas.entrySet().iterator(); it.hasNext(); ) {
                    //schemas.entrySet().iterator() permet de parcourir les schémas un par un.
                    Map.Entry<String, io.swagger.v3.oas.models.media.Schema> entry = it.next();
                    String schemaName = entry.getKey(); //entry.getKey() récupère le nom du schéma.
                    io.swagger.v3.oas.models.media.Schema schemaValue = entry.getValue(); //entry.getValue() récupère les détails du schéma.
                    saveSchema(schemaName, schemaValue, openAPI); //fonction saveSchema : stocke id schema , nom_shema , contenu schema
                }
            }

            Map<String, PathItem> paths = openAPI.getPaths();
            //openAPI.getPaths() récupère un dictionnaire (Map) contenant les chemins (endpoints) de l'API.
            //Chaque clé du Map est un chemin (/users, /products, etc.).
            //Chaque valeur est un objet PathItem, contenant les opérations HTTP (GET, POST, etc.) associées au chemin.

            if (paths != null) { //parcourt chaque path de facon unique, paths contient tous les paths
                for (Iterator<Map.Entry<String, PathItem>> it = paths.entrySet().iterator(); it.hasNext(); ) { //it.hasNext() : Vérifie s’il reste encore des éléments à parcourir., map : dictionnaire
                    //schemas.entrySet().iterator() permet de parcourir les paths un par un.
                    Map.Entry<String, PathItem> pathEntry = it.next(); //entry : permet d'acceder au paire clé-valeur du dictionnaire map
                    String pathName = pathEntry.getKey(); //pathEntry.getKey() récupère le chemin
                    PathItem pathValue = pathEntry.getValue(); //pathEntry.getValue() récupère les opérations HTTP disponibles sur ce chemin.

                    //Pour chaque méthode trouvée, il appelle processOperation pour traiter les détails de l’opération.
                    if (pathValue.getGet() != null) {
                        processOperation(pathName, pathValue.getGet(), "GET", version, openAPI);
                    }
                    if (pathValue.getPost() != null) {
                        processOperation(pathName, pathValue.getPost(), "POST", version, openAPI);
                    }
                    if (pathValue.getPut() != null) {
                        processOperation(pathName, pathValue.getPut(), "PUT", version, openAPI);
                    }
                    if (pathValue.getDelete() != null) {
                        processOperation(pathName, pathValue.getDelete(), "DELETE", version, openAPI);
                    }
                    if (pathValue.getOptions() != null) {
                        processOperation(pathName, pathValue.getOptions(), "OPTIONS", version, openAPI);
                    }
                    if (pathValue.getHead() != null) {
                        processOperation(pathName, pathValue.getHead(), "HEAD", version, openAPI);
                    }
                    if (pathValue.getTrace() != null) {
                        processOperation(pathName, pathValue.getTrace(), "TRACE", version, openAPI);
                    }
                }
            }

            return "Fichier Swagger analysé et enregistré avec succès !";
        } catch (Exception e) {
            return "Erreur lors du traitement du fichier : " + e.getMessage();
        }
    }

    //Analyse une opération d’API et enregistre ses informations.
    private void processOperation(String path, Operation operation, String method, String version, OpenAPI openAPI) {
        if (operation != null) {
            Api api = new Api(); // Crée un objet Api contenant les informations de l’opération.
            api.setPath(path);
            api.setMethod(method);
            api.setDescription(operation.getDescription() != null ? operation.getDescription() : operation.getSummary());
            api.setVersion(version);

            // Extraction des tags
            List<String> tags = operation.getTags();
            api.setTags(tags != null ? String.join(", ", tags) : ""); //concaténer les éléments de la liste en une seule chaîne de caractères, séparés par ", ".

            // Extraction du requestBody
            RequestBody requestBody = operation.getRequestBody(); //récuperation de la partie requestbody
            if (requestBody != null) { // Vérification si le requestBody est présent
                Content content = requestBody.getContent(); //récupère les types de contenu
                if (content != null && content.containsKey("application/json")) { // Vérification du format application/json
                    MediaType mediaType = content.get("application/json"); // Récupère la structure attendue
                    io.swagger.v3.oas.models.media.Schema schema = mediaType.getSchema(); // Récupère la référence du schéma

                    if (schema != null) { // Si le schéma est disponible, on extrait son contenu
                        String requestBodyContent = extractRequestBodyContent(schema, openAPI); // Fonction extractRequestBodyContent : contenu requestbody , on cherche la partie schema dans openapi
                        api.setRequest_body(requestBodyContent); // stocke le requestbody dans la table api

                        // Vérifie si le schema_name existe et n'est pas vide avant de le stocker
                        String schemaName = extractSchemaRef(schema);
                        if (schemaName != null && !schemaName.isEmpty()) {
                            api.setSchema_name(schemaName); // Enregistre le nom du schéma uniquement s'il est présent dans la table api
                        }
                    }
                }
            }

            // Enregistrement de l'API
            Api savedApi = apiRepository.save(api);

            // Extraction des paramètres
            List<Parameter> parameters = operation.getParameters(); //parameters contient tous les paramètres
            if (parameters != null) {
                for (Parameter parameter : parameters) { // On parcourt les paramètres un par un
                    ApiParameters apiParameters = new ApiParameters();
                    apiParameters.setApi(savedApi);
                    apiParameters.setName(parameter.getName());

                    if (parameter.getIn() != null) {
                        apiParameters.setTypein(parameter.getIn().toString());
                    }

                    if (parameter.getExample() != null) {
                        try {
                            apiParameters.setExample(parameter.getExample().toString());
                        } catch (Exception e) {
                            System.out.println("Erreur lors de la récupération de l'exemple pour le paramètre " + parameter.getName());
                        }
                    } else {
                        apiParameters.setExample("Aucun exemple disponible");
                    }

                    if (parameter.getSchema() != null) { //data type
                        apiParameters.setData_type(parameter.getSchema().getType());
                    }

                    if (parameter.getDescription() != null) {
                        apiParameters.setDescription(parameter.getDescription());
                    }

                    apiParameters.setRequired(parameter.getRequired());

                    apiParametersRepository.save(apiParameters);
                }
            }

            // Enregistrement des réponses
            if (operation.getResponses() != null) {
                for (Iterator<Map.Entry<String, io.swagger.v3.oas.models.responses.ApiResponse>> it = operation.getResponses().entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry<String, io.swagger.v3.oas.models.responses.ApiResponse> entry = it.next(); // récupère les clés-valeurs
                    String status = entry.getKey(); // récupère le status
                    io.swagger.v3.oas.models.responses.ApiResponse responseValue = entry.getValue(); // récupère la valeur contenant les détails
                    saveApiResponse(status, responseValue, savedApi, openAPI); // Appel de la fonction ApiResponse : stocke le status, id api, nom_schema_ref, id schema, contenu schema
                }
            }
        }
    }



    private void saveApiResponse(String status, io.swagger.v3.oas.models.responses.ApiResponse response, Api api, OpenAPI openAPI) {
        //status → Code HTTP de la réponse (200, 400, 500, etc.).
        //response → Objet OpenAPI représentant la réponse de l'API.
        //api → Objet API concerné (entité liée à l'endpoint).
        //openAPI → L'objet principal contenant la documentation OpenAPI.
        com.example.api_tierces.model.ApiResponse apiResponse = new com.example.api_tierces.model.ApiResponse(); //On crée une nouvelle instance de ApiResponse qui sera enregistrée en base.
        apiResponse.setApi(api);
        apiResponse.setStatus(status);

        if (response != null) { //response contient plusieurs informations : description, content ...
            apiResponse.setDescription(response.getDescription()); //On extrait la description de la réponse OpenAPI et on l'enregistre.

            if (response.getContent() != null && response.getContent().containsKey("application/json")) {
                //On vérifie si la réponse contient un corps (response.getContent()).
                //On s’assure que ce contenu est bien au format "application/json".
                MediaType mediaType = response.getContent().get("application/json"); //MediaType provient de io.swagger.v3.oas.models.media.MediaType. Son rôle est de décrire le type de contenu qu'une API peut envoyer ou recevoir dans une requête ou une réponse.
                //response.getContent() : Vérifie si la réponse contient un corps (body).
                io.swagger.v3.oas.models.media.Schema schema = mediaType.getSchema(); //schema contient ceci : #/components/schemas/CustomerDetailsResponseBody

                if (schema != null) {
                    String schemaNameRef = extractSchemaRef(schema); //fonction extractSchemaRef (nom schema)

                    List<Schema> existingSchemas = schemaRepository.findByName(schemaNameRef); //On recherche dans la base de données si un schéma avec ce nom existe déjà.
                    Schema existingSchema = existingSchemas != null && !existingSchemas.isEmpty() ? existingSchemas.get(0) : null; //pour chercher l'id du schema dans la table schema

                    apiResponse.setName_schema(schemaNameRef); // Stocke uniquement le nom du schéma

                    if (existingSchema != null) {
                        apiResponse.setSchema(existingSchema); // Associe l'entité Schema si elle existe
                    }
                }
            }
        }

        apiResponseRepository.save(apiResponse);
    }

    private void saveSchema(String name, io.swagger.v3.oas.models.media.Schema schema, OpenAPI openAPI) { //vérifie si un schéma existe dans la table de données avec le meme nom ou non
        //openAPI : Objet OpenAPI contenant tous les composants.
        List<com.example.api_tierces.model.Schema> existingSchemas = schemaRepository.findByName(name); //Rechercher dans la base de données si un schéma avec le même nom existe déjà.
        if (existingSchemas == null || existingSchemas.isEmpty()) {
            com.example.api_tierces.model.Schema newSchema = new com.example.api_tierces.model.Schema(); // Créer un nouvel objet Schema
            newSchema.setName(name);
            newSchema.setType(schema.getType());

            try {
                newSchema.setSchemas(Json.pretty(schema)); // Tentative de conversion de l'objet schema en chaîne JSON formatée
            } catch (Exception e) {
                newSchema.setSchemas("Erreur lors de la sérialisation du schema: " + e.getMessage());
            }
            schemaRepository.save(newSchema);
        }
    }

    private String extractRequestBodyContent(io.swagger.v3.oas.models.media.Schema schema, OpenAPI openAPI) {
        try {
            if (schema.get$ref() != null) { //si schema contient une reference
                String ref = schema.get$ref();
                String refName = ref.replace("#/components/schemas/", "");
                Map<String, io.swagger.v3.oas.models.media.Schema> schemas = openAPI.getComponents().getSchemas();
                //Récupère tous les schémas définis dans components/schemas sous forme d’une Map (nom → objet Schema).
                //retourne tous les schémas définis dans OpenAPI.

                if (schemas != null && schemas.containsKey(refName)) {
                    io.swagger.v3.oas.models.media.Schema resolvedSchema = schemas.get(refName);
                    return Json.pretty(resolvedSchema); //On le convertit en format JSON lisible
                } else {
                    return "Schema reference not found: " + ref;
                }
            } else { // si schema ne contient pas de reference
                return Json.pretty(schema); //On le convertit en format JSON lisible
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la sérialisation du schéma : " + e.getMessage());
            return "Erreur de sérialisation";
        }

    }

    private String extractSchemaRef(io.swagger.v3.oas.models.media.Schema schema) {
        //io.swagger.v3.oas.models.media.Schema : représentant un schéma OpenAPI
        if (schema.get$ref() != null) {
            return schema.get$ref().replace("#/components/schemas/", ""); //.replace("#/components/schemas/", "") enlève cette partie, retourne seulement la référence
            //get$ref() retourne cette référence si elle existe, sinon null.
        }
        return null;
    }
}*/
/*package com.example.api_tierces.service;

import com.example.api_tierces.model.*;
import com.example.api_tierces.repository.*;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.springframework.stereotype.Service;
import io.swagger.v3.oas.models.parameters.RequestBody;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

@Service
public class FileUploadService implements UploadService {

    private final ApiRepository apiRepository;
    private final ApiResponseRepository apiResponseRepository;
    private final SchemaRepository schemaRepository;
    private final ApiParametersRepository apiParametersRepository;

    public FileUploadService(ApiRepository apiRepository,
                             ApiResponseRepository apiResponseRepository,
                             SchemaRepository schemaRepository,
                             ApiParametersRepository apiParametersRepository) {
        this.apiRepository = apiRepository;
        this.apiResponseRepository = apiResponseRepository;
        this.schemaRepository = schemaRepository;
        this.apiParametersRepository = apiParametersRepository;
    }

    @Transactional
    public String parseSwaggerFileFromUrl(String swaggerUrl) {
        try {
            String fileContent = downloadFileContent(swaggerUrl);
            return parseSwaggerFile(fileContent);
        } catch (IOException e) {
            return "Erreur lors du téléchargement du fichier depuis l'URL : " + e.getMessage();
        }
    }

    @Transactional
    public String parseSwaggerFile(String fileContent) {
        try {
            OpenAPIV3Parser parser = new OpenAPIV3Parser();
            OpenAPI openAPI = parser.readContents(fileContent, null, null).getOpenAPI();
            String version = openAPI.getInfo().getVersion();

            Map<String, io.swagger.v3.oas.models.media.Schema> schemas = openAPI.getComponents().getSchemas();
            if (schemas != null) {
                for (Iterator<Map.Entry<String, io.swagger.v3.oas.models.media.Schema>> it = schemas.entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry<String, io.swagger.v3.oas.models.media.Schema> entry = it.next();
                    String schemaName = entry.getKey();
                    io.swagger.v3.oas.models.media.Schema schemaValue = entry.getValue();
                    saveSchema(schemaName, schemaValue, openAPI);
                }
            }

            Map<String, PathItem> paths = openAPI.getPaths();
            if (paths != null) {
                for (Iterator<Map.Entry<String, PathItem>> it = paths.entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry<String, PathItem> pathEntry = it.next();
                    String pathName = pathEntry.getKey();
                    PathItem pathValue = pathEntry.getValue();

                    if (pathValue.getGet() != null) {
                        processOperation(pathName, pathValue.getGet(), "GET", version, openAPI);
                    }
                    if (pathValue.getPost() != null) {
                        processOperation(pathName, pathValue.getPost(), "POST", version, openAPI);
                    }
                    if (pathValue.getPut() != null) {
                        processOperation(pathName, pathValue.getPut(), "PUT", version, openAPI);
                    }
                    if (pathValue.getDelete() != null) {
                        processOperation(pathName, pathValue.getDelete(), "DELETE", version, openAPI);
                    }
                    if (pathValue.getOptions() != null) {
                        processOperation(pathName, pathValue.getOptions(), "OPTIONS", version, openAPI);
                    }
                    if (pathValue.getHead() != null) {
                        processOperation(pathName, pathValue.getHead(), "HEAD", version, openAPI);
                    }
                    if (pathValue.getTrace() != null) {
                        processOperation(pathName, pathValue.getTrace(), "TRACE", version, openAPI);
                    }
                }
            }

            return "Fichier Swagger analysé et enregistré avec succès !";
        } catch (Exception e) {
            return "Erreur lors du traitement du fichier : " + e.getMessage();
        }
    }

    private void processOperation(String path, Operation operation, String method, String version, OpenAPI openAPI) {
        if (operation != null) {
            Api api = new Api();
            api.setPath(path);
            api.setMethod(method);
            api.setDescription(operation.getDescription() != null ? operation.getDescription() : operation.getSummary());
            api.setVersion(version);

            List<String> tags = operation.getTags();
            api.setTags(tags != null ? String.join(", ", tags) : "");

            RequestBody requestBody = operation.getRequestBody();
            if (requestBody != null) {
                Content content = requestBody.getContent();
                if (content != null && content.containsKey("application/json")) {
                    MediaType mediaType = content.get("application/json");
                    io.swagger.v3.oas.models.media.Schema schema = mediaType.getSchema();

                    if (schema != null) {
                        String requestBodyContent = extractRequestBodyContent(schema, openAPI);
                        api.setRequest_body(requestBodyContent);

                        String schemaName = extractSchemaRef(schema);
                        if (schemaName != null && !schemaName.isEmpty()) {
                            api.setSchema_name(schemaName);
                        }
                    }
                }
            }

            Api savedApi = apiRepository.save(api);

            List<Parameter> parameters = operation.getParameters();
            if (parameters != null) {
                for (Parameter parameter : parameters) {
                    ApiParameters apiParameters = new ApiParameters();
                    apiParameters.setApi(savedApi);
                    apiParameters.setName(parameter.getName());

                    if (parameter.getIn() != null) {
                        apiParameters.setTypein(parameter.getIn().toString());
                    }

                    if (parameter.getExample() != null) {
                        try {
                            apiParameters.setExample(parameter.getExample().toString());
                        } catch (Exception e) {
                            System.out.println("Erreur lors de la récupération de l'exemple pour le paramètre " + parameter.getName());
                        }
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

                    apiParametersRepository.save(apiParameters);
                }
            }

            if (operation.getResponses() != null) {
                for (Iterator<Map.Entry<String, io.swagger.v3.oas.models.responses.ApiResponse>> it = operation.getResponses().entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry<String, io.swagger.v3.oas.models.responses.ApiResponse> entry = it.next();
                    String status = entry.getKey();
                    io.swagger.v3.oas.models.responses.ApiResponse responseValue = entry.getValue();
                    saveApiResponse(status, responseValue, savedApi, openAPI);
                }
            }
        }
    }

    private void saveApiResponse(String status, io.swagger.v3.oas.models.responses.ApiResponse response, Api api, OpenAPI openAPI) {
        com.example.api_tierces.model.ApiResponse apiResponse = new com.example.api_tierces.model.ApiResponse();
        apiResponse.setApi(api);
        apiResponse.setStatus(status);

        if (response != null) {
            apiResponse.setDescription(response.getDescription());

            if (response.getContent() != null && response.getContent().containsKey("application/json")) {
                MediaType mediaType = response.getContent().get("application/json");
                io.swagger.v3.oas.models.media.Schema schema = mediaType.getSchema();

                if (schema != null) {
                    String schemaNameRef = extractSchemaRef(schema);

                    List<Schema> existingSchemas = schemaRepository.findByName(schemaNameRef);
                    Schema existingSchema = existingSchemas != null && !existingSchemas.isEmpty() ? existingSchemas.get(0) : null;

                    apiResponse.setName_schema(schemaNameRef);

                    if (existingSchema != null) {
                        apiResponse.setSchema(existingSchema);
                    }
                }
            }
        }

        apiResponseRepository.save(apiResponse);
    }

    private void saveSchema(String name, io.swagger.v3.oas.models.media.Schema schema, OpenAPI openAPI) {
        List<com.example.api_tierces.model.Schema> existingSchemas = schemaRepository.findByName(name);
        if (existingSchemas == null || existingSchemas.isEmpty()) {
            com.example.api_tierces.model.Schema newSchema = new com.example.api_tierces.model.Schema();
            newSchema.setName(name);
            newSchema.setType(schema.getType());

            try {
                newSchema.setSchemas(Json.pretty(schema));
            } catch (Exception e) {
                newSchema.setSchemas("Erreur lors de la sérialisation du schema: " + e.getMessage());
            }
            schemaRepository.save(newSchema);
        }
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
            System.err.println("Erreur lors de la sérialisation du schéma : " + e.getMessage());
            return "Erreur de sérialisation";
        }

    }

    private String extractSchemaRef(io.swagger.v3.oas.models.media.Schema schema) {
        if (schema.get$ref() != null) {
            return schema.get$ref().replace("#/components/schemas/", "");
        }
        return null;
    }

    private String downloadFileContent(String swaggerUrl) throws IOException {
        StringBuilder content = new StringBuilder();
        URL url = new URL(swaggerUrl);
        URLConnection connection = url.openConnection();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

}*/
/*adheyaaaaaaaa shihhhhhhhhhhhhhhhh
package com.example.api_tierces.service;

import com.example.api_tierces.model.*;
import com.example.api_tierces.repository.*;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.springframework.stereotype.Service;
import io.swagger.v3.oas.models.parameters.RequestBody;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.models.responses.ApiResponse;

import java.util.*;

@Service
public class FileUploadService implements UploadService {

    private final ApiRepository apiRepository;
    private final ApiResponseRepository apiResponseRepository;
    private final SchemaRepository schemaRepository;
    private final ApiParametersRepository apiParametersRepository;

    public FileUploadService(ApiRepository apiRepository,
                             ApiResponseRepository apiResponseRepository,
                             SchemaRepository schemaRepository,
                             ApiParametersRepository apiParametersRepository) {
        this.apiRepository = apiRepository;
        this.apiResponseRepository = apiResponseRepository;
        this.schemaRepository = schemaRepository;
        this.apiParametersRepository = apiParametersRepository;
    }

    @Transactional
    public String parseSwaggerFileFromUrl(String fileContent) {
        try {
            //String fileContent = downloadFileContent(swaggerUrl);
            return parseSwaggerFile(fileContent);
        }catch (Exception e) {
            return "Erreur lors du traitement du fichier Swagger : " + e.getMessage();
        }
    }

    @Transactional
    public String parseSwaggerFile(String fileContent) {
        try {
            OpenAPIV3Parser parser = new OpenAPIV3Parser();
            OpenAPI openAPI = parser.readContents(fileContent, null, null).getOpenAPI();
            String version = openAPI.getInfo().getVersion();

            // Gestion des schémas
            Map<String, io.swagger.v3.oas.models.media.Schema> schemas = openAPI.getComponents().getSchemas();
            Set<String> currentSchemaNames = new HashSet<>();

            if (schemas != null) {
                for (Map.Entry<String, io.swagger.v3.oas.models.media.Schema> entry : schemas.entrySet()) {
                    String schemaName = entry.getKey();
                    io.swagger.v3.oas.models.media.Schema schemaValue = entry.getValue();
                    saveSchema(schemaName, schemaValue, openAPI);
                    currentSchemaNames.add(schemaName);
                }
            }

            // Supprimer les schémas obsolètes
            deleteObsoleteSchemas(currentSchemaNames);

            // Gestion des chemins (API)
            Map<String, PathItem> paths = openAPI.getPaths();
            Set<String> currentPaths = new HashSet<>();

            if (paths != null) {
                for (Map.Entry<String, PathItem> pathEntry : paths.entrySet()) {
                    String pathName = pathEntry.getKey();
                    PathItem pathValue = pathEntry.getValue();

                    currentPaths.add(pathName);

                    if (pathValue.getGet() != null) {
                        processOperation(pathName, pathValue.getGet(), "GET", version, openAPI);
                    }
                    if (pathValue.getPost() != null) {
                        processOperation(pathName, pathValue.getPost(), "POST", version, openAPI);
                    }
                    if (pathValue.getPut() != null) {
                        processOperation(pathName, pathValue.getPut(), "PUT", version, openAPI);
                    }
                    if (pathValue.getDelete() != null) {
                        processOperation(pathName, pathValue.getDelete(), "DELETE", version, openAPI);
                    }
                    if (pathValue.getOptions() != null) {
                        processOperation(pathName, pathValue.getOptions(), "OPTIONS", version, openAPI);
                    }
                    if (pathValue.getHead() != null) {
                        processOperation(pathName, pathValue.getHead(), "HEAD", version, openAPI);
                    }
                    if (pathValue.getTrace() != null) {
                        processOperation(pathName, pathValue.getTrace(), "TRACE", version, openAPI);
                    }
                }
            }

            // Supprimer les API obsolètes
            deleteObsoleteApis(currentPaths);

            return "Fichier Swagger analysé et enregistré avec succès !";
        } catch (Exception e) {
            return "Erreur lors du traitement du fichier : " + e.getMessage();
        }
    }


    private void processOperation(String path, Operation operation, String method, String version, OpenAPI openAPI) {
        if (operation != null) {
            // Vérifier si l'API existe déjà
            Api existingApi = apiRepository.findByPathAndMethod(path, method);

            Api api;
            if (existingApi != null) {
                // Mettre à jour l'API existante
                api = existingApi;
                api.setDescription(operation.getDescription() != null ? operation.getDescription() : operation.getSummary());
                api.setVersion(version);
            } else {
                // Créer une nouvelle API
                api = new Api();
                api.setPath(path);
                api.setMethod(method);
                api.setDescription(operation.getDescription() != null ? operation.getDescription() : operation.getSummary());
                api.setVersion(version);
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
                        api.setRequest_body(requestBodyContent);

                        // Extraire et enregistrer le nom du schéma
                        String schemaName = extractSchemaRef(schema);
                        if (schemaName != null && !schemaName.isEmpty()) {
                            api.setSchema_name(schemaName);
                        }
                    }
                }
            }

            // Sauvegarder ou mettre à jour l'API
            Api savedApi = apiRepository.save(api);

            // Gestion des paramètres
            List<Parameter> parameters = operation.getParameters();
            if (parameters != null) {
                for (Parameter parameter : parameters) {
                    ApiParameters existingParameter = apiParametersRepository.findByApiAndName(savedApi, parameter.getName());

                    ApiParameters apiParameters;
                    if (existingParameter != null) {
                        // Mettre à jour le paramètre existant
                        apiParameters = existingParameter;
                    } else {
                        // Créer un nouveau paramètre
                        apiParameters = new ApiParameters();
                        apiParameters.setApi(savedApi);
                        apiParameters.setName(parameter.getName());
                    }

                    // Mettre à jour les champs du paramètre
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

                    // Sauvegarder ou mettre à jour le paramètre
                    apiParametersRepository.save(apiParameters);
                }
            }

            // Gestion des réponses
            if (operation.getResponses() != null) {
                Set<String> currentStatuses = new HashSet<>();

                for (Map.Entry<String, ApiResponse> entry : operation.getResponses().entrySet()) {
                    String status = entry.getKey();
                    ApiResponse responseValue = entry.getValue();

                    // Ajouter le statut à la liste des statuts actuels
                    currentStatuses.add(status);

                    // Sauvegarder ou mettre à jour la réponse
                    saveApiResponse(status, responseValue, savedApi, openAPI);
                }

                // Supprimer les réponses obsolètes
                deleteObsoleteResponses(savedApi, currentStatuses);
            }
        }
    }

    private void saveApiResponse(String status, ApiResponse response, Api api, OpenAPI openAPI) {
        // Vérifier si la réponse existe déjà pour cette API
        com.example.api_tierces.model.ApiResponse existingResponse = apiResponseRepository.findByApiAndStatus(api, status);

        if (existingResponse == null) {
            // Si la réponse n'existe pas, on la crée
            com.example.api_tierces.model.ApiResponse apiResponse = new com.example.api_tierces.model.ApiResponse();
            apiResponse.setApi(api);
            apiResponse.setStatus(status);

            if (response != null) {
                apiResponse.setDescription(response.getDescription());

                if (response.getContent() != null && response.getContent().containsKey("application/json")) {
                    MediaType mediaType = response.getContent().get("application/json");
                    io.swagger.v3.oas.models.media.Schema schema = mediaType.getSchema();

                    if (schema != null) {
                        String schemaNameRef = extractSchemaRef(schema);

                        List<Schema> existingSchemas = schemaRepository.findByName(schemaNameRef);
                        Schema existingSchema = existingSchemas != null && !existingSchemas.isEmpty() ? existingSchemas.get(0) : null;

                        apiResponse.setName_schema(schemaNameRef);

                        if (existingSchema != null) {
                            apiResponse.setSchema(existingSchema);
                        }
                    }
                }
            }

            // Sauvegarder la nouvelle réponse
            apiResponseRepository.save(apiResponse);
        }
    }

    private void saveSchema(String name, io.swagger.v3.oas.models.media.Schema schema, OpenAPI openAPI) {
        List<com.example.api_tierces.model.Schema> existingSchemas = schemaRepository.findByName(name);
        com.example.api_tierces.model.Schema schemaToSave;

        if (existingSchemas != null && !existingSchemas.isEmpty()) {
            // Mettre à jour le schéma existant
            schemaToSave = existingSchemas.get(0);
        } else {
            // Créer un nouveau schéma
            schemaToSave = new com.example.api_tierces.model.Schema();
            schemaToSave.setName(name);
        }

        schemaToSave.setType(schema.getType());

        try {
            schemaToSave.setSchemas(Json.pretty(schema));
        } catch (Exception e) {
            schemaToSave.setSchemas("Erreur lors de la sérialisation du schema: " + e.getMessage());
        }

        schemaRepository.save(schemaToSave);
    }

    private void deleteObsoleteApis(Set<String> currentPaths) {
        // Récupérer toutes les API existantes
        List<Api> existingApis = apiRepository.findAll();

        for (Api existingApi : existingApis) {
            // Si le chemin de l'API n'est pas dans les chemins actuels, on la supprime
            if (!currentPaths.contains(existingApi.getPath())) {
                apiRepository.delete(existingApi);
            }
        }
    }

    private void deleteObsoleteSchemas(Set<String> currentSchemaNames) {
        // Récupérer tous les schémas existants
        List<com.example.api_tierces.model.Schema> existingSchemas = schemaRepository.findAll();

        for (com.example.api_tierces.model.Schema existingSchema : existingSchemas) {
            // Si le nom du schéma n'est pas dans les noms actuels, on le supprime
            if (!currentSchemaNames.contains(existingSchema.getName())) {
                schemaRepository.delete(existingSchema);
            }
        }
    }

    private void deleteObsoleteResponses(Api api, Set<String> currentStatuses) {
        // Récupérer toutes les réponses associées à cette API
        List<com.example.api_tierces.model.ApiResponse> existingResponses = apiResponseRepository.findByApi(api);

        for (com.example.api_tierces.model.ApiResponse existingResponse : existingResponses) {
            // Si le statut de la réponse n'est pas dans les statuts actuels, on la supprime
            if (!currentStatuses.contains(existingResponse.getStatus())) {
                apiResponseRepository.delete(existingResponse);
            }
        }
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
            System.err.println("Erreur lors de la sérialisation du schéma : " + e.getMessage());
            return "Erreur de sérialisation";
        }
    }

    private String extractSchemaRef(io.swagger.v3.oas.models.media.Schema schema) {
        if (schema.get$ref() != null) {
            return schema.get$ref().replace("#/components/schemas/", "");
        }
        return null;
    }

    private String downloadFileContent(String swaggerUrl) throws IOException {
        StringBuilder content = new StringBuilder();
        URL url = new URL(swaggerUrl);
        URLConnection connection = url.openConnection();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }
}*/
//shih l code hedha
/*@Service
public class FileUploadService implements UploadService {

    private final ApiRepository apiRepository;
    private final ApiResponseRepository apiResponseRepository;
    private final SchemaRepository schemaRepository;
    private final ApiParametersRepository apiParametersRepository;

    public FileUploadService(ApiRepository apiRepository,
                             ApiResponseRepository apiResponseRepository,
                             SchemaRepository schemaRepository,
                             ApiParametersRepository apiParametersRepository) {
        this.apiRepository = apiRepository;
        this.apiResponseRepository = apiResponseRepository;
        this.schemaRepository = schemaRepository;
        this.apiParametersRepository = apiParametersRepository;
    }

    @Transactional
    public String parseSwaggerFileFromUrl(String fileContent) {
        try {
            return parseSwaggerFile(fileContent);
        } catch (Exception e) {
            return "Erreur lors du traitement du fichier Swagger : " + e.getMessage();
        }
    }

    @Transactional
    public String parseSwaggerFile(String fileContent) {
        boolean hasChanges = false; // Indicateur de changement global

        try {
            OpenAPIV3Parser parser = new OpenAPIV3Parser();
            OpenAPI openAPI = parser.readContents(fileContent, null, null).getOpenAPI();
            String version = openAPI.getInfo().getVersion();

            // Gestion des schémas
            Map<String, io.swagger.v3.oas.models.media.Schema> schemas = openAPI.getComponents().getSchemas();
            Set<String> currentSchemaNames = new HashSet<>();

            if (schemas != null) {
                for (Map.Entry<String, io.swagger.v3.oas.models.media.Schema> entry : schemas.entrySet()) {
                    String schemaName = entry.getKey();
                    io.swagger.v3.oas.models.media.Schema schemaValue = entry.getValue();
                    if (saveSchema(schemaName, schemaValue, openAPI)) {
                        hasChanges = true; // Changement détecté
                    }
                    currentSchemaNames.add(schemaName);
                }
            }

            // Supprimer les schémas obsolètes
            if (deleteObsoleteSchemas(currentSchemaNames)) {
                hasChanges = true; // Changement détecté
            }

            // Gestion des chemins (API)
            Map<String, PathItem> paths = openAPI.getPaths();
            Set<String> currentPaths = new HashSet<>();

            if (paths != null) {
                for (Map.Entry<String, PathItem> pathEntry : paths.entrySet()) {
                    String pathName = pathEntry.getKey();
                    PathItem pathValue = pathEntry.getValue();

                    currentPaths.add(pathName);

                    if (pathValue.getGet() != null && processOperation(pathName, pathValue.getGet(), "GET", version, openAPI)) {
                        hasChanges = true; // Changement détecté
                    }
                    if (pathValue.getPost() != null && processOperation(pathName, pathValue.getPost(), "POST", version, openAPI)) {
                        hasChanges = true; // Changement détecté
                    }
                    if (pathValue.getPut() != null && processOperation(pathName, pathValue.getPut(), "PUT", version, openAPI)) {
                        hasChanges = true; // Changement détecté
                    }
                    if (pathValue.getDelete() != null && processOperation(pathName, pathValue.getDelete(), "DELETE", version, openAPI)) {
                        hasChanges = true; // Changement détecté
                    }
                    if (pathValue.getOptions() != null && processOperation(pathName, pathValue.getOptions(), "OPTIONS", version, openAPI)) {
                        hasChanges = true; // Changement détecté
                    }
                    if (pathValue.getHead() != null && processOperation(pathName, pathValue.getHead(), "HEAD", version, openAPI)) {
                        hasChanges = true; // Changement détecté
                    }
                    if (pathValue.getTrace() != null && processOperation(pathName, pathValue.getTrace(), "TRACE", version, openAPI)) {
                        hasChanges = true; // Changement détecté
                    }
                }
            }

            // Supprimer les API obsolètes
            if (deleteObsoleteApis(currentPaths)) {
                hasChanges = true; // Changement détecté
            }

            // Afficher le message en fonction de hasChanges
            if (!hasChanges) {
                logChanges("Aucun changement réalisé.");
            }

            return "Fichier Swagger analysé et enregistré avec succès !";
        } catch (Exception e) {
            return "Erreur lors du traitement du fichier : " + e.getMessage();
        }
    }

    private void logChanges(String message) {
        // Vous pouvez utiliser un logger ici ou simplement afficher le message dans la console
        System.out.println(message);
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

    private boolean saveSchema(String name, io.swagger.v3.oas.models.media.Schema schema, OpenAPI openAPI) {
        List<com.example.api_tierces.model.Schema> existingSchemas = schemaRepository.findByName(name);
        com.example.api_tierces.model.Schema schemaToSave;
        boolean hasChanged = false;

        if (existingSchemas != null && !existingSchemas.isEmpty()) {
            // Mettre à jour le schéma existant
            schemaToSave = existingSchemas.get(0);

            // Vérifier si les valeurs ont changé
            if (hasChanged(schemaToSave.getType(), schema.getType())) {
                schemaToSave.setType(schema.getType());
                hasChanged = true;
            }

            String newSchemaContent = Json.pretty(schema);
            if (hasChanged(schemaToSave.getSchemas(), newSchemaContent)) {
                schemaToSave.setSchemas(newSchemaContent);
                hasChanged = true;
            }

            if (hasChanged) {
                logChanges("Mise à jour du schéma: " + name);
                schemaRepository.save(schemaToSave);
            }
        } else {
            // Créer un nouveau schéma
            schemaToSave = new com.example.api_tierces.model.Schema();
            schemaToSave.setName(name);
            schemaToSave.setType(schema.getType());
            schemaToSave.setSchemas(Json.pretty(schema));
            logChanges("Ajout d'un nouveau schéma: " + name);
            schemaRepository.save(schemaToSave);
            hasChanged = true;
        }

        return hasChanged;
    }

    private boolean deleteObsoleteSchemas(Set<String> currentSchemaNames) {
        boolean hasChanged = false;
        List<com.example.api_tierces.model.Schema> existingSchemas = schemaRepository.findAll();

        for (com.example.api_tierces.model.Schema existingSchema : existingSchemas) {
            if (!currentSchemaNames.contains(existingSchema.getName())) {
                logChanges("Suppression du schéma obsolète: " + existingSchema.getName());
                schemaRepository.delete(existingSchema);
                hasChanged = true;
            }
        }

        return hasChanged;
    }

    private boolean processOperation(String path, Operation operation, String method, String version, OpenAPI openAPI) {
        boolean hasChanged = false;

        if (operation != null) {
            Api existingApi = apiRepository.findByPathAndMethod(path, method);
            Api api;

            if (existingApi != null) {
                // Mettre à jour l'API existante
                api = existingApi;

                // Vérifier si les valeurs ont changé
                String newDescription = operation.getDescription() != null ? operation.getDescription() : operation.getSummary();
                if (hasChanged(api.getDescription(), newDescription)) {
                    api.setDescription(newDescription);
                    hasChanged = true;
                }
                if (hasChanged(api.getVersion(), version)) {
                    api.setVersion(version);
                    hasChanged = true;
                }

                if (hasChanged) {
                    logChanges("Mise à jour de l'API: " + path + " (" + method + ")");
                    apiRepository.save(api);
                }
            } else {
                // Créer une nouvelle API
                api = new Api();
                api.setPath(path);
                api.setMethod(method);
                api.setDescription(operation.getDescription() != null ? operation.getDescription() : operation.getSummary());
                api.setVersion(version);
                logChanges("Ajout d'une nouvelle API: " + path + " (" + method + ")");
                apiRepository.save(api);
                hasChanged = true;
            }

            // Gestion du request body, paramètres et réponses (même logique)
        }

        return hasChanged;
    }

    private boolean deleteObsoleteApis(Set<String> currentPaths) {
        boolean hasChanged = false;
        List<Api> existingApis = apiRepository.findAll();

        for (Api existingApi : existingApis) {
            if (!currentPaths.contains(existingApi.getPath())) {
                logChanges("Suppression de l'API obsolète: " + existingApi.getPath() + " (" + existingApi.getMethod() + ")");
                apiRepository.delete(existingApi);
                hasChanged = true;
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
            System.err.println("Erreur lors de la sérialisation du schéma : " + e.getMessage());
            return "Erreur de sérialisation";
        }
    }

    private String extractSchemaRef(io.swagger.v3.oas.models.media.Schema schema) {
        if (schema.get$ref() != null) {
            return schema.get$ref().replace("#/components/schemas/", "");
        }
        return null;
    }
}*/
//Code hedhaaaaa shihhhhhh yextati partie request_body w schema blech aka l patie properties
/*package com.example.api_tierces.service;

import com.example.api_tierces.model.*;
import com.example.api_tierces.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.springframework.stereotype.Service;
import io.swagger.v3.oas.models.parameters.RequestBody;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.models.responses.ApiResponse;

import java.util.*;

@Service
public class FileUploadService implements UploadService {

    private final ApiRepository apiRepository;
    private final ApiResponseRepository apiResponseRepository;
    private final SchemaRepository schemaRepository;
    private final ApiParametersRepository apiParametersRepository;
    private final ObjectMapper objectMapper; // Ajout de ObjectMapper

    public FileUploadService(ApiRepository apiRepository,
                             ApiResponseRepository apiResponseRepository,
                             SchemaRepository schemaRepository,
                             ApiParametersRepository apiParametersRepository,
                             ObjectMapper objectMapper) { //Injection de  ObjectMapper
        this.apiRepository = apiRepository;
        this.apiResponseRepository = apiResponseRepository;
        this.schemaRepository = schemaRepository;
        this.apiParametersRepository = apiParametersRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public String parseSwaggerFileFromUrl(String fileContent) {
        try {
            //String fileContent = downloadFileContent(swaggerUrl);
            return parseSwaggerFile(fileContent);
        }catch (Exception e) {
            return "Erreur lors du traitement du fichier Swagger : " + e.getMessage();
        }
    }

    @Transactional
    public String parseSwaggerFile(String fileContent) {
        try {
            OpenAPIV3Parser parser = new OpenAPIV3Parser();
            OpenAPI openAPI = parser.readContents(fileContent, null, null).getOpenAPI();
            String version = openAPI.getInfo().getVersion();

            // Gestion des schémas
            Map<String, io.swagger.v3.oas.models.media.Schema> schemas = openAPI.getComponents().getSchemas();
            Set<String> currentSchemaNames = new HashSet<>();

            if (schemas != null) {
                for (Map.Entry<String, io.swagger.v3.oas.models.media.Schema> entry : schemas.entrySet()) {
                    String schemaName = entry.getKey();
                    io.swagger.v3.oas.models.media.Schema schemaValue = entry.getValue();
                    saveSchema(schemaName, schemaValue, openAPI);
                    currentSchemaNames.add(schemaName);
                }
            }

            // Supprimer les schémas obsolètes
            deleteObsoleteSchemas(currentSchemaNames);

            // Gestion des chemins (API)
            Map<String, PathItem> paths = openAPI.getPaths();
            Set<String> currentPaths = new HashSet<>();

            if (paths != null) {
                for (Map.Entry<String, PathItem> pathEntry : paths.entrySet()) {
                    String pathName = pathEntry.getKey();
                    PathItem pathValue = pathEntry.getValue();

                    currentPaths.add(pathName);

                    if (pathValue.getGet() != null) {
                        processOperation(pathName, pathValue.getGet(), "GET", version, openAPI);
                    }
                    if (pathValue.getPost() != null) {
                        processOperation(pathName, pathValue.getPost(), "POST", version, openAPI);
                    }
                    if (pathValue.getPut() != null) {
                        processOperation(pathName, pathValue.getPut(), "PUT", version, openAPI);
                    }
                    if (pathValue.getDelete() != null) {
                        processOperation(pathName, pathValue.getDelete(), "DELETE", version, openAPI);
                    }
                    if (pathValue.getOptions() != null) {
                        processOperation(pathName, pathValue.getOptions(), "OPTIONS", version, openAPI);
                    }
                    if (pathValue.getHead() != null) {
                        processOperation(pathName, pathValue.getHead(), "HEAD", version, openAPI);
                    }
                    if (pathValue.getTrace() != null) {
                        processOperation(pathName, pathValue.getTrace(), "TRACE", version, openAPI);
                    }
                }
            }

            // Supprimer les API obsolètes
            deleteObsoleteApis(currentPaths);

            return "Fichier Swagger analysé et enregistré avec succès !";
        } catch (Exception e) {
            return "Erreur lors du traitement du fichier : " + e.getMessage();
        }
    }


    private void processOperation(String path, Operation operation, String method, String version, OpenAPI openAPI) {
        if (operation != null) {
            // Vérifier si l'API existe déjà
            Api existingApi = apiRepository.findByPathAndMethod(path, method);

            Api api;
            if (existingApi != null) {
                // Mettre à jour l'API existante
                api = existingApi;
                api.setDescription(operation.getDescription() != null ? operation.getDescription() : operation.getSummary());
                api.setVersion(version);
            } else {
                // Créer une nouvelle API
                api = new Api();
                api.setPath(path);
                api.setMethod(method);
                api.setDescription(operation.getDescription() != null ? operation.getDescription() : operation.getSummary());
                api.setVersion(version);
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
                        api.setRequest_body(requestBodyContent);

                        // Extraire et enregistrer le nom du schéma
                        String schemaName = extractSchemaRef(schema);
                        if (schemaName != null && !schemaName.isEmpty()) {
                            api.setSchema_name(schemaName);
                        }
                    }
                }
            }

            // Sauvegarder ou mettre à jour l'API
            Api savedApi = apiRepository.save(api);

            // Gestion des paramètres
            List<Parameter> parameters = operation.getParameters();
            if (parameters != null) {
                for (Parameter parameter : parameters) {
                    ApiParameters existingParameter = apiParametersRepository.findByApiAndName(savedApi, parameter.getName());

                    ApiParameters apiParameters;
                    if (existingParameter != null) {
                        // Mettre à jour le paramètre existant
                        apiParameters = existingParameter;
                    } else {
                        // Créer un nouveau paramètre
                        apiParameters = new ApiParameters();
                        apiParameters.setApi(savedApi);
                        apiParameters.setName(parameter.getName());
                    }

                    // Mettre à jour les champs du paramètre
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

                    // Sauvegarder ou mettre à jour le paramètre
                    apiParametersRepository.save(apiParameters);
                }
            }

            // Gestion des réponses
            if (operation.getResponses() != null) {
                Set<String> currentStatuses = new HashSet<>();

                for (Map.Entry<String, ApiResponse> entry : operation.getResponses().entrySet()) {
                    String status = entry.getKey();
                    ApiResponse responseValue = entry.getValue();

                    // Ajouter le statut à la liste des statuts actuels
                    currentStatuses.add(status);

                    // Sauvegarder ou mettre à jour la réponse
                    saveApiResponse(status, responseValue, savedApi, openAPI);
                }

                // Supprimer les réponses obsolètes
                deleteObsoleteResponses(savedApi, currentStatuses);
            }
        }
    }

    private void saveApiResponse(String status, ApiResponse response, Api api, OpenAPI openAPI) {
        // Vérifier si la réponse existe déjà pour cette API
        com.example.api_tierces.model.ApiResponse existingResponse = apiResponseRepository.findByApiAndStatus(api, status);

        if (existingResponse == null) {
            // Si la réponse n'existe pas, on la crée
            com.example.api_tierces.model.ApiResponse apiResponse = new com.example.api_tierces.model.ApiResponse();
            apiResponse.setApi(api);
            apiResponse.setStatus(status);

            if (response != null) {
                apiResponse.setDescription(response.getDescription());

                if (response.getContent() != null && response.getContent().containsKey("application/json")) {
                    MediaType mediaType = response.getContent().get("application/json");
                    io.swagger.v3.oas.models.media.Schema schema = mediaType.getSchema();

                    if (schema != null) {
                        String schemaNameRef = extractSchemaRef(schema);

                        List<Schema> existingSchemas = schemaRepository.findByName(schemaNameRef);
                        Schema existingSchema = existingSchemas != null && !existingSchemas.isEmpty() ? existingSchemas.get(0) : null;

                        apiResponse.setName_schema(schemaNameRef);

                        if (existingSchema != null) {
                            apiResponse.setSchema(existingSchema);
                        }
                    }
                }
            }

            // Sauvegarder la nouvelle réponse
            apiResponseRepository.save(apiResponse);
        }
    }

    private void saveSchema(String name, io.swagger.v3.oas.models.media.Schema schema, OpenAPI openAPI) {
        List<com.example.api_tierces.model.Schema> existingSchemas = schemaRepository.findByName(name);
        com.example.api_tierces.model.Schema schemaToSave;

        if (existingSchemas != null && !existingSchemas.isEmpty()) {
            // Mettre à jour le schéma existant
            schemaToSave = existingSchemas.get(0);
            schemaToSave.setSchemas(extractSchemaProperties(schema)); // Modification ici
        } else {
            // Créer un nouveau schéma
            schemaToSave = new com.example.api_tierces.model.Schema();
            schemaToSave.setName(name);
            schemaToSave.setType(schema.getType());
            schemaToSave.setSchemas(extractSchemaProperties(schema)); // Modification ici
        }


        schemaRepository.save(schemaToSave);
    }

    private String extractSchemaProperties(io.swagger.v3.oas.models.media.Schema schema) {
        Map<String, Object> extractedProperties = new HashMap<>();
        if (schema.getProperties() != null) {
            for (Object entry : schema.getProperties().entrySet()) {
                @SuppressWarnings("unchecked")
                Map.Entry<String, io.swagger.v3.oas.models.media.Schema> propertyEntry = (Map.Entry<String, io.swagger.v3.oas.models.media.Schema>) entry;
                String propertyName = propertyEntry.getKey();
                io.swagger.v3.oas.models.media.Schema propertySchema = propertyEntry.getValue();
                String propertyType = determineType(propertySchema);
                extractedProperties.put(propertyName, propertyType);
            }
        }
        try {
            return objectMapper.writeValueAsString(extractedProperties);
        } catch (JsonProcessingException e) {
            System.err.println("Erreur lors de la sérialisation JSON : " + e.getMessage());
            return "{}"; // Retourner un objet vide en cas d'erreur
        }
    }

    private void deleteObsoleteApis(Set<String> currentPaths) {
        // Récupérer toutes les API existantes
        List<Api> existingApis = apiRepository.findAll();

        for (Api existingApi : existingApis) {
            // Si le chemin de l'API n'est pas dans les chemins actuels, on la supprime
            if (!currentPaths.contains(existingApi.getPath())) {
                apiRepository.delete(existingApi);
            }
        }
    }

    private void deleteObsoleteSchemas(Set<String> currentSchemaNames) {
        // Récupérer tous les schémas existants
        List<com.example.api_tierces.model.Schema> existingSchemas = schemaRepository.findAll();

        for (com.example.api_tierces.model.Schema existingSchema : existingSchemas) {
            // Si le nom du schéma n'est pas dans les noms actuels, on le supprime
            if (!currentSchemaNames.contains(existingSchema.getName())) {
                schemaRepository.delete(existingSchema);
            }
        }
    }

    private void deleteObsoleteResponses(Api api, Set<String> currentStatuses) {
        // Récupérer toutes les réponses associées à cette API
        List<com.example.api_tierces.model.ApiResponse> existingResponses = apiResponseRepository.findByApi(api);

        for (com.example.api_tierces.model.ApiResponse existingResponse : existingResponses) {
            // Si le statut de la réponse n'est pas dans les statuts actuels, on la supprime
            if (!currentStatuses.contains(existingResponse.getStatus())) {
                apiResponseRepository.delete(existingResponse);
            }
        }
    }

    private String extractRequestBodyContent(io.swagger.v3.oas.models.media.Schema schema, OpenAPI openAPI) {
        try {
            if (schema.get$ref() != null) {
                String ref = schema.get$ref();
                String refName = ref.replace("#/components/schemas/", "");
                Map<String, io.swagger.v3.oas.models.media.Schema> schemas = openAPI.getComponents().getSchemas();

                if (schemas != null && schemas.containsKey(refName)) {
                    io.swagger.v3.oas.models.media.Schema resolvedSchema = schemas.get(refName);
                    return extractProperties(resolvedSchema);
                } else {
                    return "Schema reference not found: " + ref;
                }
            } else {
                return extractProperties(schema);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la sérialisation du schéma : " + e.getMessage());
            return "Erreur de sérialisation";
        }
    }

    private String extractProperties(io.swagger.v3.oas.models.media.Schema schema) {
        Map<String, Object> extractedProperties = new HashMap<>();
        if (schema.getProperties() != null) {
            for (Object entry : schema.getProperties().entrySet()) {
                @SuppressWarnings("unchecked")
                Map.Entry<String, io.swagger.v3.oas.models.media.Schema> propertyEntry = (Map.Entry<String, io.swagger.v3.oas.models.media.Schema>) entry;
                String propertyName = propertyEntry.getKey();
                io.swagger.v3.oas.models.media.Schema propertySchema = propertyEntry.getValue();
                String propertyType = determineType(propertySchema);
                extractedProperties.put(propertyName, propertyType);
            }
        }
        try {
            return objectMapper.writeValueAsString(extractedProperties);
        } catch (JsonProcessingException e) {
            System.err.println("Erreur lors de la sérialisation JSON : " + e.getMessage());
            return "{}"; // Retourner un objet vide en cas d'erreur
        }
    }

    private String determineType(io.swagger.v3.oas.models.media.Schema propertySchema) {
        if (propertySchema.getType() != null) {
            switch (propertySchema.getType()) {
                case "integer":
                    if ("int64".equals(propertySchema.getFormat())) {
                        return "<long>";
                    }
                    return "<integer>";
                case "number":
                    return "<number>";
                case "boolean":
                    return "<boolean>";
                case "string":
                    return "<string>";
                case "array":
                    return "<array>"; // Vous pouvez affiner cela davantage si nécessaire
                case "object":
                    return "<object>";
                default:
                    return "<unknown>";
            }
        } else {
            return "<unknown>";
        }
    }


    private String extractSchemaRef(io.swagger.v3.oas.models.media.Schema schema) {
        if (schema.get$ref() != null) {
            return schema.get$ref().replace("#/components/schemas/", "");
        }
        return null;
    }

    private String downloadFileContent(String swaggerUrl) throws IOException {
        StringBuilder content = new StringBuilder();
        URL url = new URL(swaggerUrl);
        URLConnection connection = url.openConnection();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }
}*/
package com.example.api_tierces.service;

import com.example.api_tierces.model.*;
import com.example.api_tierces.repository.*;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.swagger.v3.oas.models.parameters.RequestBody;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

import io.swagger.v3.oas.models.responses.ApiResponse;
//Code hedhaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa shihhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh
@Service
public class FileUploadService implements UploadService {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadService.class);

    private final ApiRepository apiRepository;
    private final ApiResponseRepository apiResponseRepository;
    private final SchemaRepository schemaRepository;
    private final ApiParametersRepository apiParametersRepository;


    public FileUploadService(ApiRepository apiRepository,
                             ApiResponseRepository apiResponseRepository,
                             SchemaRepository schemaRepository,
                             ApiParametersRepository apiParametersRepository) {
        this.apiRepository = apiRepository;
        this.apiResponseRepository = apiResponseRepository;
        this.schemaRepository = schemaRepository;
        this.apiParametersRepository = apiParametersRepository;
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

        try {
            OpenAPIV3Parser parser = new OpenAPIV3Parser();
            OpenAPI openAPI = parser.readContents(fileContent, null, null).getOpenAPI();
            String version = openAPI.getInfo().getVersion();

            // Gestion des schémas
            Map<String, io.swagger.v3.oas.models.media.Schema> schemas = openAPI.getComponents().getSchemas();
            Set<String> currentSchemaNames = new HashSet<>();

            if (schemas != null) {
                for (Map.Entry<String, io.swagger.v3.oas.models.media.Schema> entry : schemas.entrySet()) {
                    String schemaName = entry.getKey();
                    io.swagger.v3.oas.models.media.Schema schemaValue = entry.getValue();
                    if (saveSchema(schemaName, schemaValue, openAPI)) {
                        hasChanges = true; // Changement détecté
                    }
                    currentSchemaNames.add(schemaName);
                }
            }

            // Supprimer les schémas obsolètes
            if (deleteObsoleteSchemas(currentSchemaNames)) {
                hasChanges = true; // Changement détecté
            }

            // Gestion des chemins (API)
            Map<String, PathItem> paths = openAPI.getPaths();
            Set<String> currentPaths = new HashSet<>();

            if (paths != null) {
                for (Map.Entry<String, PathItem> pathEntry : paths.entrySet()) {
                    String pathName = pathEntry.getKey();
                    PathItem pathValue = pathEntry.getValue();

                    currentPaths.add(pathName);

                    if (pathValue.getGet() != null && processOperation(pathName, pathValue.getGet(), "GET", version, openAPI)) {
                        hasChanges = true; // Changement détecté
                    }
                    if (pathValue.getPost() != null && processOperation(pathName, pathValue.getPost(), "POST", version, openAPI)) {
                        hasChanges = true; // Changement détecté
                    }
                    if (pathValue.getPut() != null && processOperation(pathName, pathValue.getPut(), "PUT", version, openAPI)) {
                        hasChanges = true; // Changement détecté
                    }
                    if (pathValue.getDelete() != null && processOperation(pathName, pathValue.getDelete(), "DELETE", version, openAPI)) {
                        hasChanges = true; // Changement détecté
                    }
                    if (pathValue.getOptions() != null && processOperation(pathName, pathValue.getOptions(), "OPTIONS", version, openAPI)) {
                        hasChanges = true; // Changement détecté
                    }
                    if (pathValue.getHead() != null && processOperation(pathName, pathValue.getHead(), "HEAD", version, openAPI)) {
                        hasChanges = true; // Changement détecté
                    }
                    if (pathValue.getTrace() != null && processOperation(pathName, pathValue.getTrace(), "TRACE", version, openAPI)) {
                        hasChanges = true; // Changement détecté
                    }
                }
            }

            // Supprimer les API obsolètes
            if (deleteObsoleteApis(currentPaths)) {
                hasChanges = true; // Changement détecté
            }


            // Afficher le message en fonction de hasChanges
            if (!hasChanges) {
                logChanges("Aucun changement réalisé.");
            }

            return "Fichier Swagger analysé et enregistré avec succès !";
        } catch (Exception e) {
            logger.error("Erreur lors du traitement du fichier : {}", e.getMessage());
            return "Erreur lors du traitement du fichier : " + e.getMessage();
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

    private boolean saveSchema(String name, io.swagger.v3.oas.models.media.Schema schema, OpenAPI openAPI) {
        List<com.example.api_tierces.model.Schema> existingSchemas = schemaRepository.findByName(name);
        com.example.api_tierces.model.Schema schemaToSave;
        boolean hasChanged = false;

        if (existingSchemas != null && !existingSchemas.isEmpty()) {
            // Mettre à jour le schéma existant
            schemaToSave = existingSchemas.get(0);

            // Vérifier si les valeurs ont changé
            if (hasChanged(schemaToSave.getType(), schema.getType())) {
                logChanges("Type du schéma '" + name + "' modifié de '" + schemaToSave.getType() + "' à '" + schema.getType() + "'");
                schemaToSave.setType(schema.getType());
                hasChanged = true;
            }

            String newSchemaContent = Json.pretty(schema);
            if (hasChanged(schemaToSave.getSchemas(), newSchemaContent)) {
                logChanges("Contenu du schéma '" + name + "' modifié.");
                schemaToSave.setSchemas(newSchemaContent);
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
            logChanges("Ajout d'un nouveau schéma: " + name);
            schemaRepository.save(schemaToSave);
            hasChanged = true;
        }

        return hasChanged;
    }

    private boolean deleteObsoleteSchemas(Set<String> currentSchemaNames) {
        boolean hasChanged = false;
        List<com.example.api_tierces.model.Schema> existingSchemas = schemaRepository.findAll();

        for (com.example.api_tierces.model.Schema existingSchema : existingSchemas) {
            if (!currentSchemaNames.contains(existingSchema.getName())) {
                logChanges("Suppression du schéma obsolète: " + existingSchema.getName());
                schemaRepository.delete(existingSchema);
                hasChanged = true;
            }
        }

        return hasChanged;
    }

    private boolean processOperation(String path, Operation operation, String method, String version, OpenAPI openAPI) {
        boolean hasChanged = false;

        if (operation != null) {
            Api existingApi = apiRepository.findByPathAndMethod(path, method);
            Api api;

            if (existingApi != null) {
                // Mettre à jour l'API existante
                api = existingApi;

                // Vérifier si les valeurs ont changé
                String newDescription = operation.getDescription() != null ? operation.getDescription() : operation.getSummary();
                if (hasChanged(api.getDescription(), newDescription)) {
                    logChanges("Description de l'API '" + path + " (" + method + ")' modifiée de '" + api.getDescription() + "' à '" + newDescription + "'");
                    api.setDescription(newDescription);
                    hasChanged = true;
                }
                if (hasChanged(api.getVersion(), version)) {
                    logChanges("Version de l'API '" + path + " (" + method + ")' modifiée de '" + api.getVersion() + "' à '" + version + "'");
                    api.setVersion(version);
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
                                logChanges("Request body de l'API '" + path + " (" + method + ")' modifié.");
                                api.setRequest_body(requestBodyContent);
                                hasChanged = true;
                            }


                            // Extraire et enregistrer le nom du schéma
                            String schemaName = extractSchemaRef(schema);
                            if (schemaName != null && !schemaName.isEmpty()) {
                                if (hasChanged(api.getSchema_name(), schemaName)) {
                                    logChanges("Schema name de l'API '" + path + " (" + method + ")' modifié de '" + api.getSchema_name() + "' à '" + schemaName + "'");
                                    api.setSchema_name(schemaName);
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
                logChanges("Ajout d'une nouvelle API: " + path + " (" + method + ")");
                apiRepository.save(api);
                hasChanged = true;
            }

            //Sauvegarder l'API
            Api savedApi = apiRepository.save(api);

            // Gestion des paramètres
            hasChanged = processParameters(openAPI, path, operation, method, savedApi, hasChanged);

            // Gestion des réponses
            hasChanged = processResponses(openAPI, path, operation, method, savedApi, hasChanged);


        }

        return hasChanged;
    }

    private boolean processParameters(OpenAPI openAPI, String path, Operation operation, String method, Api savedApi, boolean hasChanged) {
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
                        logChanges("Type du paramètre '" + parameter.getName() + "' de l'API '" + path + " (" + method + ")' modifié de '" + apiParameters.getTypein() + "' à '" + (parameter.getIn() != null ? parameter.getIn().toString() : null) + "'");
                        apiParameters.setTypein(parameter.getIn() != null ? parameter.getIn().toString() : null);
                        parameterChanged = true;
                    }
                    if (hasChanged(apiParameters.getExample(), parameter.getExample() != null ? parameter.getExample().toString() : "Aucun exemple disponible")) {
                        logChanges("Example du paramètre '" + parameter.getName() + "' de l'API '" + path + " (" + method + ")' modifié de '" + apiParameters.getExample() + "' à '" + (parameter.getExample() != null ? parameter.getExample().toString() : "Aucun exemple disponible") + "'");
                        apiParameters.setExample(parameter.getExample() != null ? parameter.getExample().toString() : "Aucun exemple disponible");
                        parameterChanged = true;
                    }
                    if (hasChanged(apiParameters.getData_type(), parameter.getSchema() != null ? parameter.getSchema().getType() : null)) {
                        logChanges("Data type du paramètre '" + parameter.getName() + "' de l'API '" + path + " (" + method + ")' modifié de '" + apiParameters.getData_type() + "' à '" + (parameter.getSchema() != null ? parameter.getSchema().getType() : null) + "'");
                        apiParameters.setData_type(parameter.getSchema() != null ? parameter.getSchema().getType() : null);
                        parameterChanged = true;
                    }
                    if (hasChanged(apiParameters.getDescription(), parameter.getDescription())) {
                        logChanges("Description du paramètre '" + parameter.getName() + "' de l'API '" + path + " (" + method + ")' modifiée de '" + apiParameters.getDescription() + "' à '" + parameter.getDescription() + "'");
                        apiParameters.setDescription(parameter.getDescription());
                        parameterChanged = true;
                    }
                    if (hasChanged(apiParameters.getRequired(), parameter.getRequired())) {
                        logChanges("Required du paramètre '" + parameter.getName() + "' de l'API '" + path + " (" + method + ")' modifié de '" + apiParameters.getRequired() + "' à '" + parameter.getRequired() + "'");
                        apiParameters.setRequired(parameter.getRequired());
                        parameterChanged = true;
                    }

                    if (parameterChanged) {
                        try {
                            apiParametersRepository.save(apiParameters);
                            hasChanged = true;
                        }catch (Exception e){
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
                    logChanges("Ajout du paramètre '" + parameter.getName() + "' à l'API '" + path + " (" + method + ")'");
                    try {
                        apiParametersRepository.save(apiParameters);
                        hasChanged = true;
                    }catch (Exception e){
                        logger.error("Erreur lors de la sauvegarde du parametre : {}", e.getMessage());
                    }

                }


            }
        }

        //Supprimer les parametres obsolètes
        List<ApiParameters> existingParameters = apiParametersRepository.findByApi(savedApi);
        for (ApiParameters existingParameter : existingParameters) {
            if (!currentParameterNames.contains(existingParameter.getName())) {
                logChanges("Suppression du paramètre obsolète '" + existingParameter.getName() + "' de l'API '" + path + " (" + method + ")'");
                try{
                    apiParametersRepository.delete(existingParameter);
                    hasChanged = true;
                }catch (Exception e){
                    logger.error("Erreur lors de la suppression du parametre : {}", e.getMessage());
                }


            }
        }
        return hasChanged;
    }


    /*private boolean processResponses(OpenAPI openAPI, String path, Operation operation, String method, Api savedApi, boolean hasChanged) {

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
                    //Mettre à jour la réponse existante
                    apiResponse = existingResponse;

                    //Vérifier si les valeurs ont changé
                    if (hasChanged(apiResponse.getDescription(), responseValue.getDescription())) {
                        logChanges("Description de la réponse '" + status + "' de l'API '" + path + " (" + method + ")' modifiée de '" + apiResponse.getDescription() + "' à '" + responseValue.getDescription() + "'");
                        apiResponse.setDescription(responseValue.getDescription());
                        responseChanged = true;
                    }


                    String schemaNameRef = null;
                    if (responseValue.getContent() != null && responseValue.getContent().containsKey("application/json")) {
                        MediaType mediaType = responseValue.getContent().get("application/json");
                        if(mediaType != null){
                            io.swagger.v3.oas.models.media.Schema schema = mediaType.getSchema();
                            if (schema != null) {
                                schemaNameRef = extractSchemaRef(schema);
                            }
                        }

                    }
                    if(hasChanged(apiResponse.getName_schema(), schemaNameRef)){
                        logChanges("Name Schema de la réponse '" + status + "' de l'API '" + path + " (" + method + ")' modifié de '" + apiResponse.getName_schema() + "' à '" + schemaNameRef + "'");
                        apiResponse.setName_schema(schemaNameRef);
                        responseChanged = true;
                    }



                    if (responseChanged) {
                        try {
                            apiResponseRepository.save(apiResponse);
                            hasChanged = true;
                        }catch (Exception e){
                            logger.error("Erreur lors de la sauvegarde de la reponse : {}", e.getMessage());
                        }

                    }


                } else {
                    //Créer une nouvelle réponse
                    apiResponse = new com.example.api_tierces.model.ApiResponse();
                    apiResponse.setApi(savedApi);
                    apiResponse.setStatus(status);
                    apiResponse.setDescription(responseValue.getDescription());

                    String schemaNameRef = null;
                    if (responseValue.getContent() != null && responseValue.getContent().containsKey("application/json")) {
                        MediaType mediaType = responseValue.getContent().get("application/json");
                        if(mediaType != null){
                            io.swagger.v3.oas.models.media.Schema schema = mediaType.getSchema();
                            if (schema != null) {
                                schemaNameRef = extractSchemaRef(schema);
                            }
                        }

                    }

                    apiResponse.setName_schema(schemaNameRef);

                    logChanges("Ajout de la reponse '" + status + "' à l'API '" + path + " (" + method + ")'");
                    try {
                        apiResponseRepository.save(apiResponse);
                        hasChanged = true;
                    }catch (Exception e){
                        logger.error("Erreur lors de la sauvegarde de la reponse : {}", e.getMessage());
                    }

                }


            }


            //Supprimer les réponses obsolètes
            List<com.example.api_tierces.model.ApiResponse> existingResponses = apiResponseRepository.findByApi(savedApi);
            for (com.example.api_tierces.model.ApiResponse existingResponse : existingResponses) {
                if (!currentStatuses.contains(existingResponse.getStatus())) {
                    logChanges("Suppression de la réponse obsolète '" + existingResponse.getStatus() + "' de l'API '" + path + " (" + method + ")'");
                    try{
                        apiResponseRepository.delete(existingResponse);
                        hasChanged = true;
                    }catch (Exception e){
                        logger.error("Erreur lors de la suppression de la reponse : {}", e.getMessage());
                    }

                }
            }
        }

        return hasChanged;
    }*/
    private boolean processResponses(OpenAPI openAPI, String path, Operation operation, String method, Api savedApi, boolean hasChanged) {
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
                        logChanges("Description de la réponse '" + status + "' de l'API '" + path + " (" + method + ")' modifiée de '" + apiResponse.getDescription() + "' à '" + responseValue.getDescription() + "'");
                        apiResponse.setDescription(responseValue.getDescription());
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
                        logChanges("Name Schema de la réponse '" + status + "' de l'API '" + path + " (" + method + ")' modifié de '" + apiResponse.getName_schema() + "' à '" + schemaNameRef + "'");
                        apiResponse.setName_schema(schemaNameRef);
                        apiResponse.setSchema(schemaEntity); // Lier l'entité Schema
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

                    logChanges("Ajout de la réponse '" + status + "' à l'API '" + path + " (" + method + ")'");
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
                    logChanges("Suppression de la réponse obsolète '" + existingResponse.getStatus() + "' de l'API '" + path + " (" + method + ")'");
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


    private boolean deleteObsoleteApis(Set<String> currentPaths) {
        boolean hasChanged = false;
        List<Api> existingApis = apiRepository.findAll();

        for (Api existingApi : existingApis) {
            if (!currentPaths.contains(existingApi.getPath())) {
                logChanges("Suppression de l'API obsolète: " + existingApi.getPath() + " (" + existingApi.getMethod() + ")");
                try {
                    apiRepository.delete(existingApi);
                    hasChanged = true;
                }catch (Exception e){
                    logger.error("Erreur lors de la suppression de l'api : {}", e.getMessage());
                }

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
