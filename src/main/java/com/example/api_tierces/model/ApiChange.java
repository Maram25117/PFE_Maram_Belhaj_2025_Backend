package com.example.api_tierces.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "api_change")
public class ApiChange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String changement;

    @Column(nullable = false)
    private LocalDateTime temps;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String donneeChange;

    public ApiChange() {}

    public ApiChange(String changement, LocalDateTime temps, String donneeChange) {
        this.changement = changement;
        this.temps = temps;
        this.donneeChange = donneeChange;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getChangement() {
        return changement;
    }

    public void setChangement(String changement) {
        this.changement = changement;
    }

    public LocalDateTime getTemps() {
        return temps;
    }

    public void setTemps(LocalDateTime temps) {
        this.temps = temps;
    }

    public String getDonneeChange() {
        return donneeChange;
    }

    public void setDonneeChange(String donneeChange) {
        this.donneeChange = donneeChange;
    }
}
