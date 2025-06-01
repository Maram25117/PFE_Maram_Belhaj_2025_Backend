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
                .roles("ADMIN")
                .build();
    }

    @Transactional
    public void save(Admin admin) {
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
