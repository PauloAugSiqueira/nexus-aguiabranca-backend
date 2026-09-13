package br.com.fiap.gabinova.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AddPointsRequest(
        @NotBlank(message = "userId e obrigatorio.") String userId,
        int points,
        @NotBlank(message = "eventType e obrigatorio.") String eventType,
        String description
) {
}
