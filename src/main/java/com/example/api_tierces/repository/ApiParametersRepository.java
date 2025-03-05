package com.example.api_tierces.repository;

import com.example.api_tierces.model.ApiParameters;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiParametersRepository extends JpaRepository<ApiParameters, Long> {
}
