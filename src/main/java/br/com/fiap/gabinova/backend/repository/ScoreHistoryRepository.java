package br.com.fiap.gabinova.backend.repository;

import br.com.fiap.gabinova.backend.domain.ScoreHistoryEntry;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ScoreHistoryRepository extends MongoRepository<ScoreHistoryEntry, String> {
    List<ScoreHistoryEntry> findByUserIdOrderByDateDesc(String userId);
}
