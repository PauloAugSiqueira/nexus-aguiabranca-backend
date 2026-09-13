package br.com.fiap.gabinova.backend.security;

import br.com.fiap.gabinova.backend.domain.UserRole;

/** Principal colocado no SecurityContext apos validacao do JWT (ver JwtAuthenticationFilter). */
public record AuthenticatedUser(String userId, String email, String name, UserRole role) {
}
