package dev.notebook.notebook.controller;

import dev.notebook.notebook.dto.ProjectRequestDto;
import dev.notebook.notebook.dto.ProjectResponseDto;
import dev.notebook.notebook.service.ProjectService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

  private final ProjectService projectService;

  @GetMapping
  public List<ProjectResponseDto> getAll() {
    return projectService.getAll();
  }

  @GetMapping("/optimized")
  public List<ProjectResponseDto> getAllOptimized() {
    return projectService.getAllOptimized();
  }

  @GetMapping("/{id}")
  public ProjectResponseDto getById(@PathVariable Long id) {
    return projectService.getById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ProjectResponseDto create(@RequestBody ProjectRequestDto dto) {
    return projectService.create(dto);
  }

  @PostMapping("/without-transactional/{userId}")
  public String createProjectWithTasksNonTransactional(@PathVariable Long userId) {
    try {
      projectService.createProjectWithTasksNonTransactional(userId);
    } catch (IllegalStateException e) {
      return "ОШИБКА: " + e.getMessage()
          + ". Проверь БД: Проект и одна задача уже там.";
    }
    return "Успех";
  }

  @PostMapping("/with-transactional/{userId}")
  public String createProjectWithTasksTransactional(@PathVariable Long userId) {
    try {
      projectService.createProjectWithTasksTransactional(userId);
    } catch (IllegalStateException e) {
      return "ОШИБКА: " + e.getMessage()
          + ". Проверь БД: В базе ПУСТО. Проект и задачи откатились.";
    }
    return "Успех";
  }

  @PutMapping("/{id}")
  public ProjectResponseDto update(@PathVariable Long id, @RequestBody ProjectRequestDto dto) {
    return projectService.update(id, dto);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    projectService.delete(id);
  }
}
