package com.example.api_tierces.repository;


import com.example.api_tierces.model.Swagger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface SwaggerUrlRepository extends JpaRepository<Swagger, String> {
    // JpaRepository fournit les méthodes CRUD de base comme save(), findById(), findAll(), deleteById() etc.
}
