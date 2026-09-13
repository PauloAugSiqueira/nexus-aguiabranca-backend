package br.com.fiap.gabinova.backend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateIdeaRequest(
        @NotBlank(message = "Titulo e obrigatorio.") String title,
        String description,
        @NotBlank(message = "Selecione um setor.") String sector,
        @NotBlank(message = "Selecione uma categoria.") String category,
        String expectedImpact,
        @Min(value = 1, message = "Urgencia minima e 1.") @Max(value = 5, message = "Urgencia maxima e 5.") int urgency,
        String guidelineId
) {
}
