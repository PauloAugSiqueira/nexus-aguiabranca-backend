package br.com.fiap.gabinova.backend.controller;

import br.com.fiap.gabinova.backend.dto.request.CreateIdeaRequest;
import br.com.fiap.gabinova.backend.dto.request.UpdateIdeaPriorityRequest;
import br.com.fiap.gabinova.backend.dto.request.UpdateIdeaStatusRequest;
import br.com.fiap.gabinova.backend.dto.response.IdeaDto;
import br.com.fiap.gabinova.backend.security.AuthenticatedUser;
import br.com.fiap.gabinova.backend.security.CurrentUserProvider;
import br.com.fiap.gabinova.backend.service.IdeaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ideas")
public class IdeaController {

    private final IdeaService ideaService;
    private final CurrentUserProvider currentUserProvider;

    public IdeaController(IdeaService ideaService, CurrentUserProvider currentUserProvider) {
        this.ideaService = ideaService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('COLLABORATOR','MANAGER')")
    public List<IdeaDto> list() {
        return ideaService.list(currentUserProvider.get());
    }

    @GetMapping("/user/{userId}")
    public List<IdeaDto> listByUser(@PathVariable String userId) {
        return ideaService.listByAuthor(userId, currentUserProvider.get());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('COLLABORATOR')")
    public IdeaDto create(@Valid @RequestBody CreateIdeaRequest request) {
        return ideaService.create(currentUserProvider.get(), request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('COLLABORATOR')")
    public IdeaDto update(@PathVariable String id, @Valid @RequestBody CreateIdeaRequest request) {
        return ideaService.update(id, currentUserProvider.get(), request);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('MANAGER')")
    public IdeaDto updateStatus(@PathVariable String id, @Valid @RequestBody UpdateIdeaStatusRequest request) {
        return ideaService.updateStatus(id, currentUserProvider.get(), request.status());
    }

    @PatchMapping("/{id}/priority")
    @PreAuthorize("hasRole('MANAGER')")
    public IdeaDto prioritize(@PathVariable String id, @RequestBody(required = false) UpdateIdeaPriorityRequest request) {
        return ideaService.prioritize(id, currentUserProvider.get());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIdea(
            @AuthenticationPrincipal AuthenticatedUser requester,
            @PathVariable String id) {
        ideaService.delete(id, requester);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/implement")
    @PreAuthorize("hasRole('MANAGER')")
    public IdeaDto startImplementation(@PathVariable String id, @AuthenticationPrincipal AuthenticatedUser requester) {
        return ideaService.startImplementation(id, requester);
    }
}
