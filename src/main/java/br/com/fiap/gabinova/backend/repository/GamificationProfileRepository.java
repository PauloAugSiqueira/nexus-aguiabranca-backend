package br.com.fiap.gabinova.backend.repository;

import br.com.fiap.gabinova.backend.domain.GamificationProfile;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GamificationProfileRepository extends MongoRepository<GamificationProfile, String> {
}
