package br.com.fiap.gabinova.backend.controller;

import br.com.fiap.gabinova.backend.domain.UserRole;
import br.com.fiap.gabinova.backend.dto.request.CreateIdeaRequest;
import br.com.fiap.gabinova.backend.dto.request.UpdateIdeaStatusRequest;
import br.com.fiap.gabinova.backend.dto.response.IdeaDto;
import br.com.fiap.gabinova.backend.service.IdeaService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class IdeaControllerTest extends AbstractControllerTest {

    @MockBean
    private IdeaService ideaService;

    private IdeaDto sampleDto() {
        return new IdeaDto("i1", "Titulo", "desc", "Operações", "Segurança", "impacto",
                3, "PENDING", "collab-id", "Operador", null, 42, "2026-01-01T00:00:00Z", "2026-01-01T00:00:00Z");
    }

    @Test
    void criarComoOperadorRetorna201() throws Exception {
        when(ideaService.create(any(), any())).thenReturn(sampleDto());

        CreateIdeaRequest request = new CreateIdeaRequest("Titulo", "desc", "Operações", "Segurança", "impacto", 3, null);

        mockMvc.perform(post("/api/ideas")
                        .header("Authorization", bearer(UserRole.COLLABORATOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void criarComoGestorRetorna403() throws Exception {
        CreateIdeaRequest request = new CreateIdeaRequest("Titulo", "desc", "Operações", "Segurança", "impacto", 3, null);

        mockMvc.perform(post("/api/ideas")
                        .header("Authorization", bearer(UserRole.MANAGER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void criarComUrgenciaForaDoIntervaloRetorna400() throws Exception {
        CreateIdeaRequest request = new CreateIdeaRequest("Titulo", "desc", "Operações", "Segurança", "impacto", 9, null);

        mockMvc.perform(post("/api/ideas")
                        .header("Authorization", bearer(UserRole.COLLABORATOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listarComoLiderancaRetorna403() throws Exception {
        mockMvc.perform(get("/api/ideas").header("Authorization", bearer(UserRole.ADMIN)))
                .andExpect(status().isForbidden());
    }

    @Test
    void aprovarComoOperadorRetorna403() throws Exception {
        UpdateIdeaStatusRequest request = new UpdateIdeaStatusRequest("APPROVED");

        mockMvc.perform(patch("/api/ideas/i1/status")
                        .header("Authorization", bearer(UserRole.COLLABORATOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void aprovarComoGestorRetorna200() throws Exception {
        when(ideaService.updateStatus(any(), any(), any())).thenReturn(sampleDto());
        UpdateIdeaStatusRequest request = new UpdateIdeaStatusRequest("APPROVED");

        mockMvc.perform(patch("/api/ideas/i1/status")
                        .header("Authorization", bearer(UserRole.MANAGER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
