package com.spendsmart.authservice.service.impl;

import com.spendsmart.authservice.domain.User;
import com.spendsmart.authservice.dto.LoginResponse;
import com.spendsmart.authservice.dto.OAuthAuthorizeResponse;
import com.spendsmart.authservice.enums.AuthProvider;
import com.spendsmart.authservice.messaging.NotificationEventPublisher;
import com.spendsmart.authservice.repository.UserRepository;
import com.spendsmart.authservice.security.JwtTokenService;
import com.spendsmart.authservice.service.OAuthService;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@Transactional
public class OAuthServiceImpl implements OAuthService {

    private final UserRepository userRepository;
    private final JwtTokenService jwtTokenService;
    private final NotificationEventPublisher notificationEventPublisher;
    private final RestTemplate restTemplate = new RestTemplate();
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${oauth2.google.client-id:}")
    private String googleClientId;
    @Value("${oauth2.google.client-secret:}")
    private String googleClientSecret;
    @Value("${oauth2.google.redirect-uri:http://localhost:5173/oauth/callback/google}")
    private String googleRedirectUri;
    @Value("${oauth2.google.scopes:openid profile email}")
    private String googleScopes;

    @Value("${oauth2.github.client-id:}")
    private String githubClientId;
    @Value("${oauth2.github.client-secret:}")
    private String githubClientSecret;
    @Value("${oauth2.github.redirect-uri:http://localhost:5173/oauth/callback/github}")
    private String githubRedirectUri;
    @Value("${oauth2.github.scopes:read:user user:email}")
    private String githubScopes;

    public OAuthServiceImpl(
            UserRepository userRepository,
            JwtTokenService jwtTokenService,
            ObjectProvider<NotificationEventPublisher> notificationEventPublisherProvider
    ) {
        this.userRepository = userRepository;
        this.jwtTokenService = jwtTokenService;
        this.notificationEventPublisher = notificationEventPublisherProvider == null
                ? null
                : notificationEventPublisherProvider.getIfAvailable();
    }

    @Override
    public OAuthAuthorizeResponse getAuthorizationUrl(String provider) {
        AuthProvider authProvider = parseProvider(provider);
        return switch (authProvider) {
            case GOOGLE -> new OAuthAuthorizeResponse(buildGoogleAuthorizationUrl());
            case GITHUB -> new OAuthAuthorizeResponse(buildGithubAuthorizationUrl());
            default -> throw new IllegalArgumentException("OAuth provider is not supported: " + provider);
        };
    }

    @Override
    public LoginResponse loginWithAuthorizationCode(String provider, String code) {
        AuthProvider authProvider = parseProvider(provider);
        OAuthProfile profile = switch (authProvider) {
            case GOOGLE -> fetchGoogleProfile(code);
            case GITHUB -> fetchGithubProfile(code);
            default -> throw new IllegalArgumentException("OAuth provider is not supported: " + provider);
        };

        User user = upsertOAuthUser(authProvider, profile);
        String token = jwtTokenService.generateToken(user.getUserId(), user.getEmail());
        if (notificationEventPublisher != null) {
            notificationEventPublisher.publishWelcome(user);
        }
        return new LoginResponse(token, user.getUserId(), user.getEmail());
    }

    private OAuthProfile fetchGoogleProfile(String code) {
        ensureConfigured(googleClientId, "GOOGLE client id");
        ensureConfigured(googleClientSecret, "GOOGLE client secret");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("client_id", googleClientId);
        body.add("client_secret", googleClientSecret);
        body.add("redirect_uri", googleRedirectUri);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        ResponseEntity<Map> tokenResponse = restTemplate.exchange(
                "https://oauth2.googleapis.com/token",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                Map.class
        );

        String accessToken = extractRequiredString(tokenResponse.getBody(), "access_token");

        HttpHeaders userInfoHeaders = new HttpHeaders();
        userInfoHeaders.setBearerAuth(accessToken);

        ResponseEntity<Map> userInfoResponse = restTemplate.exchange(
                "https://openidconnect.googleapis.com/v1/userinfo",
                HttpMethod.GET,
                new HttpEntity<>(userInfoHeaders),
                Map.class
        );

        Map<?, ?> profile = userInfoResponse.getBody();
        if (profile == null) {
            throw new IllegalArgumentException("GOOGLE user profile is empty");
        }

        String email = asString(profile.get("email"));
        String fullName = fallback(asString(profile.get("name")), email);
        String avatarUrl = asString(profile.get("picture"));

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("GOOGLE account did not return an email");
        }

