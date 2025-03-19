package com.example.api_tierces.controller;

import com.example.api_tierces.model.Schema;
import com.example.api_tierces.service.SchemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schemas")
public class SchemaController {

    @Autowired
    private SchemaService schemaService;

    // Récupérer tous les schémas
    @GetMapping
    public List<Schema> getAllSchemas() {
        return schemaService.getAllSchemas();
    }

    // Récupérer un schéma par son ID
    @GetMapping("/{id}")
    public ResponseEntity<Schema> getSchemaById(@PathVariable Long id) {
        return schemaService.getSchemaById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Récupérer un schéma par son nom
    @GetMapping("/name/{name}")
    public ResponseEntity<List<Schema>> getSchemaByName(@PathVariable String name) {
        List<Schema> schemas = schemaService.getSchemaByName(name);
        if (schemas.isEmpty()) {
            return ResponseEntity.notFound().build(); // Retourner 404 si aucun schéma n'est trouvé
        }
        return ResponseEntity.ok(schemas); // Retourner 200 avec la liste des schémas
    }
}

