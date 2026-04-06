package com.spendsmart.authservice.service.impl;

import com.spendsmart.authservice.domain.User;
import com.spendsmart.authservice.repository.UserRepository;
import com.spendsmart.authservice.security.JwtTokenService;
import com.spendsmart.authservice.service.AuthService;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtTokenService jwtTokenService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthServiceImpl(UserRepository userRepository, JwtTokenService jwtTokenService) {
        this.userRepository = userRepository;
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public User register(User user, String rawPassword) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        return userRepository.save(user);
    }

    @Override
    public String login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .filter(User::isActive)
                .orElseThrow(() -> new EntityNotFoundException("Active user not found for provided email"));

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        return jwtTokenService.generateToken(user.getUserId(), user.getEmail());
    }

    @Override
    public void logout(String token) {
        jwtTokenService.revoke(token);
    }

    @Override
    public boolean validateToken(String token) {
        return jwtTokenService.isValid(token);
    }

    @Override
    public String refreshToken(String token) {
        if (!jwtTokenService.isValid(token)) {
            throw new IllegalArgumentException("Invalid token");
        }

        Long userId = jwtTokenService.extractUserId(token);
        User user = userRepository.findByUserId(userId)
                .filter(User::isActive)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        jwtTokenService.revoke(token);
        return jwtTokenService.generateToken(user.getUserId(), user.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long userId) {
        return userRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Long getUserIdFromToken(String token) {
        if (!jwtTokenService.isValid(token)) {
            throw new IllegalArgumentException("Invalid token");
        }
        return jwtTokenService.extractUserId(token);
    }

    @Override
    public String getEmailFromToken(String token) {
        if (!jwtTokenService.isValid(token)) {
            throw new IllegalArgumentException("Invalid token");
        }
        return jwtTokenService.extractEmail(token);
    }

    @Override
    public User updateProfile(Long userId, User profile) {
        User existing = userRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        existing.setFullName(profile.getFullName());
        existing.setAvatarUrl(profile.getAvatarUrl());
        existing.setBio(profile.getBio());
        existing.setTimezone(profile.getTimezone());

        if (profile.getEmail() != null && !profile.getEmail().equalsIgnoreCase(existing.getEmail())) {
            if (userRepository.existsByEmail(profile.getEmail())) {
                throw new IllegalArgumentException("Email is already in use");
            }
            existing.setEmail(profile.getEmail());
        }

        return userRepository.save(existing);
    }

    @Override
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User existing = userRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!passwordEncoder.matches(currentPassword, existing.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is invalid");
        }

        existing.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(existing);
    }

    @Override
    public User updateCurrency(Long userId, String currency) {
        User existing = userRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        existing.setCurrency(currency);
        return userRepository.save(existing);
    }

    @Override
    public User updateMonthlyBudget(Long userId, BigDecimal monthlyBudget) {
        User existing = userRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        existing.setMonthlyBudget(monthlyBudget == null ? BigDecimal.ZERO : monthlyBudget);
        return userRepository.save(existing);
    }

    @Override
    public void deactivateAccount(Long userId) {
        User existing = userRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        existing.setActive(false);
        userRepository.save(existing);
    }
}
