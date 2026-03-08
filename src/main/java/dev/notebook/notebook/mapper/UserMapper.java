package dev.notebook.notebook.mapper;

import dev.notebook.notebook.dto.UserResponseDto;
import dev.notebook.notebook.entity.Project;
import dev.notebook.notebook.entity.User;
import java.util.ArrayList;
import java.util.List;
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
      List<String> projectNames = new ArrayList<>();
      for (Project project : user.getProjects()) {
        projectNames.add(project.getName());
      }
      dto.setProjects(projectNames);
    }
    return dto;
  }
}

