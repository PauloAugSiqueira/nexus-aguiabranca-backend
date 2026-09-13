package br.com.fiap.gabinova.backend.service;

import br.com.fiap.gabinova.backend.domain.User;
import br.com.fiap.gabinova.backend.dto.response.UserDto;
import br.com.fiap.gabinova.backend.exception.ResourceNotFoundException;
import br.com.fiap.gabinova.backend.mapper.UserMapper;
import br.com.fiap.gabinova.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserDto> listAll() {
        return userRepository.findAll().stream().map(UserMapper::toDto).toList();
    }

    public UserDto getById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado: " + id));
        return UserMapper.toDto(user);
    }
}
