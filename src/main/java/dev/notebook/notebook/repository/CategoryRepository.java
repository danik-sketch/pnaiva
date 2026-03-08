package dev.notebook.notebook.repository;

import dev.notebook.notebook.entity.Category;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

  List<Category> findByTitleContaining(String title);
}

