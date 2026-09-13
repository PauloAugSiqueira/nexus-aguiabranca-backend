package br.com.fiap.gabinova.backend.dto.response;

public record ScoreHistoryDto(
        int points,
        String description,
        String date,
        String eventType
) {
}
