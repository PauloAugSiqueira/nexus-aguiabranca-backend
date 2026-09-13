package br.com.fiap.gabinova.backend.controller;

import br.com.fiap.gabinova.backend.dto.request.CreateGuidelineRequest;
import br.com.fiap.gabinova.backend.dto.response.GuidelineDto;
import br.com.fiap.gabinova.backend.service.GuidelineService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/guidelines")
public class GuidelineController {

    private final GuidelineService guidelineService;

    public GuidelineController(GuidelineService guidelineService) {
        this.guidelineService = guidelineService;
    }

    @GetMapping
    public List<GuidelineDto> list() {
        return guidelineService.listActive();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public GuidelineDto create(@Valid @RequestBody CreateGuidelineRequest request) {
        return guidelineService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public GuidelineDto update(@PathVariable String id, @Valid @RequestBody CreateGuidelineRequest request) {
        return guidelineService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable String id) {
        guidelineService.delete(id);
    }
}
