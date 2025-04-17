package com.example.api_tierces.model;

import jakarta.persistence.*;

@Entity
@Table(name = "swagger_url")
public class Swagger {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "url" , nullable = true)
    private String url;


    public Swagger() {
    }


    public Swagger(String id, String url) {
        this.id = id;
        this.url = url;
    }

    // --- Getters et Setters ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
