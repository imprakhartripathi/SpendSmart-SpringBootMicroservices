package com.spendsmart.authservice.web;

import com.spendsmart.authservice.domain.User;
import com.spendsmart.authservice.dto.LoginResponse;
import com.spendsmart.authservice.dto.OAuthAuthorizeResponse;
import com.spendsmart.authservice.enums.AuthProvider;
import com.spendsmart.authservice.service.AuthService;
import com.spendsmart.authservice.service.OAuthService;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthResource.class)
class AuthResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private OAuthService oAuthService;

    @Test
    void registerCreatesProfile() throws Exception {
        User user = new User();
        user.setUserId(42L);
        user.setFullName("Prakhar Tripathi");
        user.setEmail("prakhar@example.com");
        user.setCurrency("INR");
        user.setTimezone("Asia/Kolkata");
        user.setAvatarUrl("https://avatar.example.com/p.png");
        user.setBio("Founder");
        user.setProvider(AuthProvider.LOCAL);
        user.setActive(true);
        user.setCreatedAt(Instant.parse("2026-04-18T00:00:00Z"));
        user.setMonthlyBudget(BigDecimal.valueOf(3000));

        when(authService.register(any(User.class), eq("secret"))).thenReturn(user);

        mockMvc.perform(
                        post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "fullName": "Prakhar Tripathi",
                                          "email": "prakhar@example.com",
                                          "password": "secret",
                                          "currency": "INR",
                                          "timezone": "Asia/Kolkata",
                                          "avatarUrl": "https://avatar.example.com/p.png",
                                          "bio": "Founder",
                                          "provider": "LOCAL",
                                          "monthlyBudget": 3000
                                        }
                                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(42))
                .andExpect(jsonPath("$.email").value("prakhar@example.com"));
    }
}
