package br.com.fiap.gabinova.backend.dto.response;

public record UserDto(
        String id,
        String name,
        String email,
        String role,
        String department,
        int points,
        int level,
        String createdAt
) {
}
