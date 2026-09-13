package br.com.fiap.gabinova.backend.controller;

import br.com.fiap.gabinova.backend.dto.response.UserDto;
import br.com.fiap.gabinova.backend.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public List<UserDto> list() {
        return userService.listAll();
    }

    @GetMapping("/{id}")
    public UserDto getById(@PathVariable String id) {
        return userService.getById(id);
    }
}
