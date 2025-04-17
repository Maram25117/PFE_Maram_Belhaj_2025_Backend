package com.example.api_tierces.repository;


import com.example.api_tierces.model.Swagger; // Assurez-vous que le chemin est correct
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface SwaggerUrlRepository extends JpaRepository<Swagger, String> {
    // JpaRepository fournit les méthodes CRUD de base comme save(), findById(), findAll(), deleteById() etc.

    // Vous pouvez ajouter des méthodes de recherche personnalisées ici si nécessaire.
    // Par exemple, pour trouver par URL (si besoin):
    // Optional<SwaggerUrl> findByUrl(String url);
}
