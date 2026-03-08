package dev.notebook.notebook.repository;

import dev.notebook.notebook.entity.Project;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProjectRepository extends JpaRepository<Project, Long> {

  List<Project> findByNameContaining(String name);

  @EntityGraph(attributePaths = {"user", "tasks"})
  @Query("select p from Project p")
  List<Project> findAllWithUserAndTasks();
}

