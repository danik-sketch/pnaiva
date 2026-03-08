package dev.notebook.notebook.mapper;

import dev.notebook.notebook.dto.ProjectResponseDto;
import dev.notebook.notebook.entity.Project;
import dev.notebook.notebook.entity.Task;
import java.util.ArrayList;
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
      List<String> taskTitles = new ArrayList<>();
      for (Task task : project.getTasks()) {
        taskTitles.add(task.getTitle());
      }
      dto.setTasks(taskTitles);
    }
    return dto;
  }
}

