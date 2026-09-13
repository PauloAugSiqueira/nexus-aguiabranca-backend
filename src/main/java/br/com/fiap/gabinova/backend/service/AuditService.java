package br.com.fiap.gabinova.backend.service;

import br.com.fiap.gabinova.backend.domain.AuditLog;
import br.com.fiap.gabinova.backend.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Grava toda operacao de escrita relevante (quem, quando, o que, resultado) - requisito de
 * governanca/observabilidade do enunciado (SPEC_FUNCIONAL_BACKEND.md, secao 8).
 */
@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void record(String userId, String role, String action, String resource, String resourceId, String result) {
        auditLogRepository.save(new AuditLog(userId, role, action, resource, resourceId, result));
        log.info("AUDIT user={} role={} action={} resource={} resourceId={} result={}",
                userId, role, action, resource, resourceId, result);
    }
}
