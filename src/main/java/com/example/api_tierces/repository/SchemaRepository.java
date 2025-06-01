package com.example.api_tierces.repository;

import com.example.api_tierces.model.Schema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SchemaRepository extends JpaRepository<Schema, Long> {
    List<Schema> findByName(String name);
}
