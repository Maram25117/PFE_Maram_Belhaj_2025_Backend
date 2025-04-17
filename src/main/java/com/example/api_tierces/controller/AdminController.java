package com.example.api_tierces.controller;

import com.example.api_tierces.model.Admin;
import com.example.api_tierces.model.LoginRequest;
import com.example.api_tierces.model.ResetPasswordRequest;
import com.example.api_tierces.service.AdminServiceImpl;
import io.swagger.annotations.ApiResponse;
/*import io.swagger.io.Authentication;*/
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger; // Utilisation de SLF4J pour le logging
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.example.api_tierces.service.AdminService; // Interface pour les services
import com.example.api_tierces.service.AdminServiceImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.example.api_tierces.service.AdminService;
import java.util.Collections;
// Imports Jakarta Servlet nécessaires
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.util.HashMap;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import java.util.Map;
import java.util.Optional;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import static org.apache.http.impl.auth.BasicScheme.authenticate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminServiceImpl adminService;

    @Autowired
    private AdminService emailService;
    private final PasswordEncoder passwordEncoder;
    private static final Logger log = LoggerFactory.getLogger(AdminController.class);
    //private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


    private final AuthenticationManager authenticationManager;

    @Autowired // @Autowired est optionnel ici s'il n'y a qu'un seul constructeur
    public AdminController(AuthenticationManager authenticationManager,
                           AdminService adminService, // Injecter l'interface AdminService
                           PasswordEncoder passwordEncoder, // Injecter l'interface PasswordEncoder
                           AdminService emailService) { // Injecter l'interface emailService
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService; // Assigner le service email injecté
    }




    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody Admin admin) {
        // Validation des champs obligatoires
        if (admin.getPassword() == null || admin.getUsername() == null || admin.getEmail() == null) {
            return ResponseEntity.badRequest().body("Username, email, and password cannot be null");
        }

        if (admin.getUsername().length() < 3) {
            return ResponseEntity.badRequest().body("Username must be at least 3 characters long");
        }

        // Vérification de l'existence de l'email ou du nom d'utilisateur
        if (adminService.findByUsername(admin.getUsername()) != null) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        if (adminService.findByEmail(admin.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        // Encoder le mot de passe avant de l'enregistrer
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        adminService.save(admin);
        return ResponseEntity.ok("User registered successfully");
    }


    @PutMapping("/update/{id}")
    public ResponseEntity<Map<String, Object>> updateAdmin(@PathVariable Long id, @RequestBody Admin updatedAdmin) {
        Admin existingAdmin = adminService.findById(id);
        Map<String, Object> response = new HashMap<>();

        if (existingAdmin == null) {
            response.put("message", "Admin non trouvé");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        // Mise à jour des informations
        existingAdmin.setUsername(updatedAdmin.getUsername());
        existingAdmin.setEmail(updatedAdmin.getEmail());
        if (updatedAdmin.getPassword() != null && !updatedAdmin.getPassword().isEmpty()) {
            existingAdmin.setPassword(passwordEncoder.encode(updatedAdmin.getPassword()));  // Encodage du mot de passe
        }

        adminService.save(existingAdmin);

        response.put("message", "Admin mis à jour avec succès");
        response.put("admin", existingAdmin);
        return ResponseEntity.ok(response);
    }




    /*@GetMapping("/profile/{id}")
    public ResponseEntity<Admin> getAdminProfile(@PathVariable Long id) {
        Admin admin = adminService.findById(id);
        if (admin == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(admin);
    }*/
    @GetMapping("/profile/{id}")
    public ResponseEntity<Admin> getAdminProfile(@PathVariable Long id) {
        // Ce endpoint est protégé par .anyRequest().authenticated() dans SecurityConfig
        // Spring Security vérifiera la session AVANT d'appeler cette méthode.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.debug("Accessing profile for ID: {} by authenticated user: {}", id, (authentication != null ? authentication.getName() : "N/A"));

        Admin admin = adminService.findById(id);
        if (admin == null) {
            log.warn("Admin profile not found for ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        // Bonne pratique : Ne pas renvoyer le mot de passe, même hashé. Utiliser un DTO.
        // Pour l'instant, on renvoie l'objet tel quel pour la simplicité de l'exemple.
        log.debug("Returning profile for admin ID: {}", id);
        return ResponseEntity.ok(admin);
    }


    /*@PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email) {
        return emailService.generateResetToken(email);
    }*/
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestParam String email) {
        try {
            log.info("Requête /forgot-password reçue pour l'email: {}", email);
            String serviceResult = emailService.generateResetToken(email); // Appel service
            log.debug("Résultat retourné par adminService.generateResetToken pour {}: {}", email, serviceResult);

            if ("E-mail non trouvé.".equals(serviceResult)) {
                // --- CAS EMAIL NON TROUVÉ ---
                log.warn("Réponse pour /forgot-password (email: {}): Email non trouvé (404)", email);
                // 1. Crée un corps de réponse JSON: {"message": "Aucun compte trouvé pour cet email."}
                Map<String, String> responseBody = Collections.singletonMap("message", "Aucun compte trouvé pour cet email.");
                // 2. Retourne une ResponseEntity avec:
                //    - Statut: 404 Not Found
                //    - Corps: le JSON créé ci-dessus
                //    - Content-Type: application/json (normalement ajouté par Spring)
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseBody);

            } else if ("Un e-mail de réinitialisation a été envoyé.".equals(serviceResult)) {
                // --- CAS SUCCÈS ---
                log.info("Réponse pour /forgot-password (email: {}): Succès (200)", email);
                // 1. Crée un corps de réponse JSON: {"message": "Si un compte..."}
                Map<String, String> responseBody = Collections.singletonMap("message", "Si un compte correspondant à cet email existe, les instructions de réinitialisation ont été envoyées.");
                // 2. Retourne une ResponseEntity avec:
                //    - Statut: 200 OK
                //    - Corps: le JSON créé ci-dessus
                //    - Content-Type: application/json (normalement ajouté par Spring)
                return ResponseEntity.ok(responseBody);

            } else {
                // ... gestion erreur 500 ...
                Map<String, String> responseBody = Collections.singletonMap("message", "Réponse inattendue du serveur.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
            }

        } catch (Exception e) {
            // ... gestion exception 500 ...
            Map<String, String> responseBody = Collections.singletonMap("message", "Une erreur serveur interne est survenue.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
        }
    }
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        // Cette classe 'ResetPasswordRequest' devrait contenir le token et le nouveau mot de passe.
        String response = emailService.resetPassword(request.getToken(), request.getNewPassword());
        if (response.equals("Mot de passe réinitialisé avec succès.")) {
            return ResponseEntity.ok("Mot de passe réinitialisé avec succès.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token invalide ou expiré.");
        }
    }


     /*hedhi login shiha feha session*/
    /*@PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest loginRequest, HttpSession session) {
        Admin admin = adminService.findByUsername(loginRequest.getUsername());

        if (admin != null && passwordEncoder.matches(loginRequest.getPassword(), admin.getPassword())) {
            // Stocker l'ID de l'utilisateur dans la session
            session.setAttribute("adminId", admin.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("id", admin.getId());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("message", "Invalid credentials"));
        }
    }*/
    /*@PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody LoginRequest loginRequest,
            HttpServletRequest request, // Injecter request et response
            HttpServletResponse response) {

        try {
            // 1. Créer un token de demande d'authentification
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(), loginRequest.getPassword());

            // 2. Authentifier via AuthenticationManager (utilise votre UserDetailsService et PasswordEncoder)
            Authentication authentication = authenticationManager.authenticate(authToken);

            // 3. Mettre à jour le SecurityContext
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            // 4. Sauvegarder explicitement le contexte dans la session HTTP
            // Ceci est crucial car nous sommes en dehors du flux standard des filtres Spring Sec ici.
            HttpSessionSecurityContextRepository repo = new HttpSessionSecurityContextRepository();
            repo.saveContext(context, request, response);

            // 5. Récupérer l'ID depuis l'objet Authentication (si nécessaire)
            // Ceci dépend de ce que votre UserDetailsService retourne comme Principal
            Long adminId = null;
            if (authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.User) {
                // Si le principal est un UserDetails standard, le username est dedans.
                // Vous devrez peut-être rechercher l'ID à nouveau basé sur le username.
                Admin admin = adminService.findByUsername(authentication.getName());
                if (admin != null) {
                    adminId = admin.getId();
                    // Optionnel: Vous pouvez toujours mettre l'ID dans la session si vous le voulez
                    // request.getSession().setAttribute("adminId", adminId);
                } else {
                    // Cas étrange où l'authentification réussit mais l'admin n'est pas trouvé ensuite
                    throw new RuntimeException("Admin principal found but could not retrieve full admin details.");
                }
            } else {
                // Gérer d'autres types de Principal si votre UserDetailsService est différent
                // ou si vous avez stocké l'ID directement dans le Principal.
                throw new RuntimeException("Unexpected principal type after authentication: " + authentication.getPrincipal().getClass());
            }


            // 6. Préparer la réponse
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("message", "Login successful");
            responseBody.put("id", adminId); // Renvoyer l'ID au frontend
            return ResponseEntity.ok(responseBody);

        } catch (Exception e) { // Capturer les AuthenticationException, etc.
            SecurityContextHolder.clearContext(); // Nettoyer en cas d'échec
            // Log l'erreur côté serveur
            System.err.println("Authentication failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("message", "Invalid credentials"));
        }
    }*/
    /*@PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody LoginRequest loginRequest,
            HttpServletRequest request,
            HttpServletResponse response) {

        try {
            log.debug("Attempting login for user: {}", loginRequest.getUsername());
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(), loginRequest.getPassword());
            Authentication authentication = authenticationManager.authenticate(authToken);
            log.debug("Authentication successful for user: {}", authentication.getName());
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            HttpSessionSecurityContextRepository repo = new HttpSessionSecurityContextRepository();
            repo.saveContext(context, request, response);
            log.debug("SecurityContext saved in HTTP Session for user: {}", authentication.getName());
            Long adminId = null;
            Object principal = authentication.getPrincipal();

            if (principal instanceof org.springframework.security.core.userdetails.User) {
                String username = ((org.springframework.security.core.userdetails.User) principal).getUsername();
                log.debug("Principal is UserDetails, finding Admin ID for username: {}", username);
                Admin admin = adminService.findByUsername(username);
                if (admin != null) {
                    adminId = admin.getId();
                    log.debug("Admin ID found: {}", adminId);
                } else {
                    log.error("Authentication principal found but could not retrieve full admin details for username: {}", username);
                    throw new RuntimeException("Admin principal found but could not retrieve full admin details.");
                }
            } else {
                log.error("Unexpected principal type after authentication: {}", principal.getClass().getName());
                throw new RuntimeException("Unexpected principal type after authentication: " + principal.getClass().getName());
            }
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("message", "Login successful");
            responseBody.put("id", adminId);
            log.info("Login successful for admin ID: {}", adminId);
            return ResponseEntity.ok(responseBody);

        } catch (AuthenticationException e) {
            SecurityContextHolder.clearContext();
            log.warn("Authentication failed for user {}: {}", loginRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("message", "Invalid credentials"));
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            log.error("Unexpected error during login for user {}: {}", loginRequest.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "An internal error occurred during login."));
        }
    }*/
     @PostMapping("/login")
     public ResponseEntity<Map<String, Object>> login(
             @RequestBody LoginRequest loginRequest, // Utilise maintenant le DTO avec email
             HttpServletRequest request,
             HttpServletResponse response) {

         try {
             log.debug("Attempting login for email: {}", loginRequest.getEmail()); // Log l'email
             // 1. Créer un token avec l'EMAIL comme identifiant principal
             UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                     loginRequest.getEmail(), // Utiliser l'email ici
                     loginRequest.getPassword());

             // 2. Authentifier (le UserDetailsService cherchera par email maintenant)
             Authentication authentication = authenticationManager.authenticate(authToken);
             // authentication.getName() retournera l'email utilisé dans le token
             log.debug("Authentication successful for email: {}", authentication.getName());

             // 3. Mettre à jour le SecurityContext
             SecurityContext context = SecurityContextHolder.createEmptyContext();
             context.setAuthentication(authentication);
             SecurityContextHolder.setContext(context);

             // 4. Sauvegarder explicitement le contexte dans la session HTTP
             HttpSessionSecurityContextRepository repo = new HttpSessionSecurityContextRepository();
             repo.saveContext(context, request, response);
             log.debug("SecurityContext saved in HTTP Session for email: {}", authentication.getName());


             // 5. Récupérer l'ID depuis l'objet Authentication en utilisant l'email
             Long adminId = null;
             String authenticatedEmail = authentication.getName(); // Ceci est l'email

             log.debug("Principal is UserDetails, finding Admin ID for email: {}", authenticatedEmail);
             // Utiliser la méthode findByEmail du service
             Optional<Admin> adminOptional = adminService.findByEmail(authenticatedEmail);

             if (adminOptional.isPresent()) {
                 adminId = adminOptional.get().getId();
                 log.debug("Admin ID found: {}", adminId);
             } else {
                 // Ce cas est TRÈS improbable si l'authentification a réussi,
                 // mais c'est une sécurité supplémentaire.
                 log.error("Authentication principal found but could not retrieve full admin details for email: {}", authenticatedEmail);
                 // Ne pas lancer une exception ici si l'authentification a réussi, logguer l'erreur suffit peut-être.
                 // Si l'ID est crucial, alors une erreur 500 est justifiable.
                 throw new RuntimeException("Admin principal found but could not retrieve full admin details after successful authentication.");
             }

             // 6. Préparer la réponse
             Map<String, Object> responseBody = new HashMap<>();
             responseBody.put("message", "Login successful");
             responseBody.put("id", adminId); // L'ID récupéré
             log.info("Login successful for admin ID: {} (Email: {})", adminId, authenticatedEmail);
             return ResponseEntity.ok(responseBody);

         } catch (AuthenticationException e) {
             SecurityContextHolder.clearContext();
             log.warn("Authentication failed for email {}: {}", loginRequest.getEmail(), e.getMessage());
             return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                     .body(Collections.singletonMap("message", "Identifiants invalides")); // Message plus générique
         } catch (Exception e) {
             SecurityContextHolder.clearContext();
             log.error("Unexpected error during login for email {}: {}", loginRequest.getEmail(), e.getMessage(), e);
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                     .body(Collections.singletonMap("message", "Une erreur interne est survenue pendant la connexion."));
         }
     }

    /*@PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        // Invalidates the session, effectively logging out the user
        session.invalidate();
        return ResponseEntity.ok("Logged out successfully");
    }*/
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        // Le endpoint est public (configuré dans SecurityConfig)
        // Invalider la session est la bonne approche
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = (authentication != null) ? authentication.getName() : "Unknown user"; // Pour le log

        log.info("Logout request received for user: {}", username);
        session.invalidate(); // Détruit la session
        log.info("Session invalidated for user: {}", username);
        // Optionnel : Nettoyer le contexte de sécurité pour la requête courante, bien que la session soit partie
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok("Logged out successfully");
    }


}





