package com.example.api_tierces;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ApiTiercesApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiTiercesApplication.class, args);
	}

}
