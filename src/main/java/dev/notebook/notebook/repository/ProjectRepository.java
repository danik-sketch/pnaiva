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
  @EntityGraph(attributePaths = {"user", "tasks"})
  List<Project> findAll();

  @Override
  @EntityGraph(attributePaths = {"user", "tasks"})
  Optional<Project> findById(Long id);

  @EntityGraph(attributePaths = {"user", "tasks"})
  @Query("""
      select p
      from Project p
      where (:projectName is null or p.name = :projectName)
        and exists (
          select 1
          from Task t
          where t.project = p
            and (:taskTitle is null or t.title like concat('%', :taskTitle, '%'))
        and (:completed is null
          or (:completed = true and t.completed is not null)
          or (:completed = false and t.completed is null))
            and t.dueDate >= :dueFrom
            and t.dueDate <= :dueTo
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

  @EntityGraph(attributePaths = {"user", "tasks"})
  @Query(
      value = """
          select p.*
          from projects p
          where (:projectName is null or p.name = :projectName)
            and exists (
              select 1
              from tasks t
              where t.project_id = p.id
                and (:taskTitle is null or t.title like concat('%', :taskTitle, '%'))
                and (:completed is null
                  or (:completed = true and t.completed is not null)
                  or (:completed = false and t.completed is null))
                and t.due_date >= :dueFrom
                and t.due_date <= :dueTo
            )
          """,
      countQuery = """
          select count(p.id)
          from projects p
          where (:projectName is null or p.name = :projectName)
            and exists (
              select 1
              from tasks t
              where t.project_id = p.id
                and (:taskTitle is null or t.title like concat('%', :taskTitle, '%'))
                and (:completed is null
                  or (:completed = true and t.completed is not null)
                  or (:completed = false and t.completed is null))
                and t.due_date >= :dueFrom
                and t.due_date <= :dueTo
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
