package br.com.fiap.gabinova.backend.service;

import br.com.fiap.gabinova.backend.domain.*;
import br.com.fiap.gabinova.backend.dto.response.DashboardDto;
import br.com.fiap.gabinova.backend.repository.IdeaRepository;
import br.com.fiap.gabinova.backend.repository.ProjectRepository;
import br.com.fiap.gabinova.backend.repository.StrategicGuidelineRepository;
import br.com.fiap.gabinova.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private IdeaRepository ideaRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private StrategicGuidelineRepository guidelineRepository;

    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService(ideaRepository, projectRepository, userRepository, guidelineRepository);
    }

    private Idea idea(String authorId, IdeaStatus status, String guidelineId) {
        Idea i = new Idea();
        i.setAuthorId(authorId);
        i.setStatus(status);
        i.setGuidelineId(guidelineId);
        i.setCreatedAt(Instant.now());
        return i;
    }

    private Project project(ProjectStatus status, String investment, String actualReturn, String productivity) {
        Project p = new Project();
        p.setStatus(status);
        p.setInvestment(investment);
        p.setActualReturn(actualReturn);
        p.setProductivity(productivity);
        return p;
    }

    private User collaborator(String id) {
        User u = new User("Colab " + id, id + "@gab.com", "hash", UserRole.COLLABORATOR, "Operações");
        u.setId(id);
        return u;
    }

    @Test
    void calculaContagensBasicasDeIdeiasEProjetos() {
        when(ideaRepository.findAll()).thenReturn(List.of(
                idea("u1", IdeaStatus.APPROVED, null),
                idea("u1", IdeaStatus.PENDING, null),
                idea("u2", IdeaStatus.PENDING, null)
        ));
        when(projectRepository.findAll()).thenReturn(List.of(
                project(ProjectStatus.IN_PROGRESS, "R$ 100.000", "", ""),
                project(ProjectStatus.COMPLETED, "R$ 50.000", "R$ 120.000", "80%")
        ));
        when(userRepository.findByRole(UserRole.COLLABORATOR)).thenReturn(List.of(collaborator("u1"), collaborator("u2")));
        lenient().when(ideaRepository.findByAuthorId("u1")).thenReturn(List.of(idea("u1", IdeaStatus.APPROVED, null)));
        lenient().when(ideaRepository.findByAuthorId("u2")).thenReturn(List.of(idea("u2", IdeaStatus.PENDING, null)));
        when(guidelineRepository.findByStatus(GuidelineStatus.ACTIVE)).thenReturn(List.of());

        DashboardDto dto = dashboardService.getDashboard();

        assertThat(dto.totalIdeas()).isEqualTo(3);
        assertThat(dto.approvedIdeas()).isEqualTo(1);
        assertThat(dto.pendingIdeas()).isEqualTo(2);
        assertThat(dto.activeProjects()).isEqualTo(1);
        assertThat(dto.completedProjects()).isEqualTo(1);
        assertThat(dto.totalProjects()).isEqualTo(2);
        assertThat(dto.engagedCollaborators()).isEqualTo(2);
        assertThat(dto.totalCollaborators()).isEqualTo(2);
        assertThat(dto.engagementRate()).isEqualTo(100.0);
    }

    @Test
    void calculaRoiApenasSobreProjetosConcluidos() {
        when(ideaRepository.findAll()).thenReturn(List.of());
        when(projectRepository.findAll()).thenReturn(List.of(
                project(ProjectStatus.COMPLETED, "R$ 100.000", "R$ 200.000", ""),
                project(ProjectStatus.IN_PROGRESS, "R$ 999.000", "", "")
        ));
        when(userRepository.findByRole(UserRole.COLLABORATOR)).thenReturn(List.of());
        when(guidelineRepository.findByStatus(GuidelineStatus.ACTIVE)).thenReturn(List.of());

        DashboardDto dto = dashboardService.getDashboard();

        // roi = (200.000 - 100.000) / 100.000 * 100 = 100.0 (ignora o projeto em andamento)
        assertThat(dto.roi()).isEqualTo(100.0);
    }

    @Test
    void dashboardVazioNaoLancaExcecaoEZeraIndicadores() {
        when(ideaRepository.findAll()).thenReturn(List.of());
        when(projectRepository.findAll()).thenReturn(List.of());
        when(userRepository.findByRole(UserRole.COLLABORATOR)).thenReturn(List.of());
        when(guidelineRepository.findByStatus(GuidelineStatus.ACTIVE)).thenReturn(List.of());

        DashboardDto dto = dashboardService.getDashboard();

        assertThat(dto.totalIdeas()).isZero();
        assertThat(dto.roi()).isZero();
        assertThat(dto.engagementRate()).isZero();
        assertThat(dto.topGuideline()).isEqualTo("N/A");
    }
}
