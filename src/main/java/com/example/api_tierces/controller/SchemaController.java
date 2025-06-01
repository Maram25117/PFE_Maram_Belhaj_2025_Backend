package com.example.api_tierces.controller;

import com.example.api_tierces.model.Schema;
import com.example.api_tierces.service.SchemaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Api Schema")
@RestController
@RequestMapping("/api/schemas")
public class SchemaController {

    @Autowired
    private SchemaService schemaService;

    @Operation(summary = "Récupérer tous les schemas")
    @GetMapping
    public List<Schema> getAllSchemas() {
        return schemaService.getAllSchemas();
    }

    @Operation(summary = "Récupérer un schema par son id")
    @GetMapping("/{id}")
    public ResponseEntity<Schema> getSchemaById(@PathVariable Long id) {
        return schemaService.getSchemaById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Récupérer un schema par son nom")
    @GetMapping("/name/{name}")
    public ResponseEntity<List<Schema>> getSchemaByName(@PathVariable String name) {
        List<Schema> schemas = schemaService.getSchemaByName(name);
        if (schemas.isEmpty()) {
            return ResponseEntity.notFound().build(); // Retourner 404 si aucun schéma n'est trouvé
        }
        return ResponseEntity.ok(schemas); // Retourner 200 avec la liste des schémas
    }
}

