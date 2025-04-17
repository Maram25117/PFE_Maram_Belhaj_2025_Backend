package com.example.api_tierces.service;

import com.example.api_tierces.model.Admin;
import com.example.api_tierces.repository.AdminRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.UUID;

/*@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private EmailService emailService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public String generateResetToken(String email) {
        Optional<Admin> adminOpt = adminRepository.findByEmail(email);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            String token = UUID.randomUUID().toString();
            admin.setResetToken(token);
            adminRepository.save(admin);

            // Envoi de l'email
            String resetLink = token;
            String message = "Token pour rénitialiser votre mot de passe : " + resetLink;
            emailService.sendEmail(admin.getEmail(), "Réinitialisation du mot de passe", message);

            return "Un e-mail de réinitialisation a été envoyé.";
        } else {
            return "E-mail non trouvé.";
        }
    }

    @Transactional
    public String resetPassword(String token, String newPassword) {
        Optional<Admin> adminOpt = adminRepository.findByResetToken(token);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            admin.setPassword(passwordEncoder.encode(newPassword));
            admin.setResetToken(null); // Supprimer le token après utilisation
            adminRepository.save(admin);
            return "Mot de passe réinitialisé avec succès.";
        } else {
            return "Token invalide.";
        }
    }

}*/


import com.example.api_tierces.model.Admin;
import com.example.api_tierces.repository.AdminRepository;
// Correction de l'import jakarta si vous utilisez Spring Boot 3+
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
// Importez le Logger
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // Gardez si utilisé ailleurs
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.UUID;

import com.example.api_tierces.model.Admin;
import com.example.api_tierces.repository.AdminRepository;
import jakarta.transaction.Transactional; // Correction import si Spring Boot 3+
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder; // <<<--- Importer l'interface PasswordEncoder
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;

@Service
public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private EmailService emailService; // Assurez-vous que ce service existe et fonctionne

    // --- Injection du Bean PasswordEncoder ---
    // Spring va injecter l'instance BCryptPasswordEncoder que vous avez définie
    // dans votre classe de configuration de sécurité.
    @Autowired
    private PasswordEncoder passwordEncoder; // <<<--- Injecter le Bean via l'interface

    @Transactional
    public String generateResetToken(String email) {
        log.debug("Tentative de génération de token pour l'email: {}", email);
        Optional<Admin> adminOpt = adminRepository.findByEmail(email);
        if (adminOpt.isPresent()) {
            // --- Email trouvé ---
            Admin admin = adminOpt.get();
            log.info("Admin trouvé pour l'email: {}. Génération du token.", email);
            String token = UUID.randomUUID().toString();
            admin.setResetToken(token);
            adminRepository.save(admin);
            log.debug("Token sauvegardé pour l'admin ID: {}", admin.getId());

            // --- Envoi de l'email ---
            String resetLink = token;
            String message = "Voici votre token pour réinitialiser votre mot de passe : " + resetLink;
            String subject = "Réinitialisation du mot de passe - APIMonitor";

            try {
                emailService.sendEmail(admin.getEmail(), subject, message);
                log.info("Email de réinitialisation envoyé avec succès à {}", email);
                return "Un e-mail de réinitialisation a été envoyé.";
            } catch (Exception e) {
                log.error("ERREUR lors de l'envoi de l'email à {}: {}", email, e.getMessage());
                // Considérer de retourner une chaîne d'erreur différente ici
                return "Erreur lors de l'envoi de l'email."; // Ex: Retourner ceci
                // Ou retourner le message succès, mais ce n'est pas idéal
                // return "Un e-mail de réinitialisation a été envoyé.";
            }
        } else {
            // --- Email NON trouvé ---
            log.warn("Aucun admin trouvé pour l'email: {}", email);
            return "E-mail non trouvé.";
        }
    }

    @Transactional
    public String resetPassword(String token, String newPassword) {
        log.debug("Tentative de réinitialisation de mot de passe pour le token: {}", token);
        Optional<Admin> adminOpt = adminRepository.findByResetToken(token);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            log.info("Token valide trouvé pour l'admin ID: {}. Mise à jour du mot de passe.", admin.getId());

            // --- Utiliser directement le bean PasswordEncoder injecté ---
            admin.setPassword(passwordEncoder.encode(newPassword)); // <<<--- CORRECTION ICI
            admin.setResetToken(null); // Supprimer le token après utilisation
            adminRepository.save(admin);

            log.info("Mot de passe réinitialisé avec succès pour l'admin ID: {}", admin.getId());
            return "Mot de passe réinitialisé avec succès.";
        } else {
            log.warn("Token de réinitialisation invalide ou expiré: {}", token);
            return "Token invalide ou expiré.";
        }
    }

    // --- PAS BESOIN de la méthode helper encodePassword NI du champ BCryptPasswordEncoder ici ---
    // // Méthode helper ou injection du Bean PasswordEncoder
    // @Autowired
    // private BCryptPasswordEncoder passwordEncoderBean; // SUPPRIMER
    // private String encodePassword(String rawPassword) { // SUPPRIMER
    //     return passwordEncoderBean.encode(rawPassword); // SUPPRIMER
    // }

}