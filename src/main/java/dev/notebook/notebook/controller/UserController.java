package dev.notebook.notebook.controller;

import dev.notebook.notebook.dto.UserRequestDto;
import dev.notebook.notebook.dto.UserResponseDto;
import dev.notebook.notebook.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
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
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
@Tag(name = "Users", description = "Operations for managing users")
public class UserController {

  private final UserService userService;

  @GetMapping
  @Operation(
      summary = "Get all users",
      description = "Returns the full list of users"
  )
  public List<UserResponseDto> getAll() {
    return userService.getAll();
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get user by id", description = "Returns a single user by identifier")
  public UserResponseDto getById(
      @Parameter(description = "User identifier")
      @PathVariable @Positive(message = "Id must be positive") Long id
  ) {
    return userService.getById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create user", description = "Creates a new user")
  public UserResponseDto create(@Valid @RequestBody UserRequestDto dto) {
    return userService.create(dto);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update user", description = "Updates an existing user")
  public UserResponseDto update(
      @Parameter(description = "User identifier")
      @PathVariable @Positive(message = "Id must be positive") Long id,
      @Valid @RequestBody UserRequestDto dto
  ) {
    return userService.update(id, dto);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete user", description = "Deletes a user by identifier")
  public void delete(@PathVariable @Positive(message = "Id must be positive") Long id) {
    userService.delete(id);
  }
}
