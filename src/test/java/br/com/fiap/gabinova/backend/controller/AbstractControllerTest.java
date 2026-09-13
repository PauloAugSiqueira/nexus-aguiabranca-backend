package br.com.fiap.gabinova.backend.controller;

import br.com.fiap.gabinova.backend.domain.User;
import br.com.fiap.gabinova.backend.domain.UserRole;
import br.com.fiap.gabinova.backend.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Base para testes de controller com seguranca JWT real de ponta a ponta:
 * carrega o contexto Spring completo (sem MongoDB real - nenhum repository e efetivamente
 * acionado, pois cada teste faz @MockBean do Service usado pelo controller sob teste),
 * gera tokens JWT validos via o JwtTokenProvider real e exercita o SecurityFilterChain de verdade.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class AbstractControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected JwtTokenProvider jwtTokenProvider;

    protected String tokenFor(UserRole role) {
        User user = new User("Usuario Teste", role.name().toLowerCase() + "@gab.com", "hash", role, "Operações");
        user.setId(role.name().toLowerCase() + "-id");
        return jwtTokenProvider.generateToken(user);
    }

    protected String bearer(UserRole role) {
        return "Bearer " + tokenFor(role);
    }
}
