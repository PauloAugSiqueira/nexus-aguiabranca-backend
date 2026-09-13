package br.com.fiap.gabinova.backend.controller;

import br.com.fiap.gabinova.backend.domain.ScoreEventType;
import br.com.fiap.gabinova.backend.domain.UserRole;
import br.com.fiap.gabinova.backend.dto.request.AddPointsRequest;
import br.com.fiap.gabinova.backend.dto.response.GamificationDto;
import br.com.fiap.gabinova.backend.dto.response.RankingDto;
import br.com.fiap.gabinova.backend.exception.BadRequestException;
import br.com.fiap.gabinova.backend.exception.ForbiddenOperationException;
import br.com.fiap.gabinova.backend.security.AuthenticatedUser;
import br.com.fiap.gabinova.backend.security.CurrentUserProvider;
import br.com.fiap.gabinova.backend.service.GamificationService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class GamificationController {

    private final GamificationService gamificationService;
    private final CurrentUserProvider currentUserProvider;

    public GamificationController(GamificationService gamificationService, CurrentUserProvider currentUserProvider) {
        this.gamificationService = gamificationService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/gamification/{userId}")
    public GamificationDto getProfile(@PathVariable String userId) {
        AuthenticatedUser requester = currentUserProvider.get();
        boolean isOwner = requester.userId().equals(userId);
        boolean isManagerOrAdmin = requester.role() == UserRole.MANAGER || requester.role() == UserRole.ADMIN;

        if (!isOwner && !isManagerOrAdmin) {
            throw new ForbiddenOperationException("Voce nao pode consultar a gamificacao de outro usuario.");
        }

        return gamificationService.getProfile(userId);
    }

    @GetMapping("/ranking")
    public List<RankingDto> getRanking() {
        return gamificationService.getRanking();
    }

    @PostMapping("/gamification/points")
    @PreAuthorize("hasRole('ADMIN')")
    public GamificationDto addPoints(@Valid @RequestBody AddPointsRequest request) {
        ScoreEventType type = parseEventType(request.eventType());
        return gamificationService.awardManual(request.userId(), request.points(), type, request.description());
    }

    private ScoreEventType parseEventType(String value) {
        try {
            return ScoreEventType.valueOf(value);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BadRequestException("eventType invalido: " + value);
        }
    }
}
