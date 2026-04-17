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
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

  private final CategoryRepository categoryRepository;

  @Transactional
  public CategoryResponseDto create(CategoryRequestDto dto) {
    try {
      Category category = CategoryMapper.toEntity(dto);
      Category saved = categoryRepository.save(category);
      log.info("CategoryService.create completed");
      return CategoryMapper.toDto(saved);
    } catch (RuntimeException exception) {
      throw new OperationFailedException("Failed to create category", exception);
    }
  }

  @Transactional
  public CategoryResponseDto update(Long id, CategoryRequestDto dto) {
    Category category = categoryRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Category not found"));

    try {
      category.setTitle(dto.title());
      Category saved = categoryRepository.save(category);
      log.info("CategoryService.update completed");
      return CategoryMapper.toDto(saved);
    } catch (RuntimeException exception) {
      throw new OperationFailedException("Failed to update category", exception);
    }
  }

  @Transactional
  public void delete(Long id) {
    try {
      categoryRepository.deleteById(id);
      log.info("CategoryService.delete completed");
    } catch (EmptyResultDataAccessException _) {
      throw new NotFoundException("Category not found");
    } catch (RuntimeException exception) {
      throw new OperationFailedException("Failed to delete category", exception);
    }
  }

  public CategoryResponseDto getById(Long id) {
    Category category = categoryRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Category not found"));
    log.info("CategoryService.getById completed");
    return CategoryMapper.toDto(category);
  }

  public List<CategoryResponseDto> getAll() {
    List<Category> categories = categoryRepository.findAll();
    List<CategoryResponseDto> result = new ArrayList<>();
    for (Category category : categories) {
      result.add(CategoryMapper.toDto(category));
    }
    log.info("CategoryService.getAll completed");
    return result;
  }

}
