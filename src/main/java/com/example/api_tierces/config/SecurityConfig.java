package com.example.api_tierces.config;
import java.util.Collections;

import com.example.api_tierces.model.Admin;
import com.example.api_tierces.repository.AdminRepository;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.security.config.Customizer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity

public class SecurityConfig  {

  // chemin publics
  private static final String[] SWAGGER_AND_LOGIN_PATHS = {
            "/admin/login",
            "/admin/logout",
            "/admin/forgot-password",
            "/admin/reset-password",
            "/actuator/prometheus",
            "/favicon.ico",
            "/webjars/**"
    };


    private static final String[] PUBLIC_PATHS = {
            "/error"
    };

    private final AdminRepository adminRepository;

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    public SecurityConfig(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
    // Elle permet d’accéder au AuthenticationManager configuré via les UserDetailsService, PasswordEncoder


    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


    // ce qui se passe lorsque un utilisateur n'est pas authentifié , on va l'utiliser dans securityfilterchain
    @Bean
    public BasicAuthenticationEntryPoint authenticationEntryPoint() {
        BasicAuthenticationEntryPoint entryPoint = new BasicAuthenticationEntryPoint();
        entryPoint.setRealmName("API Tierces Execution");
        return entryPoint;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(SWAGGER_AND_LOGIN_PATHS).permitAll()
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: Please log in.");
                        })
                );


        return http.build();
    }


    // authentification par email
    @Bean
    public UserDetailsService userDetailsService() {
        return email -> {
            log.debug("UserDetailsService attempting to load user by email: {}", email);

            Admin admin = adminRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        log.warn("UserDetailsService: Admin not found with email: {}", email);
                        return new UsernameNotFoundException("Aucun administrateur trouvé avec l'email : " + email);
                    });
            log.debug("UserDetailsService: Admin found for email: {}. Creating UserDetails.", email);

            return new User(
                    admin.getEmail(),
                    admin.getPassword(),
                    Collections.emptyList()
            );
        };
    }


    // documentation OpenAPI Swagger UI
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                 .info(new Info()
                        .title("Gestion automatisée des API Tierces")
                        .version("1.0.0")
                        .description("API pour la gestion automatisée des API Tierces.")
                        .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")));
    }
}
