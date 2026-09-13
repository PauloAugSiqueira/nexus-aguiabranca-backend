package br.com.fiap.gabinova.backend.service;

import br.com.fiap.gabinova.backend.domain.Idea;
import br.com.fiap.gabinova.backend.domain.IdeaStatus;
import br.com.fiap.gabinova.backend.domain.ScoreEventType;
import br.com.fiap.gabinova.backend.domain.UserRole;
import br.com.fiap.gabinova.backend.dto.request.CreateIdeaRequest;
import br.com.fiap.gabinova.backend.dto.response.IdeaDto;
import br.com.fiap.gabinova.backend.exception.BadRequestException;
import br.com.fiap.gabinova.backend.exception.ForbiddenOperationException;
import br.com.fiap.gabinova.backend.exception.InvalidStatusTransitionException;
import br.com.fiap.gabinova.backend.exception.ResourceNotFoundException;
import br.com.fiap.gabinova.backend.mapper.IdeaMapper;
import br.com.fiap.gabinova.backend.repository.IdeaRepository;
import br.com.fiap.gabinova.backend.security.AuthenticatedUser;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

/**
 * Regras de negocio de ideias - transicoes de status e disparo de gamificacao seguem
 * SPEC_FUNCIONAL_BACKEND.md, secao 6.2.
 */

@Service
public class IdeaService {

    private static final int DESCRIPTION_BONUS_THRESHOLD = 80;

    private final IdeaRepository ideaRepository;
    private final ScoringService scoringService;
    private final GamificationService gamificationService;
    private final AuditService auditService;

    public IdeaService(IdeaRepository ideaRepository,
                       ScoringService scoringService,
                       GamificationService gamificationService,
                       AuditService auditService) {
        this.ideaRepository = ideaRepository;
        this.scoringService = scoringService;
        this.gamificationService = gamificationService;
        this.auditService = auditService;
    }

    public IdeaDto create(AuthenticatedUser author, CreateIdeaRequest request) {
        Idea idea = new Idea();
        idea.setTitle(request.title().trim());
        idea.setDescription(request.description() == null ? "" : request.description().trim());
        idea.setSector(request.sector());
        idea.setCategory(request.category());
        idea.setExpectedImpact(request.expectedImpact() == null ? "" : request.expectedImpact().trim());
        idea.setUrgency(request.urgency());
        idea.setGuidelineId(request.guidelineId());
        idea.setStatus(IdeaStatus.PENDING);
        idea.setAuthorId(author.userId());
        idea.setAuthorName(author.name());
        idea.setScore(scoringService.calculateNexusScore(
                idea.getCategory(), idea.getSector(), idea.getExpectedImpact(), idea.getDescription(), idea.getUrgency()));
        idea.setCreatedAt(Instant.now());
        idea.setUpdatedAt(Instant.now());

        Idea saved = ideaRepository.save(idea);

        gamificationService.awardForEvent(author.userId(), ScoreEventType.IDEA_CREATED,
                "Ideia enviada: " + saved.getTitle());

        if (saved.getDescription() != null && saved.getDescription().length() >= DESCRIPTION_BONUS_THRESHOLD) {
            gamificationService.awardForEvent(author.userId(), ScoreEventType.IDEA_DESCRIBED,
                    "Descrição completa do problema e do impacto esperado");
        }

        auditService.record(author.userId(), author.role().name(), "CREATE_IDEA", "idea", saved.getId(), "SUCCESS");

        return IdeaMapper.toDto(saved);
    }

    public IdeaDto update(String id, AuthenticatedUser requester, CreateIdeaRequest request) {
        Idea idea = findOrThrow(id);

        if (!idea.getAuthorId().equals(requester.userId())) {
            throw new ForbiddenOperationException("Apenas o autor pode editar esta ideia.");
        }
        if (idea.getStatus() != IdeaStatus.PENDING) {
            throw new InvalidStatusTransitionException("A ideia so pode ser editada enquanto estiver pendente.");
        }

        idea.setTitle(request.title().trim());
        idea.setDescription(request.description() == null ? "" : request.description().trim());
        idea.setSector(request.sector());
        idea.setCategory(request.category());
        idea.setExpectedImpact(request.expectedImpact() == null ? "" : request.expectedImpact().trim());
        idea.setUrgency(request.urgency());
        idea.setGuidelineId(request.guidelineId());
        idea.setScore(scoringService.calculateNexusScore(
                idea.getCategory(), idea.getSector(), idea.getExpectedImpact(), idea.getDescription(), idea.getUrgency()));
        idea.setUpdatedAt(Instant.now());

        Idea saved = ideaRepository.save(idea);
        auditService.record(requester.userId(), requester.role().name(), "UPDATE_IDEA", "idea", saved.getId(), "SUCCESS");
        return IdeaMapper.toDto(saved);
    }

    public List<IdeaDto> list(AuthenticatedUser requester) {
        List<Idea> ideas = requester.role() == UserRole.COLLABORATOR
                ? ideaRepository.findByAuthorId(requester.userId())
                : ideaRepository.findAll();

        return ideas.stream()
                .sorted(Comparator.comparingInt(Idea::getScore).reversed())
                .map(IdeaMapper::toDto)
                .toList();
    }

