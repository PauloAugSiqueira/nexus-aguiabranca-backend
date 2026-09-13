package br.com.fiap.gabinova.backend.domain;

/**
 * Espelha exatamente br.com.fiap.gabinova.model.UserRole do app Android (mesmos literais,
 * pois o Gson do client faz UserRole.valueOf(...) case-sensitive sobre o valor vindo da API).
 * ANALYST existe no enum do app mas nao e alcancado por nenhuma rota real (ver SPEC_FUNCIONAL_BACKEND.md, secao 9.1).
 */
public enum UserRole {
    ADMIN,
    MANAGER,
    ANALYST,
    COLLABORATOR
}
