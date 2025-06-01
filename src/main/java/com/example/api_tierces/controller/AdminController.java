package com.example.api_tierces.controller;

import com.example.api_tierces.model.Admin;
import com.example.api_tierces.model.LoginRequest;
import com.example.api_tierces.model.ResetPasswordRequest;
import com.example.api_tierces.service.AdminServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.example.api_tierces.service.AdminService;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.HashMap;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import java.util.Map;
import java.util.Optional;

@Tag(name = "Gestion d'Admin")
@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminServiceImpl adminServiceImpl;
    private final AdminService adminService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);


    @Autowired
    public AdminController(
            AuthenticationManager authenticationManager,
            AdminServiceImpl adminServiceImpl,
            AdminService adminService,
            PasswordEncoder passwordEncoder
    ) {
        this.authenticationManager = authenticationManager;
        this.adminServiceImpl = adminServiceImpl;
        this.adminService = adminService;
        this.passwordEncoder = passwordEncoder;
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
        if (adminServiceImpl.findByUsername(admin.getUsername()) != null) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        if (adminServiceImpl.findByEmail(admin.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already exists");
        }
        // Encoder le mot de passe avant de l'enregistrer
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        adminServiceImpl.save(admin);
        return ResponseEntity.ok("User registered successfully");
    }


    /*@PutMapping("/update/{id}")
    public ResponseEntity<Map<String, Object>> updateAdmin(@PathVariable Long id, @RequestBody Admin updatedAdmin) {
        Admin existingAdmin = adminServiceImpl.findById(id);
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
        adminServiceImpl.save(existingAdmin);

        response.put("message", "Admin mis à jour avec succès");
        response.put("admin", existingAdmin);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/profile/{id}")
    public ResponseEntity<Admin> getAdminProfile(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.debug("Accessing profile for ID: {} by authenticated user: {}", id, (authentication != null ? authentication.getName() : "N/A"));

        Admin admin = adminServiceImpl.findById(id);
        if (admin == null) {
            log.warn("Admin profile not found for ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        log.debug("Returning profile for admin ID: {}", id);
        return ResponseEntity.ok(admin);
    }*/


    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestParam String email) {
        try {
            log.info("Requête /forgot-password reçue pour l'email: {}", email);
            String serviceResult = adminService.generateResetToken(email);
            log.debug("Résultat retourné par adminService.generateResetToken pour {}: {}", email, serviceResult);

            if ("E-mail non trouvé.".equals(serviceResult)) {
                // CAS EMAIL NON TROUVÉ
                log.warn("Réponse pour /forgot-password (email: {}): Email non trouvé (404)", email);
                Map<String, String> responseBody = Collections.singletonMap("message", "Aucun compte trouvé pour cet email.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseBody);

            } else if ("Un e-mail de réinitialisation a été envoyé.".equals(serviceResult)) {
                // CAS SUCCÈS
                log.info("Réponse pour /forgot-password (email: {}): Succès (200)", email);
                Map<String, String> responseBody = Collections.singletonMap("message", "Si un compte correspondant à cet email existe, les instructions de réinitialisation ont été envoyées.");
                return ResponseEntity.ok(responseBody);

            } else {
                Map<String, String> responseBody = Collections.singletonMap("message", "Réponse inattendue du serveur.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
            }

        } catch (Exception e) {
            Map<String, String> responseBody = Collections.singletonMap("message", "Une erreur serveur interne est survenue.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
        }
    }


    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        String response = adminService.resetPassword(request.getToken(), request.getNewPassword());
        if (response.equals("Mot de passe réinitialisé avec succès.")) {
            return ResponseEntity.ok("Mot de passe réinitialisé avec succès.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token invalide ou expiré.");
        }
    }


     @PostMapping("/login")
     public ResponseEntity<Map<String, Object>> login(
             @RequestBody LoginRequest loginRequest,
             HttpServletRequest request,
             HttpServletResponse response) {

         try {
             log.debug("Attempting login for email: {}", loginRequest.getEmail());
             UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                     loginRequest.getEmail(),
                     loginRequest.getPassword());

             Authentication authentication = authenticationManager.authenticate(authToken);
             log.debug("Authentication successful for email: {}", authentication.getName());

             SecurityContext context = SecurityContextHolder.createEmptyContext();
             context.setAuthentication(authentication);
             SecurityContextHolder.setContext(context);

             HttpSessionSecurityContextRepository repo = new HttpSessionSecurityContextRepository();
             repo.saveContext(context, request, response);
             log.debug("SecurityContext saved in HTTP Session for email: {}", authentication.getName());

             Long adminId = null;
             String authenticatedEmail = authentication.getName();

             log.debug("Principal is UserDetails, finding Admin ID for email: {}", authenticatedEmail);
             Optional<Admin> adminOptional = adminServiceImpl.findByEmail(authenticatedEmail);

             if (adminOptional.isPresent()) {
                 adminId = adminOptional.get().getId();
                 log.debug("Admin ID found: {}", adminId);
             } else {
                 log.error("Authentication principal found but could not retrieve full admin details for email: {}", authenticatedEmail);
                 throw new RuntimeException("Admin principal found but could not retrieve full admin details after successful authentication.");
             }

             Map<String, Object> responseBody = new HashMap<>();
             responseBody.put("message", "Login successful");
             responseBody.put("id", adminId);
             log.info("Login successful for admin ID: {} (Email: {})", adminId, authenticatedEmail);
             return ResponseEntity.ok(responseBody);

         } catch (AuthenticationException e) {
             SecurityContextHolder.clearContext();
             log.warn("Authentication failed for email {}: {}", loginRequest.getEmail(), e.getMessage());
             return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                     .body(Collections.singletonMap("message", "Identifiants invalides"));
         } catch (Exception e) {
             SecurityContextHolder.clearContext();
             log.error("Unexpected error during login for email {}: {}", loginRequest.getEmail(), e.getMessage(), e);
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                     .body(Collections.singletonMap("message", "Une erreur interne est survenue pendant la connexion."));
         }
     }


    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = (authentication != null) ? authentication.getName() : "Unknown user";

        log.info("Logout request received for user: {}", username);
        session.invalidate();
        log.info("Session invalidated for user: {}", username);
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok("Logged out successfully");
    }


    /*nouveauuuuuuuuuuu*/
    @PutMapping("/update/info/{id}")
    public ResponseEntity<Map<String, Object>> updateAdminInfo(@PathVariable Long id, @RequestBody Admin updatedAdmin) {
        Admin existingAdmin = adminServiceImpl.findById(id);
        Map<String, Object> response = new HashMap<>();

        if (existingAdmin == null) {
            response.put("message", "Admin non trouvé");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        // Mise à jour des informations hors mot de passe
        existingAdmin.setUsername(updatedAdmin.getUsername());
        existingAdmin.setEmail(updatedAdmin.getEmail());

        adminServiceImpl.save(existingAdmin);

        response.put("message", "Informations mises à jour avec succès");
        response.put("admin", existingAdmin);
        return ResponseEntity.ok(response);
    }



    @PutMapping("/update/password/{id}")
    public ResponseEntity<Map<String, Object>> updatePassword(@PathVariable Long id, @RequestBody Map<String, String> passwordData) {
        Admin existingAdmin = adminServiceImpl.findById(id);
        Map<String, Object> response = new HashMap<>();

        if (existingAdmin == null) {
            response.put("message", "Admin non trouvé");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        String oldPassword = passwordData.get("oldPassword");
        String newPassword = passwordData.get("newPassword");

        if (oldPassword == null || newPassword == null || oldPassword.isEmpty() || newPassword.isEmpty()) {
            response.put("message", "Les mots de passe ne doivent pas être vides");
            return ResponseEntity.badRequest().body(response);
        }

        // Vérification de l'ancien mot de passe
        if (!passwordEncoder.matches(oldPassword, existingAdmin.getPassword())) {
            response.put("message", "Ancien mot de passe incorrect");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // Mise à jour du mot de passe
        existingAdmin.setPassword(passwordEncoder.encode(newPassword));
        adminServiceImpl.save(existingAdmin);

        response.put("message", "Mot de passe mis à jour avec succès");
        return ResponseEntity.ok(response);
    }

}





