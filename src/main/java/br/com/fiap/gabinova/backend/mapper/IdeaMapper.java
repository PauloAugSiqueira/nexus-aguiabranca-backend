package br.com.fiap.gabinova.backend.mapper;

import br.com.fiap.gabinova.backend.domain.Idea;
import br.com.fiap.gabinova.backend.dto.response.IdeaDto;

public final class IdeaMapper {

    private IdeaMapper() {
    }

    public static IdeaDto toDto(Idea idea) {
        return new IdeaDto(
                idea.getId(),
                idea.getTitle(),
                idea.getDescription(),
                idea.getSector(),
                idea.getCategory(),
                idea.getExpectedImpact(),
                idea.getUrgency(),
                idea.getStatus().name(),
                idea.getAuthorId(),
                idea.getAuthorName(),
                idea.getGuidelineId(),
                idea.getScore(),
                DateFormat.iso(idea.getCreatedAt()),
                DateFormat.iso(idea.getUpdatedAt())
        );
    }
}
