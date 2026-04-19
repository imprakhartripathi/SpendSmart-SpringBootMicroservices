package com.spendsmart.authservice.service;

import com.spendsmart.authservice.dto.LoginResponse;
import com.spendsmart.authservice.dto.OAuthAuthorizeResponse;

public interface OAuthService {
    OAuthAuthorizeResponse getAuthorizationUrl(String provider);

    LoginResponse loginWithAuthorizationCode(String provider, String code);
}
