package com.example.api_tierces.repository;

import org.springframework.data.jpa.repository.JpaRepository; //CRUD
import org.springframework.stereotype.Repository;
import com.example.api_tierces.model.Api;

@Repository
public interface ApiRepository extends JpaRepository<Api, Integer>{
}