    public List<IdeaDto> listByAuthor(String authorId, AuthenticatedUser requester) {
        boolean isOwner = requester.userId().equals(authorId);
        boolean isManagerOrAdmin = requester.role() == UserRole.MANAGER || requester.role() == UserRole.ADMIN;

        if (!isOwner && !isManagerOrAdmin) {
            throw new ForbiddenOperationException("Voce nao pode consultar ideias de outro usuario.");
        }

        return ideaRepository.findByAuthorId(authorId).stream()
                .map(IdeaMapper::toDto)
                .toList();
    }

    public IdeaDto updateStatus(String id, AuthenticatedUser requester, String statusValue) {
        Idea idea = findOrThrow(id);
        IdeaStatus newStatus = parseStatus(statusValue);

        if (newStatus != IdeaStatus.APPROVED && newStatus != IdeaStatus.NOT_APPROVED) {
            throw new BadRequestException("Status invalido. Use APPROVED ou NOT_APPROVED.");
        }
        if (idea.getStatus() != IdeaStatus.PENDING) {
            throw new InvalidStatusTransitionException(
                    "Esta ideia nao pode mais ser aprovada/recusada (status atual: " + idea.getStatus() + ").");
        }

        idea.setStatus(newStatus);
        if (newStatus == IdeaStatus.APPROVED) {
            idea.setScore(Math.min(100, idea.getScore() + 5));
        }
        idea.setUpdatedAt(Instant.now());

        Idea saved = ideaRepository.save(idea);

        if (newStatus == IdeaStatus.APPROVED) {
            gamificationService.awardForEvent(saved.getAuthorId(), ScoreEventType.IDEA_APPROVED,
                    "Ideia aprovada: " + saved.getTitle());
        }

        auditService.record(requester.userId(), requester.role().name(),
                "UPDATE_IDEA_STATUS:" + newStatus, "idea", saved.getId(), "SUCCESS");

        return IdeaMapper.toDto(saved);
    }

    public IdeaDto prioritize(String id, AuthenticatedUser requester) {
        Idea idea = findOrThrow(id);

        if (idea.getStatus() != IdeaStatus.PENDING) {
            throw new InvalidStatusTransitionException(
                    "Esta ideia nao pode mais ser priorizada (status atual: " + idea.getStatus() + ").");
        }

        // Altera o status para IMPLEMENTED ao priorizar
        idea.setStatus(IdeaStatus.IMPLEMENTED);
        idea.setScore(Math.min(100, idea.getScore() + 10));
        idea.setUpdatedAt(Instant.now());

        Idea saved = ideaRepository.save(idea);

        gamificationService.awardForEvent(saved.getAuthorId(), ScoreEventType.IDEA_PRIORITIZED,
                "Ideia priorizada e movida para implementada: " + saved.getTitle());

        auditService.record(requester.userId(), requester.role().name(), "PRIORITIZE_IDEA", "idea", saved.getId(), "SUCCESS");

        return IdeaMapper.toDto(saved);
    }

    /**
     * Uso interno do ProjectService quando um projeto e criado a partir de uma ideia (ideaId).
     */
    Idea markImplemented(String ideaId) {
        Idea idea = findOrThrow(ideaId);
        idea.setStatus(IdeaStatus.IMPLEMENTED);
        idea.setUpdatedAt(Instant.now());
        return ideaRepository.save(idea);
    }

    Idea findOrThrow(String id) {
        return ideaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ideia nao encontrada: " + id));
    }

    private IdeaStatus parseStatus(String value) {
        try {
            return IdeaStatus.valueOf(value);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BadRequestException("Status invalido: " + value);
        }
    }

    public IdeaDto startImplementation(String id, AuthenticatedUser requester) {
        Idea idea = findOrThrow(id);

        if (idea.getStatus() != IdeaStatus.APPROVED) {
            throw new InvalidStatusTransitionException(
                    "Apenas ideias aprovadas podem ter a implantação iniciada (status atual: " + idea.getStatus() + ").");
        }

        idea.setStatus(IdeaStatus.IMPLEMENTED);
        idea.setUpdatedAt(Instant.now());

        Idea saved = ideaRepository.save(idea);

        gamificationService.awardForEvent(saved.getAuthorId(), ScoreEventType.IDEA_PRIORITIZED,
                "Implantação iniciada para a ideia: " + saved.getTitle());

        auditService.record(requester.userId(), requester.role().name(), "START_IMPLEMENTATION", "idea", saved.getId(), "SUCCESS");

        return IdeaMapper.toDto(saved);
    }

    public void delete(String id, AuthenticatedUser requester) {
        Idea idea = findOrThrow(id);

        // Opcional: Se quiser permitir apenas o autor ou gestor/admin excluírem
        boolean isOwner = idea.getAuthorId().equals(requester.userId());
        boolean isManagerOrAdmin = requester.role() == UserRole.MANAGER || requester.role() == UserRole.ADMIN;
        if (!isOwner && !isManagerOrAdmin) {
            throw new ForbiddenOperationException("Você não tem permissão para excluir esta ideia.");
        }

        ideaRepository.delete(idea);
        auditService.record(requester.userId(), requester.role().name(), "DELETE_IDEA", "idea", id, "SUCCESS");
    }
}