package br.com.fiap.gabinova.backend.service;

import br.com.fiap.gabinova.backend.domain.*;
import br.com.fiap.gabinova.backend.dto.response.BadgeDto;
import br.com.fiap.gabinova.backend.dto.response.GamificationDto;
import br.com.fiap.gabinova.backend.dto.response.RankingDto;
import br.com.fiap.gabinova.backend.exception.ResourceNotFoundException;
import br.com.fiap.gabinova.backend.mapper.GamificationMapper;
import br.com.fiap.gabinova.backend.repository.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Centraliza a tabela de pontos, niveis e badges da gamificacao (SPEC_FUNCIONAL_BACKEND.md, secao 6.5).
 * E o UNICO lugar que deve chamar awardForEvent(...) - IdeaService e ProjectService disparam eventos aqui,
 * nunca duplicam a tabela de pontos (SPEC_TECNICA_BACKEND.md, secao 8).
 */
@Service
public class GamificationService {

    private record LevelDef(int number, String name, int minPoints) {
    }

    private static final List<LevelDef> LEVELS = List.of(
            new LevelDef(1, "Explorador Nexus", 0),
            new LevelDef(2, "Inovador Operacional", 1500),
            new LevelDef(3, "Especialista Nexus", 3000),
            new LevelDef(4, "Referência em Inovação", 5000),
            new LevelDef(5, "Embaixador da Inovação", 7000)
    );

    private static final Map<ScoreEventType, Integer> POINTS_TABLE = Map.of(
            ScoreEventType.IDEA_CREATED, 50,
            ScoreEventType.IDEA_DESCRIBED, 100,
            ScoreEventType.IDEA_PRIORITIZED, 300,
            ScoreEventType.IDEA_APPROVED, 500,
            ScoreEventType.IDEA_TO_PROJECT, 1000,
            ScoreEventType.PROJECT_COMPLETED, 2000
    );

    private record BadgeDef(String id, String name) {
    }

    private static final List<BadgeDef> BADGE_CATALOG = List.of(
            new BadgeDef("1", "Primeira Ideia"),
            new BadgeDef("2", "Ideia Aprovada"),
            new BadgeDef("3", "Alto Impacto Estratégico"),
            new BadgeDef("4", "Redução de Custo"),
            new BadgeDef("5", "Ideia Virou Projeto"),
            new BadgeDef("6", "Resultado Mensurável"),
            new BadgeDef("7", "ROI Positivo")
    );

    private final GamificationProfileRepository profileRepository;
    private final ScoreHistoryRepository scoreHistoryRepository;
    private final UserRepository userRepository;
    private final IdeaRepository ideaRepository;
    private final ProjectRepository projectRepository;

    public GamificationService(GamificationProfileRepository profileRepository,
                                ScoreHistoryRepository scoreHistoryRepository,
                                UserRepository userRepository,
                                IdeaRepository ideaRepository,
                                ProjectRepository projectRepository) {
        this.profileRepository = profileRepository;
        this.scoreHistoryRepository = scoreHistoryRepository;
        this.userRepository = userRepository;
        this.ideaRepository = ideaRepository;
        this.projectRepository = projectRepository;
    }

    public GamificationDto awardForEvent(String userId, ScoreEventType type, String description) {
        int points = POINTS_TABLE.getOrDefault(type, 0);
        return applyPoints(userId, points, type, description);
    }

    public GamificationDto awardManual(String userId, int points, ScoreEventType type, String description) {
        return applyPoints(userId, points, type, description);
    }

    private GamificationDto applyPoints(String userId, int points, ScoreEventType type, String description) {
        GamificationProfile profile = getOrCreateProfile(userId);
        profile.setPoints(profile.getPoints() + points);

        LevelDef level = levelOf(profile.getPoints());
        profile.setLevel(level.number());
        profile.setLevelName(level.name());

        profileRepository.save(profile);

        userRepository.findById(userId).ifPresent(user -> {
            user.setPoints(profile.getPoints());
            user.setLevel(level.number());
            userRepository.save(user);
        });

        scoreHistoryRepository.save(new ScoreHistoryEntry(userId, points, description, type));

        recomputeBadges(userId);

        return getProfile(userId);
    }

