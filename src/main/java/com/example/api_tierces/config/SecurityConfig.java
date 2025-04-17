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
/*package com.example.api_tierces.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.config.Customizer;


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
                        .requestMatchers("/admin/**").permitAll()
                        // Autorise toutes les requêtes sans authentification
                        .anyRequest().permitAll()
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Gestion automatisé des API Tierces")
                        .version("1.0.0")
                        .description("API pour la gestion automatisé des API Tierces.")
                        .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")));
    }
}*/


/*hedhaaaaaa shihhhhhhh*/
/*package com.example.api_tierces.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.config.Customizer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableMethodSecurity
public class SecurityConfig implements WebMvcConfigurer {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // Utilisation de BCrypt pour l'encodage des mots de passe
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // Autoriser toutes les routes
                .allowedOrigins("http://localhost:4200")  // Autoriser l'origine de votre application Angular
                .allowedMethods("GET", "POST", "PUT", "DELETE")  // Autoriser les méthodes nécessaires
                .allowedHeaders("*")  // Autoriser tous les en-têtes
                .allowCredentials(true);  // Si nécessaire, autoriser les cookies et l'authentification
    }

    @Bean // Cette méthode sera exécutée par Spring durant la phase de démarrage de l'application
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(new AntPathRequestMatcher("/api/upload_lib_api")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/swagger-ui/**")).authenticated() //permitall
                        .requestMatchers(new AntPathRequestMatcher("/v3/api-docs/**")).permitAll()
                        .requestMatchers("/admin/**").permitAll()
                        // Autorise toutes les requêtes sans authentification
                        .anyRequest().permitAll()
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Gestion automatisée des API Tierces")
                        .version("1.0.0")
                        .description("API pour la gestion automatisée des API Tierces.")
                        .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")));
    }
}*/

/*package com.example.api_tierces.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// Import nécessaire si vous utilisez csrf.disable() de la manière la plus récente
// import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableMethodSecurity // Active la sécurité au niveau des méthodes (@PreAuthorize, etc.) si besoin
public class SecurityConfig implements WebMvcConfigurer {

    // Liste des chemins pour Swagger UI et API Docs OpenAPI v3
    private static final String[] SWAGGER_PATHS = {
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**", // Ajouter explicitement
            "/webjars/**"           // Ajouter explicitement
    };

    // Liste des chemins explicitement publics
    private static final String[] PUBLIC_PATHS = {
            "/api/upload_lib_api",
            "/admin/**" // Assurez-vous que TOUT sous /admin doit vraiment être public
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:4200")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Désactiver CSRF (courant pour les APIs REST/Stateless)
                .csrf(csrf -> csrf.disable())
                // Si la ligne ci-dessus ne fonctionne pas (ancienne version?), essayez :
                // .csrf(AbstractHttpConfigurer::disable)

                // Configurer les autorisations de requêtes
                .authorizeHttpRequests(auth -> auth
                                // Autoriser l'accès sans authentification aux chemins publics définis
                                .requestMatchers(PUBLIC_PATHS).permitAll()
                                // Exiger une authentification pour accéder à Swagger
                                .requestMatchers(SWAGGER_PATHS).authenticated()
                                // ----- OPTION RECOMMANDÉE : Sécuriser tout le reste par défaut -----
                                .anyRequest().authenticated()
                        // ----- OPTION ALTERNATIVE (ce que vous aviez, mais risqué) : -----
                        // .anyRequest().permitAll() // ATTENTION : Laisse vos autres API non sécurisées !
                )
                // Activer l'authentification HTTP Basic (provoque la pop-up de login du navigateur)
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Gestion automatisée des API Tierces")
                        .version("1.0.0")
                        .description("API pour la gestion automatisée des API Tierces.")
                        .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")));
    }
}*/

/*package com.example.api_tierces.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig implements WebMvcConfigurer {

    private static final String[] SWAGGER_PATHS = {
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**"
    };

    private static final String[] PUBLIC_PATHS = {
            "/api/upload_lib_api",
            "/admin/**"
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:4200")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        .requestMatchers(SWAGGER_PATHS).authenticated()
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .formLogin(form -> form.disable()); // Désactive le form login pour une API REST

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails swaggerUser = User.builder()
                .username("swagger")
                .password(passwordEncoder().encode("swagger123"))
                .roles("SWAGGER")
                .build();

        return new InMemoryUserDetailsManager(swaggerUser);
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Gestion automatisée des API Tierces")
                        .version("1.0.0")
                        .description("API pour la gestion automatisée des API Tierces.")
                        .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")));
    }
}*/
/*hedhaaaaaaaaa cvvvvv */
package com.example.api_tierces.config;
import java.util.Collections;

