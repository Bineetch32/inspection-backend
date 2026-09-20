package com.inspection.backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.inspection.backend.model.AppUser;
import com.inspection.backend.repository.AppUserRepository;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner createAdminFromEnvironment(AppUserRepository repository) {
        return args -> {

            String username = System.getenv("INSPECTION_ADMIN_USERNAME");
            String rawPassword = System.getenv("INSPECTION_ADMIN_PASSWORD");

            if (username == null || username.isBlank()
                    || rawPassword == null || rawPassword.isBlank()) {
                return;
            }

            if (repository.findByUsernameIgnoreCase(username.trim()).isPresent()) {
                return;
            }

            BCryptPasswordEncoder encoder =
                    new BCryptPasswordEncoder();

            AppUser admin = new AppUser();
            admin.setUsername(username.trim());
            admin.setPassword(encoder.encode(rawPassword));
            admin.setRole("ADMIN");
            admin.setActive(true);

            repository.save(admin);
        };
    }
}
