package br.com.fiap.gabinova.backend.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScoringServiceTest {

    private final ScoringService scoringService = new ScoringService();

    @Test
    void calculaScoreMaximoParaIdeiaFortementeAlinhada() {
        String description = "a".repeat(80);

        int score = scoringService.calculateNexusScore(
                "Redução de Custo", "Operações", "Redução de custo operacional", description, 5);

        // 50 (urgencia) + 25 (categoria) + 20 (setor) + 15 (impacto) + 10 (descricao) = 120 -> cap 100
        assertThat(score).isEqualTo(100);
    }

    @Test
    void categoriaESetorDesconhecidosComUrgenciaImpactoEDescricaoVaziosUsamSoOsPisosPadrao() {
        // urgencia 0 + categoria default (10) + setor default (8) + impacto vazio (0) + descricao vazia (0) = 18
        int score = scoringService.calculateNexusScore("Outro", "Outro", "", "", 0);
        assertThat(score).isEqualTo(18);
    }

    @Test
    void categoriaDesconhecidaUsaPesoPadrao() {
        int score = scoringService.calculateNexusScore(
                "Categoria Inexistente", "Setor Inexistente", "", "", 1);

        // 10 (urgencia) + 10 (categoria default) + 8 (setor default) + 0 + 0 = 28
        assertThat(score).isEqualTo(28);
    }

    @Test
    void impactoComPalavraChaveEconomiaDaQuinzePontos() {
        int scoreComEconomia = scoringService.calculateNexusScore(
                "Outro", "Outro", "Gera economia relevante", "", 1);
        int scoreGenericoPreenchido = scoringService.calculateNexusScore(
                "Outro", "Outro", "Texto qualquer sem palavra-chave", "", 1);

        assertThat(scoreComEconomia).isGreaterThan(scoreGenericoPreenchido);
    }

    @Test
    void descricaoLongaPontuaMaisQueDescricaoCurta() {
        int scoreLongo = scoringService.calculateNexusScore("Outro", "Outro", "", "a".repeat(80), 1);
        int scoreMedio = scoringService.calculateNexusScore("Outro", "Outro", "", "a".repeat(40), 1);
        int scoreCurto = scoringService.calculateNexusScore("Outro", "Outro", "", "abc", 1);

        assertThat(scoreLongo).isGreaterThan(scoreMedio);
        assertThat(scoreMedio).isGreaterThan(scoreCurto);
    }

    @Test
    void scoreNuncaUltrapassaCemNemFicaNegativo() {
        int scoreAlto = scoringService.calculateNexusScore(
                "Redução de Custo", "Operações", "Redução de custo e economia com produtividade", "a".repeat(200), 5);
        int scoreBaixo = scoringService.calculateNexusScore(null, null, null, null, 0);

        assertThat(scoreAlto).isLessThanOrEqualTo(100);
        assertThat(scoreBaixo).isGreaterThanOrEqualTo(0);
    }
}
