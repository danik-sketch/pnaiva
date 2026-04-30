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
import dev.notebook.notebook.service.cache.SearchKey;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

  private final ProjectRepository projectRepository;
  private final UserRepository userRepository;
  private final Map<SearchKey, Page<ProjectResponseDto>> searchCache = new HashMap<>();

  @Transactional
  public ProjectResponseDto create(ProjectRequestDto dto) {
    Optional<User> userOpt = userRepository.findById(dto.userId());
    User user = userOpt.orElseThrow(() -> new NotFoundException("User not found"));

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
            .orElseThrow(() -> new NotFoundException("User not found"));
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

  @Transactional(readOnly = true)
  public ProjectResponseDto getById(Long id) {
    Project project = projectRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Project not found"));
    log.info("ProjectService.getById completed");
    return ProjectMapper.toDto(project);
  }

  @Transactional(readOnly = true)
  public List<ProjectResponseDto> getAll() {
    Long currentUserId = getCurrentUserId();

    List<Project> projects;
    if (currentUserId != null) {
      projects = projectRepository.findAllByUserId(currentUserId);
      log.info("ProjectService.getAll - returning projects for userId: {}", currentUserId);
    } else {
      projects = projectRepository.findAll();
      log.info("ProjectService.getAll - returning all projects (no auth)");
    }

    log.info("ProjectService.getAll completed - {} projects", projects.size());
    return projects.stream().map(ProjectMapper::toDto).toList();
  }

  @Transactional(readOnly = true)
  public Page<ProjectResponseDto> searchByTaskJpql(
      String projectName, String taskTitle, Boolean completed, LocalDateTime dueFrom,
      LocalDateTime dueTo, Pageable pageable
  ) {
    Long currentUserId = getCurrentUserId();
    SearchKey key = new SearchKey(currentUserId, projectName, taskTitle, completed, dueFrom, dueTo,
        pageable.getPageNumber(), pageable.getPageSize());

    Page<ProjectResponseDto> cached = searchCache.get(key);
    if (cached != null) {
      return cached;
    }

    Page<ProjectResponseDto> result = projectRepository.searchByTaskJpql(currentUserId,
        projectName, taskTitle, completed, dueFrom, dueTo, pageable).map(ProjectMapper::toDto);

    searchCache.put(key, result);
    log.info("Result cached with key: {}", key);
    return result;
  }

  @Transactional
  public List<ProjectResponseDto> createBulk(List<ProjectRequestDto> dtos) {
    List<ProjectResponseDto> response = dtos.stream().map(this::create).toList();
    log.info("ProjectService.createBulk completed");
    return response;
  }

  private void invalidateSearchCache() {
    log.info("Data changed. Cache cleared.");
    searchCache.clear();
  }

  private Long getCurrentUserId() {
    try {
      Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      if (principal instanceof Long id) {
        return id;
      }
      return Long.valueOf(principal.toString());
    } catch (Exception e) {
      log.warn("Could not get currentUserId from SecurityContext: {}", e.getMessage());
      return null;
    }
  }
}