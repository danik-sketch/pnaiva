package dev.notebook.notebook.controller;

import dev.notebook.notebook.dto.CategoryRequestDto;
import dev.notebook.notebook.dto.CategoryResponseDto;
import dev.notebook.notebook.service.CategoryService;
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
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Validated
@Tag(name = "Categories", description = "Operations for managing categories")
public class CategoryController {

  private final CategoryService categoryService;

  @GetMapping
  @Operation(
      summary = "Get all categories",
      description = "Returns the full list of categories"
  )
  public List<CategoryResponseDto> getAll() {
    return categoryService.getAll();
  }

  @GetMapping("/{id}")
  @Operation(
      summary = "Get category by id",
      description = "Returns a single category by identifier"
  )
  public CategoryResponseDto getById(
      @Parameter(description = "Category identifier")
      @PathVariable @Positive(message = "Id must be positive") Long id
  ) {
    return categoryService.getById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create category", description = "Creates a new category")
  public CategoryResponseDto create(@Valid @RequestBody CategoryRequestDto dto) {
    return categoryService.create(dto);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update category", description = "Updates an existing category")
  public CategoryResponseDto update(
      @Parameter(description = "Category identifier")
      @PathVariable @Positive(message = "Id must be positive") Long id,
      @Valid @RequestBody CategoryRequestDto dto
  ) {
    return categoryService.update(id, dto);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete category", description = "Deletes a category by identifier")
  public void delete(@PathVariable @Positive(message = "Id must be positive") Long id) {
    categoryService.delete(id);
  }
}
