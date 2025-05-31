package com.example.ecommercebackend.payload.response;

import java.util.List;

public record JwtResponse(
    String token,
    String type,
    Long id,
    String username,
    String email,
    List<String> roles
) {
    public JwtResponse(String accessToken, Long id, String username, String email, List<String> roles) {
        this(accessToken, "Bearer", id, username, email, roles);
    }
}
