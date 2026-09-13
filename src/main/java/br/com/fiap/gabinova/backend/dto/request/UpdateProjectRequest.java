package br.com.fiap.gabinova.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateProjectRequest(
        @NotBlank(message = "Titulo e obrigatorio.") String title,
        String ideaOrigin,
        String responsible,
        String status,
        String stage,
        String investment,
        String expectedReturn,
        String actualReturn,
        String productivity,
        String deadline
) {
}
