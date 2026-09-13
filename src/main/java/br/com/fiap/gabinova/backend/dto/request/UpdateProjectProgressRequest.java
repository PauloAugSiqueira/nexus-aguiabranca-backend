package br.com.fiap.gabinova.backend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record UpdateProjectProgressRequest(
        @Min(value = 0, message = "Progresso minimo e 0.") @Max(value = 100, message = "Progresso maximo e 100.") int progress
) {
}
