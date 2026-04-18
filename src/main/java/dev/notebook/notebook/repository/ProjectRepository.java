package dev.notebook.notebook.repository;

import dev.notebook.notebook.entity.Project;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<Project, Long> {

  @Override
  @EntityGraph(attributePaths = {"user", "tasks", "tasks.categories", "tasks.reminders"})
  List<Project> findAll();

  @Override
  @EntityGraph(attributePaths = {"user", "tasks", "tasks.categories", "tasks.reminders"})
  Optional<Project> findById(Long id);

  @EntityGraph(attributePaths = {"user", "tasks", "tasks.categories", "tasks.reminders"})
  @Query("""
      SELECT DISTINCT p
      FROM Project p
      LEFT JOIN p.user
      LEFT JOIN p.tasks
      WHERE p.name LIKE CONCAT('%', COALESCE(:projectName, ''), '%')
        AND EXISTS (
          SELECT 1
          FROM Task t
          WHERE t.project = p
            AND t.title LIKE CONCAT('%', COALESCE(:taskTitle, ''), '%')
            AND (
              :completed IS NULL
              OR (:completed = true AND t.completed IS NOT NULL)
              OR (:completed = false AND t.completed IS NULL)
            )
            AND t.dueDate >= COALESCE(:dueFrom, t.dueDate)
            AND t.dueDate <= COALESCE(:dueTo, t.dueDate)
        )
      """)
  Page<Project> searchByTaskJpql(
      @Param("projectName") String projectName,
      @Param("taskTitle") String taskTitle,
      @Param("completed") Boolean completed,
      @Param("dueFrom") LocalDateTime dueFrom,
      @Param("dueTo") LocalDateTime dueTo,
      Pageable pageable
  );
}
