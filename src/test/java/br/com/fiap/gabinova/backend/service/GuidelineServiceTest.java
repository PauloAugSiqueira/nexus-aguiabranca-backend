package br.com.fiap.gabinova.backend.service;

import br.com.fiap.gabinova.backend.domain.GuidelineStatus;
import br.com.fiap.gabinova.backend.domain.StrategicGuideline;
import br.com.fiap.gabinova.backend.domain.UserRole;
import br.com.fiap.gabinova.backend.dto.request.CreateGuidelineRequest;
import br.com.fiap.gabinova.backend.dto.response.GuidelineDto;
import br.com.fiap.gabinova.backend.exception.ResourceNotFoundException;
import br.com.fiap.gabinova.backend.repository.StrategicGuidelineRepository;
import br.com.fiap.gabinova.backend.security.AuthenticatedUser;
import br.com.fiap.gabinova.backend.security.CurrentUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GuidelineServiceTest {

    @Mock private StrategicGuidelineRepository repository;
    @Mock private CurrentUserProvider currentUserProvider;
    @Mock private AuditService auditService;

    private GuidelineService guidelineService;

    @BeforeEach
    void setUp() {
        guidelineService = new GuidelineService(repository, currentUserProvider, auditService);
        lenient().when(currentUserProvider.get()).thenReturn(
                new AuthenticatedUser("adm-1", "lideranca@gab.com", "Liderança", UserRole.ADMIN));
        lenient().when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void listaApenasDiretrizesAtivas() {
        StrategicGuideline active = new StrategicGuideline("Ativa", "desc", "Operações", "1");
        when(repository.findByStatus(GuidelineStatus.ACTIVE)).thenReturn(List.of(active));

        List<GuidelineDto> result = guidelineService.listActive();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("Ativa");
        assertThat(result.get(0).status()).isEqualTo("ACTIVE");
    }

    @Test
    void criarDiretrizDefineStatusAtivoPorPadrao() {
        CreateGuidelineRequest request = new CreateGuidelineRequest("Nova", "desc", "Tecnologia", "2");

        GuidelineDto dto = guidelineService.create(request);

        assertThat(dto.status()).isEqualTo("ACTIVE");
        assertThat(dto.priority()).isEqualTo("2");
    }

    @Test
    void excluirFazSoftDeleteMudandoStatusParaArquivada() {
        StrategicGuideline guideline = new StrategicGuideline("Titulo", "desc", "Pessoas", "3");
        guideline.setId("g1");
        when(repository.findById("g1")).thenReturn(Optional.of(guideline));

        guidelineService.delete("g1");

        assertThat(guideline.getStatus()).isEqualTo(GuidelineStatus.ARCHIVED);
    }

    @Test
    void atualizarDiretrizInexistenteLancaNotFound() {
        when(repository.findById("nao-existe")).thenReturn(Optional.empty());

        CreateGuidelineRequest request = new CreateGuidelineRequest("Titulo", "desc", "Pessoas", "3");

        assertThatThrownBy(() -> guidelineService.update("nao-existe", request))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