import com.example.api_tierces.model.Admin;
import com.example.api_tierces.repository.AdminRepository; // Make sure this import is correct
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint; // Import correct interface
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.security.config.Customizer;

/*@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig implements WebMvcConfigurer {

    private static final String[] SWAGGER_PATHS = {
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**"
    };

    private static final String[] PUBLIC_PATHS = {
            "/api/upload_lib_api",
            "/admin/**",
            "/error"
    };

    private final AdminRepository adminRepository;

    public SecurityConfig(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:4200")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Bean
    public BasicAuthenticationEntryPoint authenticationEntryPoint() {
        BasicAuthenticationEntryPoint entryPoint = new BasicAuthenticationEntryPoint();
        entryPoint.setRealmName("API Tierces");
        return entryPoint;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        .requestMatchers(SWAGGER_PATHS).authenticated()
                        .anyRequest().authenticated()
                )
                .httpBasic(basic -> basic
                        .authenticationEntryPoint(authenticationEntryPoint()) // Utilisation du bean
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            Admin admin = adminRepository.findByUsername(username);
            if (admin == null) {
                throw new UsernameNotFoundException("Admin not found: " + username);
            }
            return new User(
                    admin.getUsername(),
                    admin.getPassword(),
                    Collections.emptyList() // Pas de rôles ou d'autorités
            );
        };
    }

    /*@Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Gestion automatisée des API Tierces")
                        .version("1.0.0")
                        .description("API pour la gestion automatisée des API Tierces.")
                        .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")));
    }*/
/*hedha sécurise les requetes*/
   /* @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList("basicAuth"))
                .components(new Components().addSecuritySchemes("basicAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("basic")
                ))
                .info(new Info()
                        .title("Gestion automatisée des API Tierces")
                        .version("1.0.0")
                        .description("API pour la gestion automatisée des API Tierces.")
                        .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")));
    }

}*/


import com.example.api_tierces.model.Admin;
import com.example.api_tierces.repository.AdminRepository;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Collections;

/*@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig implements WebMvcConfigurer {

    // Chemins Swagger: Maintenant considérés comme publics pour l'accès à l'interface
    private static final String[] SWAGGER_PATHS = {
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",       // Note: Laisser les docs publics peut être un risque de sécurité
            "/swagger-resources/**",
            "/admin/login",
            "/webjars/**"
    };

    // Autres chemins explicitement publics
    private static final String[] PUBLIC_PATHS = {
            "/error"
    };

    private final AdminRepository adminRepository;

    public SecurityConfig(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:4200")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Bean
    public BasicAuthenticationEntryPoint authenticationEntryPoint() {
        BasicAuthenticationEntryPoint entryPoint = new BasicAuthenticationEntryPoint();
        entryPoint.setRealmName("API Tierces Execution"); // Realm pour l'exécution
        return entryPoint;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Autoriser l'accès SANS authentification aux chemins publics ET Swagger UI
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        .requestMatchers(SWAGGER_PATHS).permitAll() // <-- CHANGEMENT ICI
                        // Exiger une authentification pour TOUTES les autres requêtes (vos API !)
                        .anyRequest().authenticated()
                )
                .httpBasic(basic -> basic
                        // Utiliser le bean pour déclencher la pop-up lors de l'accès à une ressource .authenticated()
                        .authenticationEntryPoint(authenticationEntryPoint())
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            Admin admin = adminRepository.findByUsername(username);
            if (admin == null) {
                throw new UsernameNotFoundException("Admin not found: " + username);
            }
            // Assurez-vous que admin.getPassword() retourne le HASH BCrypt
            return new User(
                    admin.getUsername(),
                    admin.getPassword(),
                    Collections.emptyList()
            );
        };
    }

    @Bean
    public OpenAPI customOpenAPI() {
        // NOTE : Laisser /v3/api-docs/** public peut être un risque.
        // Si vous voulez aussi sécuriser la définition de l'API elle-même,
        // il faudrait une configuration plus fine ou accepter de sécuriser aussi l'UI.
        // Pour cette demande spécifique, nous laissons les docs publics pour que l'UI se charge.
        return new OpenAPI()
                .info(new Info()
                        .title("Gestion automatisée des API Tierces")
                        .version("1.0.0")
                        .description("API pour la gestion automatisée des API Tierces.")
                        .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")));
    }
}*/

