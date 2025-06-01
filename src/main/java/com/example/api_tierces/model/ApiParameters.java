package com.example.api_tierces.model;

import jakarta.persistence.*;

@Entity
@Table(name = "api_parameters")
public class ApiParameters {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "api_id", nullable = false)
    private Api api; // Reference à l'entité Api

    private String name;
    private String typein;
    private String data_type;
    private String description;

    private Boolean required;

    @Column(columnDefinition = "TEXT")
    private String example;



    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Api getApi() { return api; }
    public void setApi(Api api) { this.api = api; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getTypein() { return typein; }
    public void setTypein(String typein) { this.typein = typein; }
    public String getData_type() { return data_type; }
    public void setData_type(String data_type) { this.data_type = data_type; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Boolean getRequired() { return required; }
    public void setRequired(Boolean required) { this.required = required; }
    public String getExample() { return example; }
    public void setExample(String example) { this.example = example; }
}
