package dev.notebook.notebook.controller;

import dev.notebook.notebook.dto.ReminderRequestDto;
import dev.notebook.notebook.dto.ReminderResponseDto;
import dev.notebook.notebook.service.ReminderService;
import jakarta.validation.Valid;
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
@RequestMapping("/api/reminders")
@RequiredArgsConstructor
public class ReminderController {

  private final ReminderService reminderService;

  @GetMapping
  public List<ReminderResponseDto> getAll() {
    return reminderService.getAll();
  }

  @GetMapping("/{id}")
  public ReminderResponseDto getById(@PathVariable Long id) {
    return reminderService.getById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ReminderResponseDto create(@Valid @RequestBody ReminderRequestDto dto) {
    return reminderService.create(dto);
  }

  @PutMapping("/{id}")
  public ReminderResponseDto update(@PathVariable Long id,
      @Valid @RequestBody ReminderRequestDto dto) {
    return reminderService.update(id, dto);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    reminderService.delete(id);
  }
}
