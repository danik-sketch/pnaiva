package dev.notebook.notebook.service;

import static dev.notebook.notebook.service.TestFixtures.category;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.notebook.notebook.dto.CategoryRequestDto;
import dev.notebook.notebook.dto.CategoryResponseDto;
import dev.notebook.notebook.entity.Category;
import dev.notebook.notebook.exception.NotFoundException;
import dev.notebook.notebook.exception.OperationFailedException;
import dev.notebook.notebook.repository.CategoryRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.EmptyResultDataAccessException;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

  @Mock
  private CategoryRepository categoryRepository;

  @InjectMocks
  private CategoryService categoryService;

  @Test
  void createShouldMapAndSave() {
    when(categoryRepository.save(any(Category.class))).thenReturn(category(1L, "Work"));

    CategoryResponseDto result = categoryService.create(new CategoryRequestDto("Work"));

    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getTitle()).isEqualTo("Work");
  }

  @Test
  void createShouldWrapRepositoryFailure() {
    when(categoryRepository.save(any(Category.class)))
        .thenThrow(new DataAccessResourceFailureException("db down"));
    CategoryRequestDto requestDto = new CategoryRequestDto("Work");

    assertThatThrownBy(() -> categoryService.create(requestDto))
        .isInstanceOf(OperationFailedException.class)
        .hasMessage("Failed to create category");
  }

  @Test
  void updateShouldThrowWhenNotFound() {
    when(categoryRepository.findById(7L)).thenReturn(Optional.empty());
    CategoryRequestDto requestDto = new CategoryRequestDto("Updated");

    assertThatThrownBy(() -> categoryService.update(7L, requestDto))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("Category not found");
  }

  @Test
  void updateShouldSaveAndReturnDto() {
    Category existing = category(7L, "Old");
    when(categoryRepository.findById(7L)).thenReturn(Optional.of(existing));
    when(categoryRepository.save(existing)).thenReturn(existing);

    CategoryResponseDto result = categoryService.update(7L, new CategoryRequestDto("Updated"));

    assertThat(result.getTitle()).isEqualTo("Updated");
  }

  @Test
  void updateShouldWrapRepositoryFailure() {
    Category existing = category(7L, "Old");
    when(categoryRepository.findById(7L)).thenReturn(Optional.of(existing));
    when(categoryRepository.save(existing)).thenThrow(new DataAccessResourceFailureException("db down"));
    CategoryRequestDto requestDto = new CategoryRequestDto("Updated");

    assertThatThrownBy(() -> categoryService.update(7L, requestDto))
        .isInstanceOf(OperationFailedException.class)
        .hasMessage("Failed to update category");
  }

  @Test
  void deleteShouldMapExceptions() {
    doThrow(new EmptyResultDataAccessException(1)).when(categoryRepository).deleteById(10L);
    assertThatThrownBy(() -> categoryService.delete(10L))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("Category not found");

    doThrow(new DataAccessResourceFailureException("db down")).when(categoryRepository).deleteById(11L);
    assertThatThrownBy(() -> categoryService.delete(11L))
        .isInstanceOf(OperationFailedException.class)
        .hasMessage("Failed to delete category");
  }

  @Test
  void deleteShouldCallRepository() {
    categoryService.delete(12L);
    verify(categoryRepository).deleteById(12L);
  }

  @Test
  void getByIdShouldMapFoundAndNotFound() {
    when(categoryRepository.findById(1L)).thenReturn(Optional.of(category(1L, "Work")));
    assertThat(categoryService.getById(1L).getTitle()).isEqualTo("Work");

    when(categoryRepository.findById(2L)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> categoryService.getById(2L))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("Category not found");
  }

  @Test
  void getAllShouldMapList() {
    when(categoryRepository.findAll()).thenReturn(List.of(category(1L, "Work"), category(2L, "Home")));

    List<CategoryResponseDto> result = categoryService.getAll();

    assertThat(result).extracting(CategoryResponseDto::getTitle).containsExactly("Work", "Home");
    verify(categoryRepository).findAll();
  }
}
