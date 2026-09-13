package br.com.fiap.gabinova.backend.repository;

import br.com.fiap.gabinova.backend.domain.AuditLog;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AuditLogRepository extends MongoRepository<AuditLog, String> {
}
