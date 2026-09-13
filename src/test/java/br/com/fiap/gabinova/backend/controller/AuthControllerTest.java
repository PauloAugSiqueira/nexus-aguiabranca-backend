package br.com.fiap.gabinova.backend.controller;

import br.com.fiap.gabinova.backend.dto.request.LoginRequest;
import br.com.fiap.gabinova.backend.dto.response.LoginResponse;
import br.com.fiap.gabinova.backend.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest extends AbstractControllerTest {

    @MockBean
    private AuthService authService;

    @Test
    void loginComCredenciaisValidasRetornaToken() throws Exception {
        when(authService.login(any())).thenReturn(new LoginResponse(
                "jwt-fake", "u1", "Operador", "COLLABORATOR", "operador@gab.com", "2026-01-01T08:00:00Z"));

        LoginRequest request = new LoginRequest("operador@gab.com", "123456");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-fake"))
                .andExpect(jsonPath("$.userRole").value("COLLABORATOR"));
    }

    @Test
    void loginComCredenciaisInvalidasRetorna401ComMensagemPadrao() throws Exception {
        when(authService.login(any())).thenThrow(new BadCredentialsException("E-mail ou senha invalidos."));

        LoginRequest request = new LoginRequest("errado@gab.com", "senha-errada");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("E-mail ou senha invalidos."));
    }

    @Test
    void loginSemSenhaRetorna400() throws Exception {
        LoginRequest request = new LoginRequest("operador@gab.com", "");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginNaoExigeTokenDeAutenticacao() throws Exception {
        when(authService.login(any())).thenReturn(new LoginResponse(
                "jwt-fake", "u1", "Gestor", "MANAGER", "gestor@gab.com", "2026-01-01T08:00:00Z"));

        LoginRequest request = new LoginRequest("gestor@gab.com", "123456");

        // nenhum header Authorization enviado - deve funcionar normalmente (rota publica)
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
