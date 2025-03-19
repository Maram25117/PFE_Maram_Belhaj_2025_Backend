package com.example.api_tierces.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "api_monitoring")
public class ApiMonitoring {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String path; // Chemin de l'API (endpoint)
    private LocalDateTime temps; // Timestamp de l'événement
    private long responseTime; // Temps de réponse en millisecondes
    private int statusCode; // Code de statut HTTP
    private String errorMessage; // Message d'erreur (facultatif)
    private String level; // Niveau de log (INFO, ERROR, etc.)
    private String metadata; // Métadonnées supplémentaires (JSON)

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public LocalDateTime getTemps() {
        return temps;
    }

    public void setTemps(LocalDateTime temps) {
        this.temps = temps;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

}
