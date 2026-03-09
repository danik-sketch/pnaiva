package dev.notebook.notebook.repository;

import dev.notebook.notebook.entity.Task;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

  @EntityGraph(attributePaths = {"project", "categories", "reminders"})
  List<Task> findByTitleContaining(String title);

  @EntityGraph(attributePaths = {"project", "categories", "reminders"})
  List<Task> findByCompleted(boolean completed);

  @EntityGraph(attributePaths = {"project", "categories", "reminders"})
  List<Task> findByDueDate(LocalDateTime dueDate);

  @EntityGraph(attributePaths = {"project", "categories", "reminders"})
  List<Task> findByDescription(String description);

  @Override
  @EntityGraph(attributePaths = {"project", "categories", "reminders"})
  List<Task> findAll();

  @Override
  @EntityGraph(attributePaths = {"project", "categories", "reminders"})
  Optional<Task> findById(Long id);

  @EntityGraph(attributePaths = {"project", "categories", "reminders"})
  List<Task> findAllBy();
}
