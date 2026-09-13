package br.com.fiap.gabinova.backend.dto.response;

public record IdeaDto(
        String id,
        String title,
        String description,
        String sector,
        String category,
        String expectedImpact,
        int urgency,
        String status,
        String authorId,
        String authorName,
        String guidelineId,
        int score,
        String createdAt,
        String updatedAt
) {
}
