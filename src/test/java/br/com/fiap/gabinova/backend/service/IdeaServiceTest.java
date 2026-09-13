package br.com.fiap.gabinova.backend.service;

import br.com.fiap.gabinova.backend.domain.Idea;
import br.com.fiap.gabinova.backend.domain.IdeaStatus;
import br.com.fiap.gabinova.backend.domain.ScoreEventType;
import br.com.fiap.gabinova.backend.domain.UserRole;
import br.com.fiap.gabinova.backend.dto.request.CreateIdeaRequest;
import br.com.fiap.gabinova.backend.dto.response.IdeaDto;
import br.com.fiap.gabinova.backend.exception.ForbiddenOperationException;
import br.com.fiap.gabinova.backend.exception.InvalidStatusTransitionException;
import br.com.fiap.gabinova.backend.exception.ResourceNotFoundException;
import br.com.fiap.gabinova.backend.repository.IdeaRepository;
import br.com.fiap.gabinova.backend.security.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdeaServiceTest {

    @Mock
    private IdeaRepository ideaRepository;
    @Mock
    private GamificationService gamificationService;
    @Mock
    private AuditService auditService;

    private IdeaService ideaService;

    private static final AuthenticatedUser COLLABORATOR =
            new AuthenticatedUser("collab-1", "operador@gab.com", "Operador", UserRole.COLLABORATOR);
    private static final AuthenticatedUser MANAGER =
            new AuthenticatedUser("mgr-1", "gestor@gab.com", "Gestor", UserRole.MANAGER);

    @BeforeEach
    void setUp() {
        // ScoringService real (regra pura), demais dependencias mockadas.
        ideaService = new IdeaService(ideaRepository, new ScoringService(), gamificationService, auditService);
        lenient().when(ideaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private Idea buildIdea(String id, String authorId, IdeaStatus status, int score) {
        Idea idea = new Idea();
        idea.setId(id);
        idea.setAuthorId(authorId);
        idea.setAuthorName("Operador");
        idea.setTitle("Ideia " + id);
        idea.setDescription("Descricao");
        idea.setSector("Operações");
        idea.setCategory("Melhoria de Processo");
        idea.setExpectedImpact("Reducao de custo");
        idea.setUrgency(3);
        idea.setStatus(status);
        idea.setScore(score);
        return idea;
    }

    @Test
    void criarIdeiaDefineStatusPendenteAutorDoTokenEConcedePontos() {
        CreateIdeaRequest request = new CreateIdeaRequest(
                "Nova ideia", "a".repeat(90), "Operações", "Redução de Custo", "Economia real", 5, null);

        IdeaDto dto = ideaService.create(COLLABORATOR, request);

        assertThat(dto.status()).isEqualTo("PENDING");
        assertThat(dto.authorId()).isEqualTo("collab-1");
        assertThat(dto.score()).isGreaterThan(0);

        verify(gamificationService).awardForEvent(eq("collab-1"), eq(ScoreEventType.IDEA_CREATED), any());
        // descricao >= 80 caracteres -> bonus IDEA_DESCRIBED tambem deve ser concedido
        verify(gamificationService).awardForEvent(eq("collab-1"), eq(ScoreEventType.IDEA_DESCRIBED), any());
    }

    @Test
    void criarIdeiaComDescricaoCurtaNaoConcedeBonusDeDescricao() {
        CreateIdeaRequest request = new CreateIdeaRequest(
                "Nova ideia", "curta", "Operações", "Melhoria de Processo", "", 2, null);

        ideaService.create(COLLABORATOR, request);

        verify(gamificationService).awardForEvent(eq("collab-1"), eq(ScoreEventType.IDEA_CREATED), any());
        verify(gamificationService, never()).awardForEvent(eq("collab-1"), eq(ScoreEventType.IDEA_DESCRIBED), any());
    }

    @Test
    void gestorAprovaIdeiaPendenteEGanhaCincoPontosDeScoreComBonusAoAutor() {
        Idea idea = buildIdea("idea-1", "collab-1", IdeaStatus.PENDING, 50);
        when(ideaRepository.findById("idea-1")).thenReturn(Optional.of(idea));

        IdeaDto dto = ideaService.updateStatus("idea-1", MANAGER, "APPROVED");

        assertThat(dto.status()).isEqualTo("APPROVED");
        assertThat(dto.score()).isEqualTo(55);
        verify(gamificationService).awardForEvent(eq("collab-1"), eq(ScoreEventType.IDEA_APPROVED), any());
    }

    @Test
    void naoPermiteAprovarIdeiaJaAprovada() {
        Idea idea = buildIdea("idea-2", "collab-1", IdeaStatus.APPROVED, 60);
        when(ideaRepository.findById("idea-2")).thenReturn(Optional.of(idea));

        assertThatThrownBy(() -> ideaService.updateStatus("idea-2", MANAGER, "APPROVED"))
                .isInstanceOf(InvalidStatusTransitionException.class);

        verify(gamificationService, never()).awardForEvent(any(), any(), any());
    }

    @Test
    void priorizarIdeiaMudaStatusParaEmAnaliseESomaDezPontosDeScore() {
        Idea idea = buildIdea("idea-3", "collab-1", IdeaStatus.PENDING, 40);
        when(ideaRepository.findById("idea-3")).thenReturn(Optional.of(idea));

        IdeaDto dto = ideaService.prioritize("idea-3", MANAGER);

        assertThat(dto.status()).isEqualTo("IN_REVIEW");
        assertThat(dto.score()).isEqualTo(50);
        verify(gamificationService).awardForEvent(eq("collab-1"), eq(ScoreEventType.IDEA_PRIORITIZED), any());
    }

    @Test
    void scoreNaoUltrapassaCemAoAprovarOuPriorizar() {
        Idea idea = buildIdea("idea-4", "collab-1", IdeaStatus.PENDING, 98);
        when(ideaRepository.findById("idea-4")).thenReturn(Optional.of(idea));

        IdeaDto dto = ideaService.prioritize("idea-4", MANAGER);

        assertThat(dto.score()).isEqualTo(100);
    }

    @Test
    void apenasAutorPodeEditarIdeia() {
        Idea idea = buildIdea("idea-5", "outro-usuario", IdeaStatus.PENDING, 30);
        when(ideaRepository.findById("idea-5")).thenReturn(Optional.of(idea));

        CreateIdeaRequest request = new CreateIdeaRequest(
                "Titulo", "desc", "Operações", "Segurança", "", 2, null);

        assertThatThrownBy(() -> ideaService.update("idea-5", COLLABORATOR, request))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void naoPermiteEditarIdeiaQueJaSaiuDoPendente() {
        Idea idea = buildIdea("idea-6", "collab-1", IdeaStatus.APPROVED, 30);
        when(ideaRepository.findById("idea-6")).thenReturn(Optional.of(idea));

        CreateIdeaRequest request = new CreateIdeaRequest(
                "Titulo", "desc", "Operações", "Segurança", "", 2, null);

        assertThatThrownBy(() -> ideaService.update("idea-6", COLLABORATOR, request))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    void operadorVeApenasAsPropriasIdeiasEGestorVeTodas() {
        Idea own = buildIdea("i1", "collab-1", IdeaStatus.PENDING, 10);
        when(ideaRepository.findByAuthorId("collab-1")).thenReturn(List.of(own));

        List<IdeaDto> collaboratorView = ideaService.list(COLLABORATOR);
        assertThat(collaboratorView).extracting(IdeaDto::id).containsExactly("i1");

        Idea other = buildIdea("i2", "collab-2", IdeaStatus.PENDING, 90);
        when(ideaRepository.findAll()).thenReturn(List.of(own, other));

        List<IdeaDto> managerView = ideaService.list(MANAGER);
        assertThat(managerView).extracting(IdeaDto::id).containsExactly("i2", "i1"); // ordenado por score desc
    }

    @Test
    void listByAuthorNegaAcessoParaUsuarioQueNaoEDonoNemGestor() {
        assertThatThrownBy(() -> ideaService.listByAuthor("outro-id", COLLABORATOR))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void ideiaInexistenteLancaResourceNotFound() {
        when(ideaRepository.findById("nao-existe")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ideaService.prioritize("nao-existe", MANAGER))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
