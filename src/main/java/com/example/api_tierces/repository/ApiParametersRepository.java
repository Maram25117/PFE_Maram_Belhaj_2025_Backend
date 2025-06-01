package com.example.api_tierces.repository;

import com.example.api_tierces.model.ApiParameters;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.api_tierces.model.Api;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiParametersRepository extends JpaRepository<ApiParameters, Long> {
    Optional<ApiParameters> findById(Long id);
    List<ApiParameters> findByApiId(Long apiId);
    List<ApiParameters> findByApi(Api api);
    ApiParameters findByApiAndName(Api api, String name);
    List<ApiParameters> findByName(String name);
}
