package dev.notebook.notebook.mapper;

import dev.notebook.notebook.dto.ProjectRequestDto;
import dev.notebook.notebook.dto.ProjectResponseDto;
import dev.notebook.notebook.entity.Project;
import dev.notebook.notebook.entity.User;
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

  public static Project toEntity(ProjectRequestDto dto, User user) {
    Project project = new Project();
    project.setName(dto.name());
    project.setDescription(dto.description());
    project.setUser(user);

    if (dto.tasks() != null) {
      dto.tasks().stream()
          .map(taskDto -> TaskMapper.toEntity(taskDto, project))
          .forEach(task -> project.getTasks().add(task));
    }

    return project;
  }
}
