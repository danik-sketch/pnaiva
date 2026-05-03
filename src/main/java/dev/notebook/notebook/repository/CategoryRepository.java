package dev.notebook.notebook.repository;

import dev.notebook.notebook.entity.Category;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

  @Override
  @EntityGraph(attributePaths = {"tasks"})
  List<Category> findAll();
}