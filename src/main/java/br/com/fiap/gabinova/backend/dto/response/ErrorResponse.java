package br.com.fiap.gabinova.backend.dto.response;

/** Formato minimo de erro esperado pelo app (ApiResult.safeApiCall le errorBody().string()). */
public record ErrorResponse(String message) {
}
