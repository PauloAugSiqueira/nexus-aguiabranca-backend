package br.com.fiap.gabinova.backend.repository;

import br.com.fiap.gabinova.backend.domain.Idea;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

public interface IdeaRepository extends MongoRepository<Idea, String> {
    List<Idea> findByAuthorId(String authorId);
    List<Idea> findByGuidelineId(String guidelineId);
    long countByGuidelineId(String guidelineId);
    long countByCreatedAtAfter(Instant instant);
}
