package br.com.fiap.gabinova.backend.controller;

import br.com.fiap.gabinova.backend.domain.UserRole;
import br.com.fiap.gabinova.backend.dto.request.CreateGuidelineRequest;
import br.com.fiap.gabinova.backend.dto.response.GuidelineDto;
import br.com.fiap.gabinova.backend.service.GuidelineService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GuidelineControllerTest extends AbstractControllerTest {

    @MockBean
    private GuidelineService guidelineService;

    @Test
    void listarSemTokenRetorna401() throws Exception {
        mockMvc.perform(get("/api/guidelines"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listarComQualquerPerfilAutenticadoRetorna200() throws Exception {
        when(guidelineService.listActive()).thenReturn(List.of(
                new GuidelineDto("1", "Eficiencia", "desc", "Operações", "1", "ACTIVE", "2026-01-01T00:00:00Z", "2026-01-01T00:00:00Z")
        ));

        mockMvc.perform(get("/api/guidelines").header("Authorization", bearer(UserRole.COLLABORATOR)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Eficiencia"));
    }

    @Test
    void criarComoOperadorRetorna403() throws Exception {
        CreateGuidelineRequest request = new CreateGuidelineRequest("Titulo", "desc", "Tecnologia", "2");

        mockMvc.perform(post("/api/guidelines")
                        .header("Authorization", bearer(UserRole.COLLABORATOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void criarComoGestorRetorna403() throws Exception {
        CreateGuidelineRequest request = new CreateGuidelineRequest("Titulo", "desc", "Tecnologia", "2");

        mockMvc.perform(post("/api/guidelines")
                        .header("Authorization", bearer(UserRole.MANAGER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void criarComoLiderancaRetorna201() throws Exception {
        CreateGuidelineRequest request = new CreateGuidelineRequest("Titulo", "desc", "Tecnologia", "2");

        when(guidelineService.create(any())).thenReturn(
                new GuidelineDto("g1", "Titulo", "desc", "Tecnologia", "2", "ACTIVE", "2026-01-01T00:00:00Z", "2026-01-01T00:00:00Z"));

        mockMvc.perform(post("/api/guidelines")
                        .header("Authorization", bearer(UserRole.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("g1"));
    }

    @Test
    void criarSemTituloRetorna400() throws Exception {
        CreateGuidelineRequest request = new CreateGuidelineRequest("", "desc", "Tecnologia", "2");

        mockMvc.perform(post("/api/guidelines")
                        .header("Authorization", bearer(UserRole.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void corpoJsonMalformadoRetorna400EmVezDe500() throws Exception {
        mockMvc.perform(post("/api/guidelines")
                        .header("Authorization", bearer(UserRole.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ isto nao e json valido"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void excluirComoLiderancaRetorna204() throws Exception {
        mockMvc.perform(delete("/api/guidelines/g1")
                        .header("Authorization", bearer(UserRole.ADMIN)))
                .andExpect(status().isNoContent());

        verify(guidelineService).delete(eq("g1"));
    }
}
