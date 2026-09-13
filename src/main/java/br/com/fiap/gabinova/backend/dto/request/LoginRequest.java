package br.com.fiap.gabinova.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "E-mail e obrigatorio.") String email,
        @NotBlank(message = "Senha e obrigatoria.") String password
) {
}
