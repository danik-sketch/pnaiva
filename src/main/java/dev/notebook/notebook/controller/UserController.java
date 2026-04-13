package dev.notebook.notebook.controller;

import dev.notebook.notebook.dto.UserRequestDto;
import dev.notebook.notebook.dto.UserResponseDto;
import dev.notebook.notebook.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import dev.notebook.notebook.service.UserService;
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
  @Operation(summary = "Get all users", description = "Returns the full list of users")
  @ApiResponse(responseCode = "200", description = "Users retrieved successfully",
      content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserResponseDto.class))))
  public List<UserResponseDto> getAll() {
    return userService.getAll();
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get user by id", description = "Returns a single user by identifier")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "User retrieved successfully",
          content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
      @ApiResponse(responseCode = "400", description = "Invalid identifier",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "404", description = "User not found",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  public UserResponseDto getById(
      @Parameter(description = "User identifier")
      @PathVariable @Positive(message = "Id must be positive") Long id
  ) {
    return userService.getById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create user", description = "Creates a new user")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "User created successfully",
          content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
      @ApiResponse(responseCode = "400", description = "Validation failed",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "409", description = "Email already exists",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  public UserResponseDto create(@Valid @RequestBody UserRequestDto dto) {
    return userService.create(dto);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update user", description = "Updates an existing user")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "User updated successfully",
          content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
      @ApiResponse(responseCode = "400", description = "Validation failed",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "404", description = "User not found",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "409", description = "Email already exists",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
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
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "User deleted successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid identifier",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "404", description = "User not found",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  public void delete(@PathVariable @Positive(message = "Id must be positive") Long id) {
    userService.delete(id);
  }
}
