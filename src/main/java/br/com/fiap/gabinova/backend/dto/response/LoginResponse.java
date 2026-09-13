package br.com.fiap.gabinova.backend.dto.response;

public record LoginResponse(
        String token,
        String userId,
        String userName,
        String userRole,
        String email,
        String expiresAt
) {
}
