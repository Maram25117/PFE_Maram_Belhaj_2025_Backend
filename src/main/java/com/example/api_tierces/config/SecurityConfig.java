package com.example.api_tierces.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.config.Customizer;

/*@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean //cette méthode sera exécutée par Spring durant la phase de démarrage de l'application
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(new AntPathRequestMatcher("/api/swagger")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/swagger-ui/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/v3/api-docs/**")).permitAll()

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.defaultSuccessUrl("/swagger-ui/index.html", true))
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}*/
@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean //cette méthode sera exécutée par Spring durant la phase de démarrage de l'application
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(new AntPathRequestMatcher("/api/upload_lib_api")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/swagger-ui/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/v3/api-docs/**")).permitAll()

                        // Autorise toutes les requêtes sans authentification
                        .anyRequest().permitAll()
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}

