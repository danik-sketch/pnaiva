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
      WHERE (:currentUserId IS NULL OR p.user.id = :currentUserId)
        AND LOWER(p.name) LIKE LOWER(CONCAT('%', COALESCE(:projectName, ''), '%'))
        AND (
          :taskTitle IS NULL
          OR EXISTS (
            SELECT 1
            FROM Task t1
            WHERE t1.project = p
              AND LOWER(t1.title) LIKE LOWER(CONCAT('%', :taskTitle, '%'))
          )
        )
        AND (
          :completed IS NULL
          OR EXISTS (
            SELECT 1
            FROM Task t2
            WHERE t2.project = p
              AND (
                (:completed = true AND t2.completed IS NOT NULL)
                OR (:completed = false AND t2.completed IS NULL)
              )
          )
        )
        AND (
          :dueFrom IS NULL
          OR EXISTS (
            SELECT 1
            FROM Task t3
            WHERE t3.project = p
              AND t3.dueDate IS NOT NULL
              AND t3.dueDate >= :dueFrom
          )
        )
        AND (
          :dueTo IS NULL
          OR EXISTS (
            SELECT 1
            FROM Task t4
            WHERE t4.project = p
              AND t4.dueDate IS NOT NULL
              AND t4.dueDate <= :dueTo
          )
        )
      """)
  Page<Project> searchByTaskJpql(
      @Param("currentUserId") Long currentUserId,
      @Param("projectName") String projectName,
      @Param("taskTitle") String taskTitle,
      @Param("completed") Boolean completed,
      @Param("dueFrom") LocalDateTime dueFrom,
      @Param("dueTo") LocalDateTime dueTo,
      Pageable pageable
  );

  @EntityGraph(attributePaths = {"user", "tasks", "tasks.categories", "tasks.reminders"})
  List<Project> findAllByUserId(Long currentUserId);
}
