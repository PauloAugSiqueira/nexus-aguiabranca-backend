package br.com.fiap.gabinova.backend.mapper;

import br.com.fiap.gabinova.backend.domain.User;
import br.com.fiap.gabinova.backend.dto.response.UserDto;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.getDepartment(),
                user.getPoints(),
                user.getLevel(),
                DateFormat.iso(user.getCreatedAt())
        );
    }
}
