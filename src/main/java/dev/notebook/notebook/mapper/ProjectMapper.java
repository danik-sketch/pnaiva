package dev.notebook.notebook.mapper;

import dev.notebook.notebook.dto.ProjectResponseDto;
import dev.notebook.notebook.entity.Project;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProjectMapper {

  public static ProjectResponseDto toDto(Project project) {
    ProjectResponseDto dto = new ProjectResponseDto();
    dto.setId(project.getId());
    dto.setName(project.getName());
    dto.setDescription(project.getDescription());
    if (project.getUser() != null) {
      dto.setUsername(project.getUser().getUsername());
    }
    if (project.getTasks() != null) {
      dto.setTasks(project.getTasks().stream()
          .map(TaskMapper::toDto)
          .toList());
    }
    return dto;
  }
}
