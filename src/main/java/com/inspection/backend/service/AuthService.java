package com.inspection.backend.service;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.inspection.backend.model.AppUser;
import com.inspection.backend.repository.AppUserRepository;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    public AuthService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    public Map<String, Object> login(String username, String password) {

        AppUser user = appUserRepository
                .findByUsernameIgnoreCase(username == null ? "" : username.trim())
                .orElse(null);

        if (user == null || !user.isActive()
                || password == null
                || !passwordEncoder.matches(password, user.getPassword())) {

            throw new IllegalArgumentException(
                    "Invalid username or password.");
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("username", user.getUsername());
        response.put("role", user.getRole());
        response.put("active", user.isActive());

        return response;
    }
}
