package com.example.api_tierces.model;

import jakarta.persistence.*;

@Entity
@Table(name = "api_schema")
public class Schema {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String type;

    @Column(columnDefinition = "TEXT")
    private String schemas; // Contenu JSON du schéma

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSchemas() {
        return schemas;
    }

    public void setSchemas(String schemas) {
        this.schemas = schemas;
    }
}
