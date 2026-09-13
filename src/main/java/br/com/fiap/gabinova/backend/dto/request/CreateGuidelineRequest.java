package br.com.fiap.gabinova.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateGuidelineRequest(
        @NotBlank(message = "O titulo e obrigatorio.") String title,
        String description,
        @NotBlank(message = "Selecione uma categoria.") String category,
        @NotBlank(message = "Selecione uma prioridade.") String priority
) {
}
