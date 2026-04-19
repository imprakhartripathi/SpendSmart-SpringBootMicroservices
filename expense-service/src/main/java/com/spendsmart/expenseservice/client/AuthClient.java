package com.spendsmart.expenseservice.client;

import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AuthClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthClient.class);

    private final RestTemplate restTemplate;

    @Value("${app.services.auth-url:http://auth-service}")
    private String authServiceUrl;

    public AuthClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Optional<UserSummary> getUserSummary(Long userId) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(
                    authServiceUrl + "/auth/profile/{userId}",
                    Map.class,
                    Map.of("userId", userId)
            );

            if (response == null) {
                return Optional.empty();
            }

            Object fullName = response.get("fullName");
            Object email = response.get("email");

            if (email == null || email.toString().isBlank()) {
                return Optional.empty();
            }

            return Optional.of(new UserSummary(
                    userId,
                    fullName == null ? "User" : fullName.toString(),
                    email.toString()
            ));
        } catch (Exception exception) {
            LOGGER.warn("Could not fetch auth profile for userId={}. Reason={}", userId, exception.getMessage());
            return Optional.empty();
        }
    }

    public record UserSummary(Long userId, String fullName, String email) {
    }
}
