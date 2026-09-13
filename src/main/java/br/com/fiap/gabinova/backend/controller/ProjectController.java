package br.com.fiap.gabinova.backend.controller;

import br.com.fiap.gabinova.backend.dto.request.CreateProjectRequest;
import br.com.fiap.gabinova.backend.dto.request.UpdateProjectProgressRequest;
import br.com.fiap.gabinova.backend.dto.request.UpdateProjectRequest;
import br.com.fiap.gabinova.backend.dto.response.ProjectDto;
import br.com.fiap.gabinova.backend.security.CurrentUserProvider;
import br.com.fiap.gabinova.backend.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final CurrentUserProvider currentUserProvider;

    public ProjectController(ProjectService projectService, CurrentUserProvider currentUserProvider) {
        this.projectService = projectService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','ANALYST')")
    public List<ProjectDto> list() {
        return projectService.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('MANAGER')")
    public ProjectDto create(@Valid @RequestBody CreateProjectRequest request) {
        return projectService.create(currentUserProvider.get(), request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN', 'ANALYST')")
    public ProjectDto update(@PathVariable String id, @Valid @RequestBody UpdateProjectRequest request) {
        return projectService.update(id, currentUserProvider.get(), request);
    }

    @PatchMapping("/{id}/progress")
    @PreAuthorize("hasRole('MANAGER')")
    public ProjectDto updateProgress(@PathVariable String id, @Valid @RequestBody UpdateProjectProgressRequest request) {
        return projectService.updateProgress(id, currentUserProvider.get(), request.progress());
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ProjectDto approve(@PathVariable String id) {
        return projectService.approve(id, currentUserProvider.get());
    }
}