package br.com.fiap.gabinova.backend.dto.response;

public record BadgeDto(
        String id,
        String name,
        boolean earned,
        String earnedAt
) {
}
