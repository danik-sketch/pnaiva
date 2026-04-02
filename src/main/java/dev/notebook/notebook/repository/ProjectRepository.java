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

  @Query("""
      SELECT DISTINCT p
      FROM Project p
      LEFT JOIN FETCH p.user
      LEFT JOIN FETCH p.tasks
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

  @EntityGraph(attributePaths = {"user", "tasks", "tasks.categories", "tasks.reminders"})
  @Query(value = """
          SELECT p.*
          FROM projects p
          WHERE p.name LIKE CONCAT('%', COALESCE(:projectName, ''), '%')
            AND EXISTS (
              SELECT 1
              FROM tasks t
              WHERE t.project_id = p.id
                AND t.title LIKE CONCAT('%', COALESCE(:taskTitle, ''), '%')
                AND (:completed IS NULL
                  OR (:completed = true AND t.completed IS NOT NULL)
                  OR (:completed = false AND t.completed IS NULL))
                AND (:dueFrom IS NULL OR t.due_date >= :dueFrom)
                AND (:dueTo IS NULL OR t.due_date <= :dueTo)
            )
          ORDER BY p.id
          """,
      countQuery = """
          SELECT COUNT(p.id)
          FROM projects p
          WHERE p.name LIKE CONCAT('%', COALESCE(:projectName, ''), '%')
            AND EXISTS (
              SELECT 1
              FROM tasks t
              WHERE t.project_id = p.id
                AND t.title LIKE CONCAT('%', COALESCE(:taskTitle, ''), '%')
                AND (:completed IS NULL
                  OR (:completed = true AND t.completed IS NOT NULL)
                  OR (:completed = false AND t.completed IS NULL))
                AND (:dueFrom IS NULL OR t.due_date >= :dueFrom)
                AND (:dueTo IS NULL OR t.due_date <= :dueTo)
            )
          """,
      nativeQuery = true)
  Page<Project> searchByTaskNative(
      @Param("projectName") String projectName,
      @Param("taskTitle") String taskTitle,
      @Param("completed") Boolean completed,
      @Param("dueFrom") LocalDateTime dueFrom,
      @Param("dueTo") LocalDateTime dueTo,
      Pageable pageable
  );
}
