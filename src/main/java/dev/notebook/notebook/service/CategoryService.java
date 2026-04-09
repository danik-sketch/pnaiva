package dev.notebook.notebook.service;

import dev.notebook.notebook.dto.CategoryRequestDto;
import dev.notebook.notebook.dto.CategoryResponseDto;
import dev.notebook.notebook.entity.Category;
import dev.notebook.notebook.exception.NotFoundException;
import dev.notebook.notebook.mapper.CategoryMapper;
import dev.notebook.notebook.repository.CategoryRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

  private final CategoryRepository categoryRepository;

  @Transactional
  public CategoryResponseDto create(CategoryRequestDto dto) {
    Category category = new Category();
    category.setTitle(dto.title());
    Category saved = categoryRepository.save(category);
    return CategoryMapper.toDto(saved);
  }

  @Transactional
  public CategoryResponseDto update(Long id, CategoryRequestDto dto) {
    Category category = categoryRepository.findById(id).orElseThrow(()
        -> new NotFoundException("Category not found"));

    category.setTitle(dto.title());
    Category saved = categoryRepository.save(category);
    return CategoryMapper.toDto(saved);
  }

  @Transactional
  public void delete(Long id) {
    categoryRepository.deleteById(id);
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
