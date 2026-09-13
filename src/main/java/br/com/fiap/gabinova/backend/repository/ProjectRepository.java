package br.com.fiap.gabinova.backend.repository;

import br.com.fiap.gabinova.backend.domain.Project;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends MongoRepository<Project, String> {
    Optional<Project> findByIdeaId(String ideaId);
    List<Project> findByGuidelineId(String guidelineId);
}
