package br.com.fiap.gabinova.backend.service;

import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Map;

/**
 * Calcula o "Nexus Score" (0..100) de uma ideia.
 * Replica EXATAMENTE calculateNexusScore() de IdeasViewModel.kt (app Android), pois a UI ja
 * exibe rotulos de prioridade calculados sobre este numero ("Alta Prioridade Estrategica" >= 80,
 * "Prioridade Moderada" >= 50) - ver SPEC_FUNCIONAL_BACKEND.md, secao 6.2.1.
 * Isolado em sua propria classe para permitir plugar uma pontuacao via IA no futuro (Plus)
 * sem reescrever o IdeaService - ver SPEC_TECNICA_BACKEND.md, secao 8.
 */
@Service
public class ScoringService {

    private static final Map<String, Integer> CATEGORY_SCORES = Map.of(
            "Redução de Custo", 25,
            "Melhoria de Processo", 22,
            "Inovação Tecnológica", 20,
            "Segurança", 20,
            "Experiência do Cliente", 18,
            "Engajamento", 15
    );

    private static final Map<String, Integer> SECTOR_SCORES = Map.ofEntries(
            Map.entry("Operações", 20),
            Map.entry("Logística", 18),
            Map.entry("Tecnologia", 16),
            Map.entry("Atendimento", 15),
            Map.entry("Financeiro", 14),
            Map.entry("RH", 12),
            Map.entry("Comercial", 12),
            Map.entry("Jurídico", 10)
    );

    private static final int DEFAULT_CATEGORY_SCORE = 10;
    private static final int DEFAULT_SECTOR_SCORE = 8;

    public int calculateNexusScore(String category, String sector, String expectedImpact, String description, int urgency) {
        int urgencyScore = urgency * 10;
        int categoryScore = category == null ? DEFAULT_CATEGORY_SCORE : CATEGORY_SCORES.getOrDefault(category, DEFAULT_CATEGORY_SCORE);
        int sectorScore = sector == null ? DEFAULT_SECTOR_SCORE : SECTOR_SCORES.getOrDefault(sector, DEFAULT_SECTOR_SCORE);
        int impactScore = calculateImpactScore(expectedImpact);
        int descriptionScore = calculateDescriptionScore(description);

        int total = urgencyScore + categoryScore + sectorScore + impactScore + descriptionScore;
        return Math.max(0, Math.min(100, total));
    }

    private int calculateImpactScore(String expectedImpact) {
        if (expectedImpact == null || expectedImpact.isBlank()) {
            return 0;
        }

        String lower = expectedImpact.toLowerCase(Locale.forLanguageTag("pt-BR"));

        if (lower.contains("redução") || lower.contains("reducao")
                || lower.contains("economia")
                || lower.contains("produtividade")) {
            return 15;
        }
        if (lower.contains("agilidade")) {
            return 12;
        }
        if (lower.contains("participação") || lower.contains("participacao")) {
            return 10;
        }
        return 8;
    }

    private int calculateDescriptionScore(String description) {
        if (description == null || description.isBlank()) {
            return 0;
        }
        int length = description.length();
        if (length >= 80) {
            return 10;
        }
        if (length >= 40) {
            return 6;
        }
        return 3;
    }
}
