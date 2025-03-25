/*package com.example.api_tierces.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "api_collection")
public class ApiCollection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "collection", nullable = false)
    private String collection; // Stocker le nom du fichier

    @Column(name = "temps", nullable = false)
    private LocalDateTime temps;

    // Constructeurs
    public ApiCollection() {}

    public ApiCollection(String collection, LocalDateTime temps) {
        this.collection = collection;
        this.temps = temps;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public LocalDateTime getTemps() {
        return temps;
    }

    public void setTemps(LocalDateTime temps) {
        this.temps = temps;
    }
}*/