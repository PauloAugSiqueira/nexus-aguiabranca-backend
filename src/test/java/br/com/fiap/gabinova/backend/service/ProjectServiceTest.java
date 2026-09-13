package br.com.fiap.gabinova.backend.service;

import br.com.fiap.gabinova.backend.domain.Idea;
import br.com.fiap.gabinova.backend.domain.Project;
import br.com.fiap.gabinova.backend.domain.ProjectStatus;
import br.com.fiap.gabinova.backend.domain.ScoreEventType;
import br.com.fiap.gabinova.backend.domain.UserRole;
import br.com.fiap.gabinova.backend.dto.request.CreateProjectRequest;
import br.com.fiap.gabinova.backend.dto.request.UpdateProjectRequest;
import br.com.fiap.gabinova.backend.dto.response.ProjectDto;
import br.com.fiap.gabinova.backend.exception.InvalidStatusTransitionException;
import br.com.fiap.gabinova.backend.repository.ProjectRepository;
import br.com.fiap.gabinova.backend.security.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock private ProjectRepository projectRepository;
    @Mock private IdeaService ideaService;
    @Mock private GamificationService gamificationService;
    @Mock private AuditService auditService;

    private ProjectService projectService;

    private static final AuthenticatedUser MANAGER =
            new AuthenticatedUser("mgr-1", "gestor@gab.com", "Gestor", UserRole.MANAGER);
    private static final AuthenticatedUser ADMIN =
            new AuthenticatedUser("adm-1", "lideranca@gab.com", "Liderança", UserRole.ADMIN);

    @BeforeEach
    void setUp() {
        projectService = new ProjectService(projectRepository, ideaService, gamificationService, auditService);
        lenient().when(projectRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private Project buildProject(String id, ProjectStatus status, int progress) {
        Project p = new Project();
        p.setId(id);
        p.setTitle("Projeto " + id);
        p.setStatus(status);
        p.setStage("Planejamento");
        p.setProgress(progress);
        p.setInvestment("R$ 100.000");
        p.setExpectedReturn("R$ 200.000");
        p.setActualReturn("");
        p.setProductivity("");
        return p;
    }

    @Test
    void criarProjetoSempreNasceAguardandoAprovacaoComProgressoZero() {
        CreateProjectRequest request = new CreateProjectRequest(
                "Novo Projeto", "Ideia origem", "Equipe X", "IN_PROGRESS", "Qualquer",
                "R$ 50.000", "R$ 150.000", "31/12/2026", null, null);

        ProjectDto dto = projectService.create(MANAGER, request);

        assertThat(dto.status()).isEqualTo("AWAITING_APPROVAL");
        assertThat(dto.progress()).isZero();
        assertThat(dto.stage()).isEqualTo("Validação Estratégica");
    }

    @Test
    void criarProjetoComIdeaIdMarcaIdeiaComoImplementadaEConcedePontosAoAutor() {
        Idea idea = new Idea();
        idea.setId("idea-1");
        idea.setAuthorId("collab-1");
        when(ideaService.markImplemented("idea-1")).thenReturn(idea);

        CreateProjectRequest request = new CreateProjectRequest(
                "Novo Projeto", "Ideia origem", "Equipe X", "AWAITING_APPROVAL", "Validação",
                "R$ 50.000", "R$ 150.000", "31/12/2026", "idea-1", null);

        projectService.create(MANAGER, request);

        verify(ideaService).markImplemented("idea-1");
        verify(gamificationService).awardForEvent(eq("collab-1"), eq(ScoreEventType.IDEA_TO_PROJECT), any());
    }

    @Test
    void aprovarProjetoMudaParaEmAndamentoComProgressoMinimoDeQuarenta() {
        Project project = buildProject("p1", ProjectStatus.AWAITING_APPROVAL, 10);
        when(projectRepository.findById("p1")).thenReturn(Optional.of(project));

        ProjectDto dto = projectService.approve("p1", ADMIN);

        assertThat(dto.status()).isEqualTo("IN_PROGRESS");
        assertThat(dto.stage()).isEqualTo("Implantação");
        assertThat(dto.progress()).isEqualTo(40);
    }

    @Test
    void aprovarProjetoPreservaProgressoQuandoJaMaiorQueQuarenta() {
        Project project = buildProject("p2", ProjectStatus.AWAITING_APPROVAL, 65);
        when(projectRepository.findById("p2")).thenReturn(Optional.of(project));

        ProjectDto dto = projectService.approve("p2", ADMIN);

        assertThat(dto.progress()).isEqualTo(65);
    }

    @Test
    void naoPermiteAprovarProjetoQueNaoEstaAguardandoAprovacao() {
        Project project = buildProject("p3", ProjectStatus.IN_PROGRESS, 50);
        when(projectRepository.findById("p3")).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> projectService.approve("p3", ADMIN))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    void concluirProjetoForcaProgressoCemEStageEncerramentoEConcedePontosAoAutorDaIdeia() {
        Project project = buildProject("p4", ProjectStatus.IN_PROGRESS, 80);
        project.setIdeaId("idea-9");
        when(projectRepository.findById("p4")).thenReturn(Optional.of(project));

        Idea idea = new Idea();
        idea.setId("idea-9");
        idea.setAuthorId("collab-9");
        when(ideaService.findOrThrow("idea-9")).thenReturn(idea);

        UpdateProjectRequest request = new UpdateProjectRequest(
                "Projeto Atualizado", "origem", "resp", "COMPLETED", "x",
                "R$ 100.000", "R$ 200.000", "R$ 250.000", "90%", "01/01/2026");

        ProjectDto dto = projectService.update("p4", MANAGER, request);

        assertThat(dto.status()).isEqualTo("COMPLETED");
        assertThat(dto.progress()).isEqualTo(100);
        assertThat(dto.stage()).isEqualTo("Encerramento");
        verify(gamificationService).awardForEvent(eq("collab-9"), eq(ScoreEventType.PROJECT_COMPLETED), any());
    }

    @Test
    void atualizarProjetoParaConcluidoSemIdeaIdNaoTentaConcederPontos() {
        Project project = buildProject("p5", ProjectStatus.IN_PROGRESS, 80);
        when(projectRepository.findById("p5")).thenReturn(Optional.of(project));

        UpdateProjectRequest request = new UpdateProjectRequest(
                "Projeto", "origem", "resp", "COMPLETED", "x",
                "R$ 100.000", "R$ 200.000", "R$ 250.000", "90%", "01/01/2026");

        projectService.update("p5", MANAGER, request);

        verifyNoInteractions(gamificationService);
        verify(ideaService, never()).findOrThrow(any());
    }
}
