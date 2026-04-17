package dev.notebook.notebook.controller;

import dev.notebook.notebook.dto.ErrorResponseDto;
import dev.notebook.notebook.dto.ProjectRequestDto;
import dev.notebook.notebook.dto.ProjectResponseDto;
import dev.notebook.notebook.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
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
@Validated
@Tag(name = "Projects", description = "Operations for managing projects and project search")
public class ProjectController {

  private final ProjectService projectService;

  @GetMapping
  @Operation(
      summary = "Get all projects",
      description = "Returns the full list of projects"
  )
  @ApiResponse(responseCode = "200", description = "Projects retrieved successfully",
      content = @Content(
          array = @ArraySchema(schema = @Schema(implementation = ProjectResponseDto.class))
      ))
  public List<ProjectResponseDto> getAll() {
    return projectService.getAll();
  }

  @GetMapping("/search-jpql")
  @Operation(
      summary = "Search projects via JPQL",
      description = "Searches projects by project and task attributes "
          + "using the JPQL implementation"
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Search completed successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid filter or pagination format",
          content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
  })
  public Page<ProjectResponseDto> searchByTaskJpql(
      @Parameter(description = "Filter by project name")
      @RequestParam(required = false) String projectName,
      @Parameter(description = "Filter by task title")
      @RequestParam(required = false) String taskTitle,
      @Parameter(description = "Filter by task completion status")
      @RequestParam(required = false) Boolean completed,
      @Parameter(description = "Lower bound for task due date")
      @RequestParam(required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dueFrom,
      @Parameter(description = "Upper bound for task due date")
      @RequestParam(required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dueTo,
      @ParameterObject Pageable pageable
  ) {
    return projectService.searchByTaskJpql(
        projectName, taskTitle, completed, dueFrom, dueTo, pageable
    );
  }

  @GetMapping("/search-native")
  @Operation(
      summary = "Search projects via native SQL",
      description = "Searches projects by project and task attributes "
          + "using the native SQL implementation"
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Search completed successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid filter or pagination format",
          content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
  })
  public Page<ProjectResponseDto> searchByTaskNative(
      @Parameter(description = "Filter by project name")
      @RequestParam(required = false) String projectName,
      @Parameter(description = "Filter by task title")
      @RequestParam(required = false) String taskTitle,
      @Parameter(description = "Filter by task completion status")
      @RequestParam(required = false) Boolean completed,
      @Parameter(description = "Lower bound for task due date")
      @RequestParam(required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dueFrom,
      @Parameter(description = "Upper bound for task due date")
      @RequestParam(required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dueTo,
      @ParameterObject Pageable pageable
  ) {
    return projectService.searchByTaskNative(
        projectName, taskTitle, completed, dueFrom, dueTo, pageable
    );
  }

  @GetMapping("/{id}")
  @Operation(
      summary = "Get project by id",
      description = "Returns a single project by identifier"
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Project retrieved successfully",
          content = @Content(schema = @Schema(implementation = ProjectResponseDto.class))),
      @ApiResponse(responseCode = "400", description = "Invalid identifier",
          content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
      @ApiResponse(responseCode = "404", description = "Project not found",
          content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
  })
  public ProjectResponseDto getById(
      @Parameter(description = "Project identifier")
      @PathVariable @Positive(message = "Id must be positive") Long id
  ) {
    return projectService.getById(id);
  }

  @PostMapping("/bulk")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create projects in bulk", description = "Creates multiple new projects " +
      "in a single request")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Projects created successfully",
          content = @Content(array = @ArraySchema(schema = @Schema(implementation =
              ProjectResponseDto.class)))),
      @ApiResponse(responseCode = "400", description = "Validation failed for one or more " +
          "projects",
          content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
      @ApiResponse(responseCode = "404", description = "Owner not found",
          content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
  })
  public List<ProjectResponseDto> createBulk(@Valid @RequestBody List<ProjectRequestDto> dtos) {
    return projectService.createBulk(dtos);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create project", description = "Creates a new project")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Project created successfully",
          content = @Content(schema = @Schema(implementation = ProjectResponseDto.class))),
      @ApiResponse(responseCode = "400", description = "Validation failed",
          content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
      @ApiResponse(responseCode = "404", description = "Owner or related entity not found",
          content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
  })
  public ProjectResponseDto create(@Valid @RequestBody ProjectRequestDto dto) {
    return projectService.create(dto);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update project", description = "Updates an existing project")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Project updated successfully",
          content = @Content(schema = @Schema(implementation = ProjectResponseDto.class))),
      @ApiResponse(responseCode = "400", description = "Validation failed",
          content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
      @ApiResponse(responseCode = "404", description = "Project or related entity not found",
          content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
  })
  public ProjectResponseDto update(
      @Parameter(description = "Project identifier")
      @PathVariable @Positive(message = "Id must be positive") Long id,
      @Valid @RequestBody ProjectRequestDto dto
  ) {
    return projectService.update(id, dto);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete project", description = "Deletes a project by identifier")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "Project deleted successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid identifier",
          content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
      @ApiResponse(responseCode = "404", description = "Project not found",
          content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
  })
  public void delete(@PathVariable @Positive(message = "Id must be positive") Long id) {
    projectService.delete(id);
  }
}
