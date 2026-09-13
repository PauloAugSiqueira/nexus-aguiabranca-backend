package br.com.fiap.gabinova.backend.config;

import br.com.fiap.gabinova.backend.domain.GamificationProfile;
import br.com.fiap.gabinova.backend.domain.User;
import br.com.fiap.gabinova.backend.domain.UserRole;
import br.com.fiap.gabinova.backend.repository.GamificationProfileRepository;
import br.com.fiap.gabinova.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Popula os 3 usuarios de teste do README do app Android na primeira subida do backend.
 * Desativado no profile "test" para nao exigir uma conexao real com o MongoDB durante os testes automatizados
 * (ver SPEC_TECNICA_BACKEND.md e o README do backend).
 */
@Component
@Profile("!test")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final GamificationProfileRepository gamificationProfileRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                       GamificationProfileRepository gamificationProfileRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.gamificationProfileRepository = gamificationProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seed("Operador", "operador@gab.com", "123456", UserRole.COLLABORATOR, "Operações");
        seed("Gestor", "gestor@gab.com", "123456", UserRole.MANAGER, "Operações");
        seed("Lideranca", "lideranca@gab.com", "123456", UserRole.ADMIN, "Diretoria");
    }

    private void seed(String name, String email, String rawPassword, UserRole role, String department) {
        if (userRepository.existsByEmail(email)) {
            return;
        }

        User user = new User(name, email, passwordEncoder.encode(rawPassword), role, department);
        User saved = userRepository.save(user);

        gamificationProfileRepository.save(new GamificationProfile(saved.getId()));

        log.info("Usuario de teste criado: {} ({})", email, role);
    }
}
