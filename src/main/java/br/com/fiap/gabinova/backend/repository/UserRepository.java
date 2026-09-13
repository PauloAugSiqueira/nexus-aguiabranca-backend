package br.com.fiap.gabinova.backend.repository;

import br.com.fiap.gabinova.backend.domain.User;
import br.com.fiap.gabinova.backend.domain.UserRole;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRole(UserRole role);
}
