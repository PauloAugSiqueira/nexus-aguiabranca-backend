package br.com.fiap.gabinova.backend.dto.response;

import java.util.List;

public record GamificationDto(
        String userId,
        String userName,
        int points,
        int level,
        String levelName,
        float progress,
        List<BadgeDto> badges,
        List<ScoreHistoryDto> history
) {
}
