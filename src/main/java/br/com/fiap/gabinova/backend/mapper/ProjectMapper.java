package br.com.fiap.gabinova.backend.mapper;

import br.com.fiap.gabinova.backend.domain.Project;
import br.com.fiap.gabinova.backend.dto.response.ProjectDto;

public final class ProjectMapper {

    private ProjectMapper() {
    }

    public static ProjectDto toDto(Project p) {
        return new ProjectDto(
                p.getId(),
                p.getTitle(),
                p.getIdeaOrigin(),
                p.getIdeaId(),
                p.getGuidelineId(),
                p.getResponsible(),
                p.getStatus().name(),
                p.getStage(),
                p.getInvestment(),
                p.getExpectedReturn(),
                p.getActualReturn(),
                p.getProductivity(),
                p.getProgress(),
                p.getDeadline(),
                DateFormat.iso(p.getCreatedAt()),
                DateFormat.iso(p.getUpdatedAt())
        );
    }
}
