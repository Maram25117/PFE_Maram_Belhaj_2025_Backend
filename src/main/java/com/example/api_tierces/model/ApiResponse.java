package com.example.api_tierces.model;

import jakarta.persistence.*;

@Entity
@Table(name = "api_responses")
public class ApiResponse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "api_id", nullable = false)
    private Api api; // Reference à l'entité Api

    @ManyToOne
    @JoinColumn(name = "id_schema")
    private Schema schema; // Reference à l'entité Schema

    private String status;
    private String description;
    private String name_schema; // Stocke le nom de la référence au schéma




    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Api getApi() { return api; }
    public void setApi(Api api) { this.api = api; }
    public Schema getSchema() { return schema; }
    public void setSchema(Schema schema) { this.schema = schema; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getName_schema() { return name_schema; }
    public void setName_schema(String name_schema) { this.name_schema = name_schema; }

}
