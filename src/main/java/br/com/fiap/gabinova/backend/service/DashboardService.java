package br.com.fiap.gabinova.backend.service;

import br.com.fiap.gabinova.backend.domain.*;
import br.com.fiap.gabinova.backend.dto.response.DashboardDto;
import br.com.fiap.gabinova.backend.repository.IdeaRepository;
import br.com.fiap.gabinova.backend.repository.ProjectRepository;
import br.com.fiap.gabinova.backend.repository.StrategicGuidelineRepository;
import br.com.fiap.gabinova.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Agregacoes do dashboard executivo - formulas em SPEC_FUNCIONAL_BACKEND.md, secao 6.4.
 */
@Service
public class DashboardService {

    private final IdeaRepository ideaRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final StrategicGuidelineRepository guidelineRepository;

    public DashboardService(IdeaRepository ideaRepository,
                            ProjectRepository projectRepository,
                            UserRepository userRepository,
                            StrategicGuidelineRepository guidelineRepository) {
        this.ideaRepository = ideaRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.guidelineRepository = guidelineRepository;
    }

    public DashboardDto getDashboard() {
        List<Idea> ideas = ideaRepository.findAll();
        List<Project> projects = projectRepository.findAll();
        List<User> collaborators = userRepository.findByRole(UserRole.COLLABORATOR);

        int totalIdeas = ideas.size();

        // Contagem real de ideias aprovadas por status e sincronização automática com projetos criados
        int statusApprovedIdeas = (int) ideas.stream().filter(i -> i.getStatus() == IdeaStatus.APPROVED).count();
        int approvedIdeas = Math.max(statusApprovedIdeas, projects.size());

        int pendingIdeas = (int) ideas.stream().filter(i -> i.getStatus() == IdeaStatus.PENDING).count();

        Instant startOfMonth = YearMonth.now(ZoneOffset.UTC).atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        int ideasThisMonth = (int) ideas.stream()
                .filter(i -> i.getCreatedAt() != null && !i.getCreatedAt().isBefore(startOfMonth))
                .count();

        int activeProjects = (int) projects.stream().filter(p -> p.getStatus() == ProjectStatus.IN_PROGRESS).count();
        int completedProjects = (int) projects.stream().filter(p -> p.getStatus() == ProjectStatus.COMPLETED).count();
        int totalProjects = projects.size(); // Mantemos o total geral para fins de volume de projetos gerados

        // ==========================================
        // Projetos válidos
        // Filtramos projetos CANCELADOS para que não entrem na conta financeira executiva
        // ==========================================
        List<Project> validProjects = projects.stream()
                .filter(p -> p.getStatus() != ProjectStatus.CANCELLED)
                .toList();

        double totalInvestment = validProjects.stream().mapToDouble(p -> MoneyUtils.parse(p.getInvestment())).sum();
        double financialReturn = validProjects.stream().mapToDouble(p -> MoneyUtils.parse(p.getActualReturn())).sum();

        // ROI calculado sobre a carteira VÁLIDA (Projetos em andamento, pausados ou concluídos)
        double roi = totalInvestment > 0 ? ((financialReturn - totalInvestment) / totalInvestment) * 100.0 : 0.0;

        double economyGenerated = Math.max(0.0, financialReturn - totalInvestment);

        List<Double> productivityValues = validProjects.stream()
                .map(Project::getProductivity)
                .filter(v -> v != null && !v.isBlank())
                .map(MoneyUtils::parsePercentage)
                .toList();

        double avgProductivity = productivityValues.isEmpty() ? 0.0
                : productivityValues.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

        int totalCollaborators = collaborators.size();
        int engagedCollaborators = (int) collaborators.stream()
                .filter(u -> !ideaRepository.findByAuthorId(u.getId()).isEmpty())
                .count();
        double engagementRate = totalCollaborators > 0 ? (engagedCollaborators * 100.0 / totalCollaborators) : 0.0;

        String topGuideline = computeTopGuideline(ideas);

        return new DashboardDto(
                totalIdeas,
                approvedIdeas,
                pendingIdeas,
                ideasThisMonth,
                activeProjects,
                completedProjects,
                totalProjects,
                MoneyUtils.format(totalInvestment),
                MoneyUtils.format(financialReturn),
                round1(roi),
                MoneyUtils.format(economyGenerated),
                Math.round(avgProductivity) + "%",
                engagedCollaborators,
                totalCollaborators,
                round1(engagementRate),
                topGuideline
        );
    }

    private String computeTopGuideline(List<Idea> ideas) {
        Map<String, Long> countByGuideline = ideas.stream()
                .filter(i -> i.getGuidelineId() != null && !i.getGuidelineId().isBlank())
                .collect(Collectors.groupingBy(Idea::getGuidelineId, Collectors.counting()));

        return countByGuideline.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> guidelineRepository.findById(entry.getKey())
                        .map(StrategicGuideline::getCategory)
                        .orElse("N/A"))
                .orElseGet(() -> guidelineRepository.findByStatus(GuidelineStatus.ACTIVE).stream()
                        .findFirst()
                        .map(StrategicGuideline::getTitle)
                        .orElse("N/A"));
    }

    private double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}