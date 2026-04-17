package dev.notebook.notebook.mapper;

import dev.notebook.notebook.dto.UserRequestDto;
import dev.notebook.notebook.dto.UserResponseDto;
import dev.notebook.notebook.entity.Project;
import dev.notebook.notebook.entity.User;
import java.util.Collections;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserMapper {

  public static UserResponseDto toDto(User user) {
    UserResponseDto dto = new UserResponseDto();
    dto.setId(user.getId());
    dto.setUsername(user.getUsername());
    dto.setEmail(user.getEmail());
    if (user.getProjects() != null) {
      dto.setProjects(user.getProjects().stream()
          .map(Project::getName)
          .toList());
    } else {
      dto.setProjects(Collections.emptyList());
    }
    return dto;
  }

  public static User toEntity(UserRequestDto dto) {
    User user = new User();
    user.setUsername(dto.username());
    user.setEmail(dto.email());
    user.setPassword(dto.password());
    return user;
  }
}

