package br.com.fiap.gabinova.backend.mapper;

import br.com.fiap.gabinova.backend.domain.Badge;
import br.com.fiap.gabinova.backend.domain.ScoreHistoryEntry;
import br.com.fiap.gabinova.backend.dto.response.BadgeDto;
import br.com.fiap.gabinova.backend.dto.response.ScoreHistoryDto;

public final class GamificationMapper {

    private GamificationMapper() {
    }

    public static BadgeDto toDto(Badge badge) {
        return new BadgeDto(badge.getId(), badge.getName(), badge.isEarned(), badge.getEarnedAt());
    }

    public static ScoreHistoryDto toDto(ScoreHistoryEntry entry) {
        return new ScoreHistoryDto(
                entry.getPoints(),
                entry.getDescription(),
                DateFormat.iso(entry.getDate()),
                entry.getEventType().name()
        );
    }
}