        return new OAuthProfile(fullName, email, avatarUrl);
    }

    private OAuthProfile fetchGithubProfile(String code) {
        ensureConfigured(githubClientId, "GITHUB client id");
        ensureConfigured(githubClientSecret, "GITHUB client secret");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", githubClientId);
        body.add("client_secret", githubClientSecret);
        body.add("code", code);
        body.add("redirect_uri", githubRedirectUri);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        ResponseEntity<Map> tokenResponse = restTemplate.exchange(
                "https://github.com/login/oauth/access_token",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                Map.class
        );

        String accessToken = extractRequiredString(tokenResponse.getBody(), "access_token");

        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.setBearerAuth(accessToken);
        userHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        userHeaders.add("X-GitHub-Api-Version", "2022-11-28");

        ResponseEntity<Map> userResponse = restTemplate.exchange(
                "https://api.github.com/user",
                HttpMethod.GET,
                new HttpEntity<>(userHeaders),
                Map.class
        );

        Map<?, ?> profile = userResponse.getBody();
        if (profile == null) {
            throw new IllegalArgumentException("GITHUB user profile is empty");
        }

        String email = asString(profile.get("email"));
        if (email == null || email.isBlank()) {
            ResponseEntity<List> emailResponse = restTemplate.exchange(
                    "https://api.github.com/user/emails",
                    HttpMethod.GET,
                    new HttpEntity<>(userHeaders),
                    List.class
            );
            email = resolveGithubEmail(emailResponse.getBody());
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("GITHUB account did not return an email");
        }

        String fullName = fallback(asString(profile.get("name")), fallback(asString(profile.get("login")), email));
        String avatarUrl = asString(profile.get("avatar_url"));
        return new OAuthProfile(fullName, email, avatarUrl);
    }

    private String resolveGithubEmail(List<?> rawEmails) {
        if (rawEmails == null) {
            return null;
        }
        String fallbackVerifiedEmail = null;

        for (Object rawItem : rawEmails) {
            if (!(rawItem instanceof Map<?, ?> item)) {
                continue;
            }

            if (!Boolean.TRUE.equals(item.get("verified"))) {
                continue;
            }

            String email = asString(item.get("email"));
            if (email == null || email.isBlank()) {
                continue;
            }

            if (Boolean.TRUE.equals(item.get("primary"))) {
                return email;
            }

            if (fallbackVerifiedEmail == null) {
                fallbackVerifiedEmail = email;
            }
        }

        return fallbackVerifiedEmail;
    }

    private User upsertOAuthUser(AuthProvider provider, OAuthProfile profile) {
        User existing = userRepository.findByEmail(profile.email()).orElse(null);
        if (existing != null) {
            existing.setFullName(profile.fullName());
            existing.setAvatarUrl(profile.avatarUrl());
            if (existing.getProvider() == AuthProvider.LOCAL) {
                existing.setProvider(provider);
            }
            existing.setActive(true);
            return userRepository.save(existing);
        }

        User user = new User();
        user.setFullName(profile.fullName());
        user.setEmail(profile.email());
        user.setAvatarUrl(profile.avatarUrl());
        user.setProvider(provider);
        user.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        User saved = userRepository.save(user);
        notificationEventPublisher.publishWelcome(saved);
        return saved;
    }

    private AuthProvider parseProvider(String provider) {
        if (provider == null || provider.isBlank()) {
            throw new IllegalArgumentException("OAuth provider is required");
        }
        try {
            return AuthProvider.valueOf(provider.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("OAuth provider is not supported: " + provider);
        }
    }

    private String buildGoogleAuthorizationUrl() {
        ensureConfigured(googleClientId, "GOOGLE client id");

        return "https://accounts.google.com/o/oauth2/v2/auth"
                + "?response_type=code"
                + "&client_id=" + encode(googleClientId)
                + "&redirect_uri=" + encode(googleRedirectUri)
                + "&scope=" + encode(googleScopes)
                + "&access_type=offline"
                + "&prompt=consent";
    }

    private String buildGithubAuthorizationUrl() {
        ensureConfigured(githubClientId, "GITHUB client id");

        return "https://github.com/login/oauth/authorize"
                + "?client_id=" + encode(githubClientId)
                + "&redirect_uri=" + encode(githubRedirectUri)
                + "&scope=" + encode(githubScopes);
    }

    private static String extractRequiredString(Map<?, ?> body, String key) {
        if (body == null) {
            throw new IllegalArgumentException("OAuth token response is empty");
        }
        String value = asString(body.get(key));
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("OAuth token response missing field: " + key);
        }
        return value;
    }

    private static String asString(Object value) {
        return value == null ? null : value.toString();
    }

    private static String fallback(String preferred, String fallback) {
        return (preferred == null || preferred.isBlank()) ? fallback : preferred;
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static void ensureConfigured(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is not configured");
        }
    }

    private record OAuthProfile(String fullName, String email, String avatarUrl) {
    }
}
