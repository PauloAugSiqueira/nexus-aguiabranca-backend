package br.com.fiap.gabinova.backend.mapper;

import br.com.fiap.gabinova.backend.domain.StrategicGuideline;
import br.com.fiap.gabinova.backend.dto.response.GuidelineDto;

public final class GuidelineMapper {

    private GuidelineMapper() {
    }

    public static GuidelineDto toDto(StrategicGuideline g) {
        return new GuidelineDto(
                g.getId(),
                g.getTitle(),
                g.getDescription(),
                g.getCategory(),
                g.getPriority(),
                g.getStatus().name(),
                DateFormat.iso(g.getCreatedAt()),
                DateFormat.iso(g.getUpdatedAt())
        );
    }
}
