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

    public ApiChange() {}

    public ApiChange(String changement, LocalDateTime temps) {
        this.changement = changement;
        this.temps = temps;
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
}

