package dev.notebook.notebook.controller;

import dev.notebook.notebook.dto.ReminderRequestDto;
import dev.notebook.notebook.dto.ReminderResponseDto;
import dev.notebook.notebook.service.ReminderService;
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
@RequestMapping("/api/reminders")
@RequiredArgsConstructor
@Validated
@Tag(name = "Reminders", description = "Operations for managing reminders")
public class ReminderController {

  private final ReminderService reminderService;

  @GetMapping
  @Operation(
      summary = "Get all reminders",
      description = "Returns the full list of reminders"
  )
  public List<ReminderResponseDto> getAll() {
    return reminderService.getAll();
  }

  @GetMapping("/{id}")
  @Operation(
      summary = "Get reminder by id",
      description = "Returns a single reminder by identifier"
  )
  public ReminderResponseDto getById(
      @Parameter(description = "Reminder identifier")
      @PathVariable @Positive(message = "Id must be positive") Long id
  ) {
    return reminderService.getById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ReminderResponseDto create(@Valid @RequestBody ReminderRequestDto dto) {
    return reminderService.create(dto);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update reminder", description = "Updates an existing reminder")
  public ReminderResponseDto update(
      @Parameter(description = "Reminder identifier")
      @PathVariable @Positive(message = "Id must be positive") Long id,
      @Valid @RequestBody ReminderRequestDto dto
  ) {
    return reminderService.update(id, dto);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete reminder", description = "Deletes a reminder by identifier")
  public void delete(@PathVariable @Positive(message = "Id must be positive") Long id) {
    reminderService.delete(id);
  }
}
