package br.com.fiap.gabinova.backend.dto.response;

public record RankingDto(
        int position,
        String userId,
        String userName,
        int points,
        String department
) {
}
