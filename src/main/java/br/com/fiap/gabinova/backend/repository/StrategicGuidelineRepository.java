package br.com.fiap.gabinova.backend.repository;

import br.com.fiap.gabinova.backend.domain.GuidelineStatus;
import br.com.fiap.gabinova.backend.domain.StrategicGuideline;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface StrategicGuidelineRepository extends MongoRepository<StrategicGuideline, String> {
    List<StrategicGuideline> findByStatus(GuidelineStatus status);
}
