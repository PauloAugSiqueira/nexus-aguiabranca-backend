package br.com.fiap.gabinova.backend.dto.response;

public record GuidelineDto(
        String id,
        String title,
        String description,
        String category,
        String priority,
        String status,
        String createdAt,
        String updatedAt
) {
}
