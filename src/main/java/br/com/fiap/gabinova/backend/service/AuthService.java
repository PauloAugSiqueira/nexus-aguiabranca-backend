package br.com.fiap.gabinova.backend.service;

import br.com.fiap.gabinova.backend.domain.User;
import br.com.fiap.gabinova.backend.dto.request.LoginRequest;
import br.com.fiap.gabinova.backend.dto.response.LoginResponse;
import br.com.fiap.gabinova.backend.repository.UserRepository;
import br.com.fiap.gabinova.backend.security.JwtTokenProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuditService auditService;

    public AuthService(UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        JwtTokenProvider jwtTokenProvider,
                        AuditService auditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.auditService = auditService;
    }

    public LoginResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("E-mail ou senha invalidos."));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("E-mail ou senha invalidos.");
        }

        String token = jwtTokenProvider.generateToken(user);
        auditService.record(user.getId(), user.getRole().name(), "LOGIN", "auth", user.getId(), "SUCCESS");

        return new LoginResponse(
                token,
                user.getId(),
                user.getName(),
                user.getRole().name(),
                user.getEmail(),
                jwtTokenProvider.getExpirationInstant().toString()
        );
    }
}
