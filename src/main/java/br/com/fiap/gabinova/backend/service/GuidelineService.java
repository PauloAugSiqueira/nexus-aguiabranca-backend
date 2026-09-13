package br.com.fiap.gabinova.backend.service;

import br.com.fiap.gabinova.backend.domain.GuidelineStatus;
import br.com.fiap.gabinova.backend.domain.StrategicGuideline;
import br.com.fiap.gabinova.backend.dto.request.CreateGuidelineRequest;
import br.com.fiap.gabinova.backend.dto.response.GuidelineDto;
import br.com.fiap.gabinova.backend.exception.ResourceNotFoundException;
import br.com.fiap.gabinova.backend.mapper.GuidelineMapper;
import br.com.fiap.gabinova.backend.repository.StrategicGuidelineRepository;
import br.com.fiap.gabinova.backend.security.CurrentUserProvider;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class GuidelineService {

    private final StrategicGuidelineRepository repository;
    private final CurrentUserProvider currentUserProvider;
    private final AuditService auditService;

    public GuidelineService(StrategicGuidelineRepository repository,
                             CurrentUserProvider currentUserProvider,
                             AuditService auditService) {
        this.repository = repository;
        this.currentUserProvider = currentUserProvider;
        this.auditService = auditService;
    }

    public List<GuidelineDto> listActive() {
        return repository.findByStatus(GuidelineStatus.ACTIVE).stream()
                .map(GuidelineMapper::toDto)
                .toList();
    }

    public GuidelineDto create(CreateGuidelineRequest request) {
        StrategicGuideline guideline = new StrategicGuideline(
                request.title().trim(),
                request.description() == null ? "" : request.description().trim(),
                request.category(),
                request.priority()
        );

        StrategicGuideline saved = repository.save(guideline);
        audit("CREATE_GUIDELINE", saved.getId());
        return GuidelineMapper.toDto(saved);
    }

    public GuidelineDto update(String id, CreateGuidelineRequest request) {
        StrategicGuideline guideline = findOrThrow(id);

        guideline.setTitle(request.title().trim());
        guideline.setDescription(request.description() == null ? "" : request.description().trim());
        guideline.setCategory(request.category());
        guideline.setPriority(request.priority());
        guideline.setUpdatedAt(Instant.now());

        StrategicGuideline saved = repository.save(guideline);
        audit("UPDATE_GUIDELINE", saved.getId());
        return GuidelineMapper.toDto(saved);
    }

    public void delete(String id) {
        StrategicGuideline guideline = findOrThrow(id);
        guideline.setStatus(GuidelineStatus.ARCHIVED);
        guideline.setUpdatedAt(Instant.now());
        repository.save(guideline);
        audit("DELETE_GUIDELINE", id);
    }

    private StrategicGuideline findOrThrow(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Diretriz nao encontrada: " + id));
    }

    private void audit(String action, String resourceId) {
        var user = currentUserProvider.get();
        auditService.record(user.userId(), user.role().name(), action, "guideline", resourceId, "SUCCESS");
    }
}
