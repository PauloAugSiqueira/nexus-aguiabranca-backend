package br.com.fiap.gabinova.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

/** status esperado: "APPROVED" ou "REJECTED" (ver SPEC_FUNCIONAL_BACKEND.md, 6.2.2). */
public record UpdateIdeaStatusRequest(
        @NotBlank(message = "Status e obrigatorio.") String status
) {
}
