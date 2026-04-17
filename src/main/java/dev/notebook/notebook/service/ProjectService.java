package dev.notebook.notebook.service;

import dev.notebook.notebook.dto.ProjectRequestDto;
import dev.notebook.notebook.dto.ProjectResponseDto;
import dev.notebook.notebook.entity.Project;
import dev.notebook.notebook.entity.User;
import dev.notebook.notebook.exception.NotFoundException;
import dev.notebook.notebook.exception.OperationFailedException;
import dev.notebook.notebook.mapper.ProjectMapper;
import dev.notebook.notebook.repository.ProjectRepository;
import dev.notebook.notebook.repository.UserRepository;
import dev.notebook.notebook.service.cache.ProductSearchKey;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

  private static final String USER_NOT_FOUND = "User not found";

  private final ProjectRepository projectRepository;
  private final UserRepository userRepository;
  private final Map<ProductSearchKey, Page<ProjectResponseDto>> searchCache = new HashMap<>();

  @Transactional
  public ProjectResponseDto create(ProjectRequestDto dto) {
    User user = userRepository.findById(dto.userId())
        .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));

    try {
      Project project = ProjectMapper.toEntity(dto, user);

      Project saved = projectRepository.save(project);
      invalidateSearchCache();
      log.info("ProjectService.create completed");
      return ProjectMapper.toDto(saved);
    } catch (RuntimeException exception) {
      throw new OperationFailedException("Failed to create project", exception);
    }
  }

  @Transactional
  public ProjectResponseDto update(Long id, ProjectRequestDto dto) {
    Project project = projectRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Project not found"));

    try {
      project.setName(dto.name());
      project.setDescription(dto.description());

      if (!project.getUser().getId().equals(dto.userId())) {
        User user = userRepository.findById(dto.userId())
            .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));
        project.setUser(user);
      }

      Project saved = projectRepository.save(project);
      invalidateSearchCache();
      log.info("ProjectService.update completed");
      return ProjectMapper.toDto(saved);
    } catch (RuntimeException exception) {
      throw new OperationFailedException("Failed to update project", exception);
    }
  }

  @Transactional
  public void delete(Long id) {
    try {
      projectRepository.deleteById(id);
      invalidateSearchCache();
      log.info("ProjectService.delete completed");
    } catch (EmptyResultDataAccessException _) {
      throw new NotFoundException("Project not found");
    } catch (RuntimeException exception) {
      throw new OperationFailedException("Failed to delete project", exception);
    }
  }

  public ProjectResponseDto getById(Long id) {
    Project project = projectRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Project not found"));
    log.info("ProjectService.getById completed");
    return ProjectMapper.toDto(project);
  }

  public List<ProjectResponseDto> getAll() {
    List<Project> projects = projectRepository.findAll();
    List<ProjectResponseDto> result = new ArrayList<>();
    for (Project project : projects) {
      result.add(ProjectMapper.toDto(project));
    }
    log.info("ProjectService.getAll completed");
    return result;
  }

  @Transactional(readOnly = true)
  public Page<ProjectResponseDto> searchByTaskJpql(
      String projectName, String taskTitle, Boolean completed,
      LocalDateTime dueFrom, LocalDateTime dueTo, Pageable pageable
  ) {
    ProductSearchKey key = new ProductSearchKey(
        projectName, taskTitle, completed, dueFrom, dueTo,
        pageable.getPageNumber(), pageable.getPageSize());

    Page<ProjectResponseDto> cached = searchCache.get(key);
    if (cached != null) {
      return cached;
    }

    Page<ProjectResponseDto> result = projectRepository.searchByTaskJpql(
        projectName, taskTitle, completed, dueFrom, dueTo, pageable).map(ProjectMapper::toDto);
    searchCache.put(key, result);

    log.info("\n\nResult cached with key: {}", key);
    log.info("Cache size after save: {}\n\n", searchCache.size());

    return result;
  }

  @Transactional(readOnly = true)
  public Page<ProjectResponseDto> searchByTaskNative(
      String projectName, String taskTitle, Boolean completed,
      LocalDateTime dueFrom, LocalDateTime dueTo, Pageable pageable
  ) {
    ProductSearchKey key = new ProductSearchKey(
        projectName, taskTitle, completed, dueFrom, dueTo,
        pageable.getPageNumber(), pageable.getPageSize());
    Page<ProjectResponseDto> cached = searchCache.get(key);
    if (cached != null) {
      return cached;
    }

    Page<ProjectResponseDto> result = projectRepository.searchByTaskNative(
        projectName, taskTitle, completed, dueFrom, dueTo, pageable).map(ProjectMapper::toDto);
    searchCache.put(key, result);

    log.info("\n\nResult cached with key: {}", key);
    log.info("Cache size after save: {}\n\n", searchCache.size());

    return result;
  }

  private void invalidateSearchCache() {
    log.info("Data changed. Cache cleared.");
    searchCache.clear();
  }

  @Transactional
  public List<ProjectResponseDto> createBulk(List<ProjectRequestDto> dtoList) {
    List<ProjectResponseDto> response = dtoList.stream().map(this::create).toList();
    log.info("ProjectService.createBulk completed");
    return response;
  }
}
