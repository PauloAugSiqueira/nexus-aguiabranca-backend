package br.com.fiap.gabinova.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateProjectRequest(
        @NotBlank(message = "Titulo e obrigatorio.") String title,
        String ideaOrigin,
        String responsible,
        String status,
        String stage,
        String investment,
        String expectedReturn,
        String deadline,
        String ideaId,
        String guidelineId
) {
}
