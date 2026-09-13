package br.com.fiap.gabinova.backend.dto.response;

public record DashboardDto(
        int totalIdeas,
        int approvedIdeas,
        int pendingIdeas,
        int ideasThisMonth,
        int activeProjects,
        int completedProjects,
        int totalProjects,
        String totalInvestment,
        String financialReturn,
        double roi,
        String economyGenerated,
        String avgProductivity,
        int engagedCollaborators,
        int totalCollaborators,
        double engagementRate,
        String topGuideline
) {
}