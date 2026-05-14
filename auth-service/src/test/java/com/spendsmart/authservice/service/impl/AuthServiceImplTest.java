package com.spendsmart.authservice.service.impl;

import com.spendsmart.authservice.domain.User;
import com.spendsmart.authservice.messaging.NotificationEventPublisher;
import com.spendsmart.authservice.repository.UserRepository;
import com.spendsmart.authservice.security.JwtTokenService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private NotificationEventPublisher notificationEventPublisher;

    @Mock
    private ObjectProvider<NotificationEventPublisher> notificationEventPublisherProvider;

    private AuthServiceImpl authService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        when(notificationEventPublisherProvider.getIfAvailable()).thenReturn(notificationEventPublisher);
        authService = new AuthServiceImpl(userRepository, jwtTokenService, notificationEventPublisherProvider);
    }

    @Test
    void registerHashesPasswordAndPublishesWelcome() {
        User request = new User();
        request.setEmail("prakhar@example.com");
        request.setFullName("Prakhar Tripathi");
        request.setCurrency("INR");
        request.setTimezone("Asia/Kolkata");
        request.setMonthlyBudget(BigDecimal.valueOf(3000));

        when(userRepository.existsByEmail("prakhar@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setUserId(12L);
            saved.setCreatedAt(Instant.parse("2026-04-18T00:00:00Z"));
            return saved;
        });

        User saved = authService.register(request, "secret");

        assertThat(saved.getPasswordHash()).isNotBlank();
        assertThat(new BCryptPasswordEncoder().matches("secret", saved.getPasswordHash())).isTrue();
        verify(notificationEventPublisher).publishWelcome(saved);
    }
}
