package com.spendsmart.authservice.web;

import com.spendsmart.authservice.domain.User;
import com.spendsmart.authservice.dto.CurrencyUpdateRequest;
import com.spendsmart.authservice.dto.LoginRequest;
import com.spendsmart.authservice.dto.LoginResponse;
import com.spendsmart.authservice.dto.LogoutRequest;
import com.spendsmart.authservice.dto.MonthlyBudgetUpdateRequest;
import com.spendsmart.authservice.dto.PasswordChangeRequest;
import com.spendsmart.authservice.dto.ProfileUpdateRequest;
import com.spendsmart.authservice.dto.RefreshTokenRequest;
import com.spendsmart.authservice.dto.RegisterRequest;
import com.spendsmart.authservice.dto.UserProfileResponse;
import com.spendsmart.authservice.service.AuthService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthResource {

    private final AuthService authService;

    public AuthResource(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserProfileResponse register(@Valid @RequestBody RegisterRequest request) {
        User user = new User();
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setCurrency(request.currency());
        user.setTimezone(request.timezone());
        user.setAvatarUrl(request.avatarUrl());
        user.setBio(request.bio());
        user.setProvider(request.provider());
        user.setMonthlyBudget(request.monthlyBudget());
        return toResponse(authService.register(user, request.password()));
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request.email(), request.password());
        User user = authService.getUserByEmail(request.email())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return new LoginResponse(token, user.getUserId(), user.getEmail());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request.token());
    }

    @PostMapping("/refresh")
    public LoginResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        String refreshedToken = authService.refreshToken(request.token());
        return new LoginResponse(
                refreshedToken,
                authService.getUserIdFromToken(refreshedToken),
                authService.getEmailFromToken(refreshedToken)
        );
    }

    @GetMapping("/profile/{userId}")
    public UserProfileResponse getProfile(@PathVariable Long userId) {
        return authService.getUserById(userId)
                .map(this::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    @GetMapping("/profile-by-email")
    public UserProfileResponse getProfileByEmail(@RequestParam String email) {
        return authService.getUserByEmail(email)
                .map(this::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    @PutMapping("/profile/{userId}")
    public UserProfileResponse updateProfile(@PathVariable Long userId, @Valid @RequestBody ProfileUpdateRequest request) {
        User profile = new User();
        profile.setFullName(request.fullName());
        profile.setEmail(request.email());
        profile.setAvatarUrl(request.avatarUrl());
        profile.setBio(request.bio());
        profile.setTimezone(request.timezone());
        return toResponse(authService.updateProfile(userId, profile));
    }

    @PutMapping("/password/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@PathVariable Long userId, @Valid @RequestBody PasswordChangeRequest request) {
        authService.changePassword(userId, request.currentPassword(), request.newPassword());
    }

    @PutMapping("/currency/{userId}")
    public UserProfileResponse updateCurrency(@PathVariable Long userId, @Valid @RequestBody CurrencyUpdateRequest request) {
        return toResponse(authService.updateCurrency(userId, request.currency()));
    }

    @PutMapping("/monthly-budget/{userId}")
    public UserProfileResponse updateMonthlyBudget(@PathVariable Long userId, @Valid @RequestBody MonthlyBudgetUpdateRequest request) {
        return toResponse(authService.updateMonthlyBudget(userId, request.monthlyBudget()));
    }

    @DeleteMapping("/deactivate/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable Long userId) {
        authService.deactivateAccount(userId);
    }

    @GetMapping("/validate/{token}")
    public Map<String, Boolean> validate(@PathVariable String token) {
        return Map.of("valid", authService.validateToken(token));
    }

    private UserProfileResponse toResponse(User user) {
        return new UserProfileResponse(
                user.getUserId(),
                user.getFullName(),
                user.getEmail(),
                user.getCurrency(),
                user.getTimezone(),
                user.getAvatarUrl(),
                user.getBio(),
                user.getProvider(),
                user.isActive(),
                user.getCreatedAt(),
                user.getMonthlyBudget()
        );
    }
}
