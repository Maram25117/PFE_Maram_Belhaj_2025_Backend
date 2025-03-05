package com.example.api_tierces.service;


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
import io.swagger.v3.oas.models.responses.ApiResponse; //utilisé pour gérer les requêtes et réponses.
import org.springframework.transaction.annotation.Transactional; // pour gérer les transactions dans une application Spring Boot.

//Importe des classes utilitaires Java pour gérer les collections (List, Map, ArrayList, HashMap) et des opérations sur les objets.
import java.util.List;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Iterator;

@Service
public class ApiServiceImpl implements ApiService {
    //Déclare des repositories pour gérer l’accès aux données des API, des réponses, des schémas et des paramètres.
    private final ApiRepository apiRepository;
    private final ApiResponseRepository apiResponseRepository;
    private final SchemaRepository schemaRepository;
    private final ApiParametersRepository apiParametersRepository;

    public ApiServiceImpl(ApiRepository apiRepository,
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
}

