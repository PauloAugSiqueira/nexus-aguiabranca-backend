package br.com.fiap.gabinova.backend.dto.response;

public record ProjectDto(
        String id,
        String title,
        String ideaOrigin,
        String ideaId,
        String guidelineId,
        String responsible,
        String status,
        String stage,
        String investment,
        String expectedReturn,
        String actualReturn,
        String productivity,
        int progress,
        String deadline,
        String createdAt,
        String updatedAt
) {
}
