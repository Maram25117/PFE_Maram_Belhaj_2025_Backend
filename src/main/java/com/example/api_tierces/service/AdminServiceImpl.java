package com.example.api_tierces.service;

import com.example.api_tierces.model.Admin;
import com.example.api_tierces.repository.AdminRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
@Service
public class AdminServiceImpl {

    @Autowired
    private AdminRepository adminRepository;


    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Admin admin = adminRepository.findByUsername(username);
        if (admin == null) {
            throw new UsernameNotFoundException("User not found");
        }

        return User.withUsername(admin.getUsername())
                .password(admin.getPassword())
                .roles("ADMIN") // Ajoute un rôle par défaut
                .build();
    }

    @Transactional
    public void save(Admin admin) {
        // Sauvegarder le mot de passe sans encodage
        adminRepository.save(admin);
    } //void : car elle ne retourne rien


    public Admin findByUsername(String username) {
        return adminRepository.findByUsername(username);
    }

    public Admin findById(Long id) {
        Optional<Admin> admin = adminRepository.findById(id);
        return admin.orElse(null);
    }


    public Optional<Admin> findByEmail(String email) {
        return adminRepository.findByEmail(email);
    }

}
/*package com.example.api_tierces.service;

import com.example.api_tierces.model.Admin;
import com.example.api_tierces.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdminServiceImpl {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AdminServiceImpl(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return adminRepository.findByUsername(username)
                .map(admin -> User.withUsername(admin.getUsername())
                        .password(admin.getPassword())
                        .roles("ADMIN")
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    public Admin save(Admin admin) {
        // Encoder le mot de passe avant sauvegarde
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        return adminRepository.save(admin);
    }

    public Optional<Admin> findByUsername(String username) {
        return adminRepository.findByUsername(username);
    }

    public Admin findById(Long id) {
        return adminRepository.findById(id).orElse(null);
    }

    public Optional<Admin> findByEmail(String email) {
        return adminRepository.findByEmail(email);
    }
}*/
