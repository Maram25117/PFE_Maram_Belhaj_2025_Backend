package com.example.api_tierces.service;

import com.example.api_tierces.model.Schema;
import com.example.api_tierces.repository.SchemaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SchemaService {

    @Autowired
    private SchemaRepository schemaRepository;


    public List<Schema> getAllSchemas() {
        return schemaRepository.findAll();
    }


    public Optional<Schema> getSchemaById(Long id) {
        return schemaRepository.findById(id);
    }


    public List<Schema> getSchemaByName(String name) {
        return schemaRepository.findByName(name);
    }
}


