package br.com.fiap.gabinova.backend.security;

import br.com.fiap.gabinova.backend.domain.User;
import br.com.fiap.gabinova.backend.domain.UserRole;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider("junit-test-secret-key-needs-32-bytes-min", 8);
    }

    private User buildUser() {
        User user = new User("Gestor Teste", "gestor@gab.com", "hash", UserRole.MANAGER, "Operações");
        user.setId("user-123");
        return user;
    }

    @Test
    void geraTokenValidoComClaimsCorretas() {
        String token = jwtTokenProvider.generateToken(buildUser());

        assertThat(jwtTokenProvider.isValid(token)).isTrue();

        Claims claims = jwtTokenProvider.parseClaims(token);
        assertThat(claims.getSubject()).isEqualTo("user-123");
        assertThat(claims.get("role", String.class)).isEqualTo("MANAGER");
        assertThat(claims.get("email", String.class)).isEqualTo("gestor@gab.com");
        assertThat(claims.get("name", String.class)).isEqualTo("Gestor Teste");
        assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
    }

    @Test
    void tokenAdulteradoENaoValido() {
        String token = jwtTokenProvider.generateToken(buildUser());
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertThat(jwtTokenProvider.isValid(tampered)).isFalse();
    }

    @Test
    void tokenComAssinaturaDeOutroSegredoENaoValido() {
        String token = jwtTokenProvider.generateToken(buildUser());

        JwtTokenProvider outroProvider = new JwtTokenProvider("outro-segredo-completamente-diferente-32b", 8);
        assertThat(outroProvider.isValid(token)).isFalse();
    }

    @Test
    void stringInvalidaNaoQuebraIsValid() {
        assertThat(jwtTokenProvider.isValid("isto-nao-e-um-jwt")).isFalse();
    }
}
