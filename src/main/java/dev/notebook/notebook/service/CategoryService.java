package dev.notebook.notebook.service;

import dev.notebook.notebook.dto.CategoryRequestDto;
import dev.notebook.notebook.dto.CategoryResponseDto;
import dev.notebook.notebook.entity.Category;
import dev.notebook.notebook.exception.NotFoundException;
import dev.notebook.notebook.exception.OperationFailedException;
import dev.notebook.notebook.mapper.CategoryMapper;
import dev.notebook.notebook.repository.CategoryRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

  private final CategoryRepository categoryRepository;

  @Transactional
  public CategoryResponseDto create(CategoryRequestDto dto) {
    try {
      Category category = new Category();
      category.setTitle(dto.title());
      Category saved = categoryRepository.save(category);
      return CategoryMapper.toDto(saved);
    } catch (RuntimeException exception) {
      throw new OperationFailedException("Failed to create category", exception);
    }
  }

  @Transactional
  public CategoryResponseDto update(Long id, CategoryRequestDto dto) {
    Category category = categoryRepository.findById(id).orElseThrow(()
        -> new NotFoundException("Category not found"));

    try {
      category.setTitle(dto.title());
      Category saved = categoryRepository.save(category);
      return CategoryMapper.toDto(saved);
    } catch (RuntimeException exception) {
      throw new OperationFailedException("Failed to update category", exception);
    }
  }

  @Transactional
  public void delete(Long id) {
    try {
      categoryRepository.deleteById(id);
    } catch (EmptyResultDataAccessException _) {
      throw new NotFoundException("Category not found");
    } catch (RuntimeException exception) {
      throw new OperationFailedException("Failed to delete category", exception);
    }
  }

  public CategoryResponseDto getById(Long id) {
    Category category = categoryRepository.findById(id).orElseThrow(()
        -> new NotFoundException("Category not found"));
    return CategoryMapper.toDto(category);
  }

  public List<CategoryResponseDto> getAll() {
    List<Category> categories = categoryRepository.findAll();
    List<CategoryResponseDto> result = new ArrayList<>();
    for (Category category : categories) {
      result.add(CategoryMapper.toDto(category));
    }
    return result;
  }

}
