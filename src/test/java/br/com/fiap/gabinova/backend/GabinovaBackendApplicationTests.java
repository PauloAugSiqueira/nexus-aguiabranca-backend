package br.com.fiap.gabinova.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Prova que TODA a fiacao do Spring (controllers, services, security, JWT filter,
 * exception handler, repositories) sobe corretamente - sem exigir um MongoDB real
 * (o driver Mongo e lazy: so conecta na primeira operacao real, e o DataSeeder fica
 * desativado no profile "test"). A integracao com um MongoDB de verdade fica a cargo
 * de quem for rodar o backend fora dos testes (ver README.md do backend).
 */
@SpringBootTest
@ActiveProfiles("test")
class GabinovaBackendApplicationTests {

    @Test
    void contextLoads() {
    }
}
