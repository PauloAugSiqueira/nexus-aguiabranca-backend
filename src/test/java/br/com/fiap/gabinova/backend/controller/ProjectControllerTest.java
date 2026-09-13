package br.com.fiap.gabinova.backend.controller;

import br.com.fiap.gabinova.backend.domain.UserRole;
import br.com.fiap.gabinova.backend.dto.response.ProjectDto;
import br.com.fiap.gabinova.backend.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProjectControllerTest extends AbstractControllerTest {

    @MockBean
    private ProjectService projectService;

    private ProjectDto sampleDto() {
        return new ProjectDto("p1", "Titulo", "origem", null, null, "resp", "IN_PROGRESS", "Implantação",
                "R$ 100.000", "R$ 200.000", "", "", 40, "31/12/2026", "2026-01-01T00:00:00Z", "2026-01-01T00:00:00Z");
    }

    @Test
    void aprovarComoGestorRetorna403() throws Exception {
        mockMvc.perform(patch("/api/projects/p1/approve").header("Authorization", bearer(UserRole.MANAGER)))
                .andExpect(status().isForbidden());
    }

    @Test
    void aprovarComoLiderancaRetorna200() throws Exception {
        when(projectService.approve(any(), any())).thenReturn(sampleDto());

        mockMvc.perform(patch("/api/projects/p1/approve").header("Authorization", bearer(UserRole.ADMIN)))
                .andExpect(status().isOk());
    }

    @Test
    void listarComoOperadorRetorna403() throws Exception {
        mockMvc.perform(get("/api/projects").header("Authorization", bearer(UserRole.COLLABORATOR)))
                .andExpect(status().isForbidden());
    }

    @Test
    void listarComoLiderancaRetorna200() throws Exception {
        when(projectService.list()).thenReturn(java.util.List.of(sampleDto()));

        mockMvc.perform(get("/api/projects").header("Authorization", bearer(UserRole.ADMIN)))
                .andExpect(status().isOk());
    }
}
