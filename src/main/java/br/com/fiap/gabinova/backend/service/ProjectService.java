package br.com.fiap.gabinova.backend.service;

import br.com.fiap.gabinova.backend.domain.Idea;
import br.com.fiap.gabinova.backend.domain.Project;
import br.com.fiap.gabinova.backend.domain.ProjectStatus;
import br.com.fiap.gabinova.backend.domain.ScoreEventType;
import br.com.fiap.gabinova.backend.dto.request.CreateProjectRequest;
import br.com.fiap.gabinova.backend.dto.request.UpdateProjectRequest;
import br.com.fiap.gabinova.backend.dto.response.ProjectDto;
import br.com.fiap.gabinova.backend.exception.BadRequestException;
import br.com.fiap.gabinova.backend.exception.InvalidStatusTransitionException;
import br.com.fiap.gabinova.backend.exception.ResourceNotFoundException;
import br.com.fiap.gabinova.backend.mapper.ProjectMapper;
import br.com.fiap.gabinova.backend.repository.ProjectRepository;
import br.com.fiap.gabinova.backend.security.AuthenticatedUser;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Regras de negocio de projetos/iniciativas - SPEC_FUNCIONAL_BACKEND.md, secao 6.3.
 * Vincula-se a IdeaService para: (a) marcar a ideia de origem como IMPLEMENTED e conceder
 * IDEA_TO_PROJECT quando ideaId e informado; (b) conceder PROJECT_COMPLETED ao autor da ideia
 * de origem quando o projeto e concluido (o campo "responsible" e texto livre, sem vinculo
 * de usuario - o autor da ideia e a unica referencia confiavel a um usuario real).
 */
@Service
public class ProjectService {

    private static final int APPROVAL_PROGRESS_CAP = 70;
    private static final int APPROVED_MIN_PROGRESS = 40;

    private final ProjectRepository projectRepository;
    private final IdeaService ideaService;
    private final GamificationService gamificationService;
    private final AuditService auditService;

    public ProjectService(ProjectRepository projectRepository,
                           IdeaService ideaService,
                           GamificationService gamificationService,
                           AuditService auditService) {
        this.projectRepository = projectRepository;
        this.ideaService = ideaService;
        this.gamificationService = gamificationService;
        this.auditService = auditService;
    }

    public List<ProjectDto> list() {
        return projectRepository.findAll().stream().map(ProjectMapper::toDto).toList();
    }

    public ProjectDto create(AuthenticatedUser requester, CreateProjectRequest request) {
        Project project = new Project();
        project.setTitle(request.title().trim());
        project.setIdeaOrigin(request.ideaOrigin() == null ? "" : request.ideaOrigin().trim());
        project.setResponsible(request.responsible() == null ? "" : request.responsible().trim());
        project.setStatus(ProjectStatus.AWAITING_APPROVAL);
        project.setStage("Validação Estratégica");
        project.setInvestment(nullToEmpty(request.investment()));
        project.setExpectedReturn(nullToEmpty(request.expectedReturn()));
        project.setActualReturn("");
        project.setProductivity("");
        project.setProgress(0);
        project.setDeadline(nullToEmpty(request.deadline()));
        project.setIdeaId(request.ideaId());
        project.setGuidelineId(request.guidelineId());
        project.setCreatedAt(Instant.now());
        project.setUpdatedAt(Instant.now());

        Project saved = projectRepository.save(project);

        if (request.ideaId() != null && !request.ideaId().isBlank()) {
            Idea idea = ideaService.markImplemented(request.ideaId());
            gamificationService.awardForEvent(idea.getAuthorId(), ScoreEventType.IDEA_TO_PROJECT,
                    "Ideia transformada em projeto: " + saved.getTitle());
        }

        auditService.record(requester.userId(), requester.role().name(), "CREATE_PROJECT", "project", saved.getId(), "SUCCESS");

        return ProjectMapper.toDto(saved);
    }

    public ProjectDto update(String id, AuthenticatedUser requester, UpdateProjectRequest request) {
        Project project = findOrThrow(id);
        ProjectStatus newStatus = parseStatus(request.status());

        project.setTitle(request.title().trim());
        project.setIdeaOrigin(nullToEmpty(request.ideaOrigin()));
        project.setResponsible(nullToEmpty(request.responsible()));
        project.setStage(nullToEmpty(request.stage()));
        project.setInvestment(nullToEmpty(request.investment()));
        project.setExpectedReturn(nullToEmpty(request.expectedReturn()));
        project.setActualReturn(nullToEmpty(request.actualReturn()));
        project.setProductivity(nullToEmpty(request.productivity()));
        project.setDeadline(nullToEmpty(request.deadline()));

        boolean becameCompleted = newStatus == ProjectStatus.COMPLETED && project.getStatus() != ProjectStatus.COMPLETED;

        project.setStatus(newStatus);
        if (newStatus == ProjectStatus.COMPLETED) {
            project.setProgress(100);
            project.setStage("Encerramento");
        } else if (newStatus == ProjectStatus.AWAITING_APPROVAL) {
            project.setProgress(Math.min(project.getProgress(), APPROVAL_PROGRESS_CAP));
        }
        project.setUpdatedAt(Instant.now());

        Project saved = projectRepository.save(project);

        if (becameCompleted && saved.getIdeaId() != null && !saved.getIdeaId().isBlank()) {
            Idea idea = ideaService.findOrThrow(saved.getIdeaId());
            gamificationService.awardForEvent(idea.getAuthorId(), ScoreEventType.PROJECT_COMPLETED,
                    "Projeto concluido com resultado mensuravel: " + saved.getTitle());
        }

        auditService.record(requester.userId(), requester.role().name(), "UPDATE_PROJECT", "project", saved.getId(), "SUCCESS");

        return ProjectMapper.toDto(saved);
    }

    public ProjectDto updateProgress(String id, AuthenticatedUser requester, int progress) {
        Project project = findOrThrow(id);
        project.setProgress(Math.max(0, Math.min(100, progress)));
        project.setUpdatedAt(Instant.now());

        Project saved = projectRepository.save(project);
        auditService.record(requester.userId(), requester.role().name(), "UPDATE_PROJECT_PROGRESS", "project", saved.getId(), "SUCCESS");
        return ProjectMapper.toDto(saved);
    }

    public ProjectDto approve(String id, AuthenticatedUser requester) {
        Project project = findOrThrow(id);

        if (project.getStatus() != ProjectStatus.AWAITING_APPROVAL) {
            throw new InvalidStatusTransitionException(
                    "Somente projetos aguardando aprovacao podem ser aprovados (status atual: " + project.getStatus() + ").");
        }

        project.setStatus(ProjectStatus.IN_PROGRESS);
        project.setStage("Implantação");
        project.setProgress(Math.max(project.getProgress(), APPROVED_MIN_PROGRESS));
        project.setUpdatedAt(Instant.now());

        Project saved = projectRepository.save(project);
        auditService.record(requester.userId(), requester.role().name(), "APPROVE_PROJECT", "project", saved.getId(), "SUCCESS");
        return ProjectMapper.toDto(saved);
    }

    private Project findOrThrow(String id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto nao encontrado: " + id));
    }

    private ProjectStatus parseStatus(String value) {
        try {
            return ProjectStatus.valueOf(value);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BadRequestException("Status de projeto invalido: " + value);
        }
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
