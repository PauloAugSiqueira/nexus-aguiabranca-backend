package br.com.fiap.gabinova.backend.service;

import br.com.fiap.gabinova.backend.domain.*;
import br.com.fiap.gabinova.backend.dto.response.GamificationDto;
import br.com.fiap.gabinova.backend.dto.response.RankingDto;
import br.com.fiap.gabinova.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GamificationServiceTest {

    @Mock private GamificationProfileRepository profileRepository;
    @Mock private ScoreHistoryRepository scoreHistoryRepository;
    @Mock private UserRepository userRepository;
    @Mock private IdeaRepository ideaRepository;
    @Mock private ProjectRepository projectRepository;

    private GamificationService gamificationService;

    @BeforeEach
    void setUp() {
        gamificationService = new GamificationService(
                profileRepository, scoreHistoryRepository, userRepository, ideaRepository, projectRepository);
    }

    private User buildUser(String id) {
        User user = new User("Operador Teste", "operador@teste.com", "hash", UserRole.COLLABORATOR, "Operações");
        user.setId(id);
        return user;
    }

    @Test
    void awardForEventSomaPontosDaTabelaFixaEAtualizaNivel() {
        String userId = "u1";
        GamificationProfile profile = new GamificationProfile(userId);
        User user = buildUser(userId);

        when(profileRepository.findById(userId)).thenReturn(Optional.of(profile));
        when(profileRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(ideaRepository.findByAuthorId(userId)).thenReturn(List.of());

        GamificationDto dto = gamificationService.awardForEvent(userId, ScoreEventType.IDEA_CREATED, "Ideia criada");

        assertThat(dto.points()).isEqualTo(50);
        assertThat(dto.level()).isEqualTo(1);
        assertThat(dto.levelName()).isEqualTo("Explorador Nexus");

        verify(scoreHistoryRepository).save(argThat(entry ->
                entry.getPoints() == 50 && entry.getEventType() == ScoreEventType.IDEA_CREATED));
    }

    @Test
    void pontosAcumuladosSobem10NivelParaInovadorOperacional() {
        String userId = "u2";
        GamificationProfile profile = new GamificationProfile(userId);
        profile.setPoints(1400);
        User user = buildUser(userId);
        user.setPoints(1400);

        when(profileRepository.findById(userId)).thenReturn(Optional.of(profile));
        when(profileRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(ideaRepository.findByAuthorId(userId)).thenReturn(List.of());

        // +500 (IDEA_APPROVED) leva de 1400 para 1900 -> nivel 2 (Inovador Operacional, faixa 1500-2999)
        GamificationDto dto = gamificationService.awardForEvent(userId, ScoreEventType.IDEA_APPROVED, "Aprovada");

        assertThat(dto.points()).isEqualTo(1900);
        assertThat(dto.level()).isEqualTo(2);
        assertThat(dto.levelName()).isEqualTo("Inovador Operacional");
    }

    @Test
    void getRankingOrdenaColaboradoresPorPontosDecrescente() {
        User first = buildUser("a");
        first.setPoints(100);
        first.setName("Ana");
        first.setDepartment("TI");

        User second = buildUser("b");
        second.setPoints(500);
        second.setName("Bruno");
        second.setDepartment("Operações");

        when(userRepository.findByRole(UserRole.COLLABORATOR)).thenReturn(new ArrayList<>(List.of(first, second)));

        List<RankingDto> ranking = gamificationService.getRanking();

        assertThat(ranking).hasSize(2);
        assertThat(ranking.get(0).userName()).isEqualTo("Bruno");
        assertThat(ranking.get(0).position()).isEqualTo(1);
        assertThat(ranking.get(1).userName()).isEqualTo("Ana");
        assertThat(ranking.get(1).position()).isEqualTo(2);
    }

    @Test
    void recomputeBadgesDesbloqueiaPrimeiraIdeiaAoCriarIdeia() {
        String userId = "u3";
        GamificationProfile profile = new GamificationProfile(userId);
        User user = buildUser(userId);

        Idea idea = new Idea();
        idea.setId("i1");
        idea.setAuthorId(userId);
        idea.setStatus(IdeaStatus.PENDING);
        idea.setCategory("Melhoria de Processo");

        when(profileRepository.findById(userId)).thenReturn(Optional.of(profile));
        when(profileRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(ideaRepository.findByAuthorId(userId)).thenReturn(List.of(idea));
        when(projectRepository.findByIdeaId(any())).thenReturn(Optional.empty());

        gamificationService.awardForEvent(userId, ScoreEventType.IDEA_CREATED, "Ideia criada");

        assertThat(profile.getBadges()).anySatisfy(badge -> {
            assertThat(badge.getId()).isEqualTo("1");
            assertThat(badge.isEarned()).isTrue();
        });
    }
}