    public GamificationDto getProfile(String userId) {
        GamificationProfile profile = getOrCreateProfile(userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado: " + userId));

        LevelDef current = levelOf(profile.getPoints());
        LevelDef next = LEVELS.stream()
                .filter(l -> l.number() == current.number() + 1)
                .findFirst()
                .orElse(null);

        float progress;
        if (next == null) {
            progress = 1f;
        } else {
            float span = next.minPoints() - current.minPoints();
            float inLevel = profile.getPoints() - current.minPoints();
            progress = Math.max(0f, Math.min(1f, inLevel / span));
        }

        List<BadgeDto> badgeDtos = profile.getBadges().stream()
                .map(GamificationMapper::toDto)
                .toList();

        List<br.com.fiap.gabinova.backend.dto.response.ScoreHistoryDto> historyDtos =
                scoreHistoryRepository.findByUserIdOrderByDateDesc(userId).stream()
                        .map(GamificationMapper::toDto)
                        .toList();

        return new GamificationDto(
                userId,
                user.getName(),
                profile.getPoints(),
                current.number(),
                current.name(),
                progress,
                badgeDtos,
                historyDtos
        );
    }

    public List<RankingDto> getRanking() {
        List<User> collaborators = userRepository.findByRole(UserRole.COLLABORATOR).stream()
                .sorted(Comparator.comparingInt(User::getPoints).reversed())
                .toList();

        List<RankingDto> ranking = new ArrayList<>();
        for (int i = 0; i < collaborators.size(); i++) {
            User u = collaborators.get(i);
            ranking.add(new RankingDto(i + 1, u.getId(), u.getName(), u.getPoints(), u.getDepartment()));
        }
        return ranking;
    }

    private GamificationProfile getOrCreateProfile(String userId) {
        return profileRepository.findById(userId)
                .orElseGet(() -> profileRepository.save(new GamificationProfile(userId)));
    }

    private LevelDef levelOf(int points) {
        return LEVELS.stream()
                .filter(l -> points >= l.minPoints())
                .reduce((first, second) -> second)
                .orElse(LEVELS.get(0));
    }

    private void recomputeBadges(String userId) {
        GamificationProfile profile = getOrCreateProfile(userId);

        List<Idea> userIdeas = ideaRepository.findByAuthorId(userId);

        boolean hasIdea = !userIdeas.isEmpty();
        boolean hasApproved = userIdeas.stream().anyMatch(i -> i.getStatus() == IdeaStatus.APPROVED);
        boolean hasHighImpact = userIdeas.stream().anyMatch(i ->
                i.getGuidelineId() != null && !i.getGuidelineId().isBlank() && i.getScore() >= 80);
        boolean hasCostReduction = userIdeas.stream().anyMatch(i -> "Redução de Custo".equals(i.getCategory()));
        boolean hasImplemented = userIdeas.stream().anyMatch(i -> i.getStatus() == IdeaStatus.IMPLEMENTED);

        List<Project> linkedProjects = userIdeas.stream()
                .map(Idea::getId)
                .map(projectRepository::findByIdeaId)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        boolean hasCompletedProject = linkedProjects.stream().anyMatch(p -> p.getStatus() == ProjectStatus.COMPLETED);
        boolean hasPositiveRoi = linkedProjects.stream()
                .filter(p -> p.getStatus() == ProjectStatus.COMPLETED)
                .anyMatch(p -> MoneyUtils.parse(p.getActualReturn()) > MoneyUtils.parse(p.getInvestment()));

        Map<String, Boolean> unlocked = Map.of(
                "1", hasIdea,
                "2", hasApproved,
                "3", hasHighImpact,
                "4", hasCostReduction,
                "5", hasImplemented,
                "6", hasCompletedProject,
                "7", hasPositiveRoi
        );

        List<Badge> updated = new ArrayList<>();
        for (BadgeDef def : BADGE_CATALOG) {
            Badge existing = profile.getBadges().stream()
                    .filter(b -> b.getId().equals(def.id()))
                    .findFirst()
                    .orElse(new Badge(def.id(), def.name(), false, null));

            boolean nowEarned = Boolean.TRUE.equals(unlocked.get(def.id()));
            if (nowEarned && !existing.isEarned()) {
                existing.setEarned(true);
                existing.setEarnedAt(java.time.Instant.now().toString());
            }
            updated.add(existing);
        }

        profile.setBadges(updated);
        profileRepository.save(profile);
    }
}
