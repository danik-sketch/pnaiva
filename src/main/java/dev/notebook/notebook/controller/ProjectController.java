package dev.notebook.notebook.controller;

import dev.notebook.notebook.dto.ProjectRequestDto;
import dev.notebook.notebook.dto.ProjectResponseDto;
import dev.notebook.notebook.service.ProjectService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

  @GetMapping("/search-jpql")
  public Page<ProjectResponseDto> searchByTaskJpql(
      @RequestParam(required = false) String projectName,
      @RequestParam(required = false) String taskTitle,
      @RequestParam(required = false) Boolean completed,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
      LocalDateTime dueFrom,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
      LocalDateTime dueTo,
      Pageable pageable
  ) {
    return projectService.searchByTaskJpql(projectName, taskTitle, completed, dueFrom, dueTo,
        pageable);
  }

  @GetMapping("/search-native")
  public Page<ProjectResponseDto> searchByTaskNative(
      @RequestParam(required = false) String projectName,
      @RequestParam(required = false) String taskTitle,
      @RequestParam(required = false) Boolean completed,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
      LocalDateTime dueFrom,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
      LocalDateTime dueTo,
      Pageable pageable
  ) {
    return projectService.searchByTaskNative(projectName, taskTitle, completed, dueFrom, dueTo,
        pageable);
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
