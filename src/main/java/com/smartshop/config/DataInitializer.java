package com.smartshop.config;

import com.smartshop.entity.User;
import com.smartshop.entity.UserRole;
import com.smartshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) {
        // Vérifier si l'admin existe déjà
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = User.builder()
                    .username("admin")
                    .password("admin123")
                    .role(UserRole.ADMIN)
                    .build();

            userRepository.save(admin);
            System.out.println("=== ADMIN UTILISATEUR CRÉÉ ===");
            System.out.println("Username: admin");
            System.out.println("Password: admin123");
            System.out.println("Role: ADMIN");
        } else {
            System.out.println("=== ADMIN EXISTE DÉJÀ ===");
        }


    }
}