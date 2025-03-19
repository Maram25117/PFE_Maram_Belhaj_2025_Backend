package com.example.api_tierces.repository;

import org.springframework.data.jpa.repository.JpaRepository; //CRUD
import org.springframework.stereotype.Repository;
import com.example.api_tierces.model.Api;

import java.util.Optional;

@Repository
public interface ApiRepository extends JpaRepository<Api, Long>{
    boolean existsByPathAndMethod(String path, String method);
    Optional<Api> findByPath(String path);
    Api findByPathAndMethod(String path, String method);
    //Optional<Api> findByPathAndMethod(String path, String method);
}
