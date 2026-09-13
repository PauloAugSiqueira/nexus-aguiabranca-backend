package br.com.fiap.gabinova.backend.controller;

import br.com.fiap.gabinova.backend.dto.request.LoginRequest;
import br.com.fiap.gabinova.backend.dto.response.LoginResponse;
import br.com.fiap.gabinova.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
