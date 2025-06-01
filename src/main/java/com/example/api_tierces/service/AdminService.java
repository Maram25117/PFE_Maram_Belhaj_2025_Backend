package com.example.api_tierces.service;

import com.example.api_tierces.model.Admin;
import com.example.api_tierces.repository.AdminRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;


@Service
public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private EmailService emailService;


    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public String generateResetToken(String email) {
        log.debug("Tentative de génération de token pour l'email: {}", email);
        Optional<Admin> adminOpt = adminRepository.findByEmail(email);
        if (adminOpt.isPresent()) {
            // Email trouvé
            Admin admin = adminOpt.get();
            log.info("Admin trouvé pour l'email: {}. Génération du token.", email);
            String token = UUID.randomUUID().toString();
            admin.setResetToken(token);
            adminRepository.save(admin);
            log.debug("Token sauvegardé pour l'admin ID: {}", admin.getId());

            // Envoi de l'email
            String resetLink = token;
            String message = "Voici votre token pour réinitialiser votre mot de passe : " + resetLink;
            String subject = "Réinitialisation du mot de passe - APIMonitor";

            try {
                emailService.sendEmail(admin.getEmail(), subject, message);
                log.info("Email de réinitialisation envoyé avec succès à {}", email);
                return "Un e-mail de réinitialisation a été envoyé.";
            } catch (Exception e) {
                log.error("ERREUR lors de l'envoi de l'email à {}: {}", email, e.getMessage());
                return "Erreur lors de l'envoi de l'email.";

            }
        } else {
            // Email NON trouvé
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
            admin.setPassword(passwordEncoder.encode(newPassword));
            admin.setResetToken(null);
            adminRepository.save(admin);

            log.info("Mot de passe réinitialisé avec succès pour l'admin ID: {}", admin.getId());
            return "Mot de passe réinitialisé avec succès.";
        } else {
            log.warn("Token de réinitialisation invalide ou expiré: {}", token);
            return "Token invalide ou expiré.";
        }
    }

}