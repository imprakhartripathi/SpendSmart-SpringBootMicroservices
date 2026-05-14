package com.spendsmart.authservice.service;

import com.spendsmart.authservice.domain.User;
import java.math.BigDecimal;
import java.util.Optional;

public interface AuthService {
    User register(User user, String rawPassword);

    String login(String email, String rawPassword);

    void logout(String token);

    boolean validateToken(String token);

    String refreshToken(String token);

    Optional<User> getUserById(Long userId);

    Optional<User> getUserByEmail(String email);

    Long getUserIdFromToken(String token);

    String getEmailFromToken(String token);

    User updateProfile(Long userId, User profile);

    void changePassword(Long userId, String currentPassword, String newPassword);

    User updateCurrency(Long userId, String currency);

    User updateMonthlyBudget(Long userId, BigDecimal monthlyBudget);

    void deactivateAccount(Long userId);
}
