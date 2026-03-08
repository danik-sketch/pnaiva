package dev.notebook.notebook.mapper;

import dev.notebook.notebook.dto.CategoryResponseDto;
import dev.notebook.notebook.entity.Category;
import dev.notebook.notebook.entity.Task;
import java.util.ArrayList;
import java.util.List;

public class CategoryMapper {

  public static CategoryResponseDto toDto(Category category) {
    CategoryResponseDto dto = new CategoryResponseDto();
    dto.setId(category.getId());
    dto.setTitle(category.getTitle());
    if (category.getTasks() != null) {
      List<String> taskTitles = new ArrayList<>();
      for (Task task : category.getTasks()) {
        taskTitles.add(task.getTitle());
      }
      dto.setTasks(taskTitles);
    }
    return dto;
  }
}

