package br.com.fiap.gabinova.backend.controller;

import br.com.fiap.gabinova.backend.dto.response.DashboardDto;
import br.com.fiap.gabinova.backend.service.DashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public DashboardDto getDashboard() {
        return dashboardService.getDashboard();
    }
}
