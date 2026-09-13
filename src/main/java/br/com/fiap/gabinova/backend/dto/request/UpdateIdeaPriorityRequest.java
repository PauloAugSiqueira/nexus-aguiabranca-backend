package br.com.fiap.gabinova.backend.dto.request;

/**
 * Corpo do PATCH /ideas/{id}/priority (acao "Priorizar" do gestor).
 * O campo "priority" e ignorado pelo backend - ver decisao 9.3 do SPEC_FUNCIONAL_BACKEND.md:
 * o endpoint aplica uma transicao de negocio fixa (status -> IN_REVIEW, score += 10),
 * nao a gravacao literal de um campo "priority" (que nao existe em Idea). Mantido no
 * contrato para compatibilidade com o ApiService.kt ja escrito no app.
 */
public record UpdateIdeaPriorityRequest(
        String priority
) {
}