import com.example.api_tierces.model.Admin;
import com.example.api_tierces.repository.AdminRepository;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
// Import nécessaire pour Customizer
import org.springframework.security.config.Customizer;
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
// Imports pour la configuration CORS
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays; // Import pour Arrays.asList
import java.util.Collections;

/* hedhiiii jawhaaa behy te5dem w labes juste mochkolt rafraichi l page*/
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
// On peut retirer "implements WebMvcConfigurer" si on ne l'utilise que pour CORS
public class SecurityConfig /* implements WebMvcConfigurer */ {

    // Chemins publics incluant Swagger UI et l'endpoint de login
  private static final String[] SWAGGER_AND_LOGIN_PATHS = {
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/admin/login",
            "/admin/logout",
            "/admin/forgot-password",
            "/admin/reset-password",
            "/actuator/prometheus", /*a supprimer de ceci , accés sécurisé*/
            "/favicon.ico",
            "/webjars/**"
    };

    // Autres chemins VRAIMENT publics
    private static final String[] PUBLIC_PATHS = {
            "/error"
    };

    private final AdminRepository adminRepository;

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class); // Optional logging

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


    // Bean pour la configuration CORS utilisée par Spring Security
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // IMPORTANT: Remplacez par l'origine exacte de votre frontend Angular
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
        // Méthodes autorisées (inclure OPTIONS pour preflight)
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // Autoriser tous les en-têtes
        configuration.setAllowedHeaders(Arrays.asList("*"));
        // Autoriser les cookies et l'authentification
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Appliquer cette configuration à tous les chemins
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


    @Bean
    public BasicAuthenticationEntryPoint authenticationEntryPoint() {
        BasicAuthenticationEntryPoint entryPoint = new BasicAuthenticationEntryPoint();
        entryPoint.setRealmName("API Tierces Execution");
        return entryPoint;
    }

    /*@Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Intégrer la configuration CORS définie dans le bean corsConfigurationSource
                .cors(Customizer.withDefaults())
                // Désactiver CSRF
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Plus besoin d'autoriser OPTIONS explicitement ici, .cors() s'en charge
                        // .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        .requestMatchers(SWAGGER_AND_LOGIN_PATHS).permitAll()
                        .anyRequest().authenticated()
                )
                .httpBasic(basic -> basic
                        .authenticationEntryPoint(authenticationEntryPoint())
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }*/
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable) // Garder si besoin, mais considérer l'activation avec sessions
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(SWAGGER_AND_LOGIN_PATHS).permitAll()
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        .anyRequest().authenticated() // Tout le reste nécessite une authentification
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: Please log in.");
                        })
                );
        // La configuration de logout de Spring Security (.logout()) n'est pas utilisée ici
        // car on a un endpoint POST personnalisé dans AdminController

        return http.build();
    }

    /*@Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            Admin admin = adminRepository.findByUsername(username);
            if (admin == null) {
                throw new UsernameNotFoundException("Admin not found: " + username);
            }
            return new User(
                    admin.getUsername(),
                    admin.getPassword(), // Doit être le hash BCrypt
                    Collections.emptyList()
            );
        };
    }*/
    @Bean
    public UserDetailsService userDetailsService() {
        return email -> { // 'email' here is the identifier passed by Spring Security (from your login request)
            log.debug("UserDetailsService attempting to load user by email: {}", email);

            // --- FIX HERE: Handle Optional and Exception ---
            Admin admin = adminRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        log.warn("UserDetailsService: Admin not found with email: {}", email);
                        // **** Use UsernameNotFoundException ****
                        return new UsernameNotFoundException("Aucun administrateur trouvé avec l'email : " + email);
                    });
            // --- END FIX ---

            log.debug("UserDetailsService: Admin found for email: {}. Creating UserDetails.", email);

            // Create Spring Security User object
            return new User(
                    admin.getEmail(),        // Use email as the principal's identifier for Spring Security
                    admin.getPassword(),     // The HASHED password from the database
                    Collections.emptyList() // TODO: Replace with actual authorities/roles if needed
                    // Example: admin.getRoles().stream()
                    //          .map(role -> new SimpleGrantedAuthority(role.getName()))
                    //          .collect(Collectors.toList())
            );
        };
    }


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
