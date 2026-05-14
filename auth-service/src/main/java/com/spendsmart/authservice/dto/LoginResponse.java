package com.spendsmart.authservice.dto;

public record LoginResponse(String token, Long userId, String email) {
}
