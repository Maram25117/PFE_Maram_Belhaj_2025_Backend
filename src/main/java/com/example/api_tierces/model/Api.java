package com.example.api_tierces.model;

import jakarta.persistence.*; //JPA est utilisée pour interagir avec des bases de données relationnelles en Java
//vous avez accès aux annotations couramment utilisées en JPA : @Entity , @Table(name = "nom_table") , @Id ...


@Entity
@Table(name = "api")
public class Api {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String path;
    private String method;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String version;
    private String tags;
    private String schema_name;

    @Column(columnDefinition = "TEXT")
    private String request_body;


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public String getSchema_name() { return schema_name; }
    public void setSchema_name(String schema_name) { this.schema_name = schema_name; }

    public String getRequest_body() { return request_body; }
    public void setRequest_body(String request_body) { this.request_body = request_body; }

}
