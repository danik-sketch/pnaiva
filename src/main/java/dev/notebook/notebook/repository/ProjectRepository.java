package dev.notebook.notebook.repository;

import dev.notebook.notebook.entity.Project;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {

  @EntityGraph(attributePaths = {"user", "tasks"})
  List<Project> findByNameContaining(String name);

  @EntityGraph(attributePaths = {"user", "tasks"})
  List<Project> findAllBy();

  @Override
  @EntityGraph(attributePaths = {"user", "tasks"})
  List<Project> findAll();

  @Override
  @EntityGraph(attributePaths = {"user", "tasks"})
  Optional<Project> findById(Long id);
}
