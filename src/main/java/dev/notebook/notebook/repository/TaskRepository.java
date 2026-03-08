package dev.notebook.notebook.repository;

import dev.notebook.notebook.entity.Task;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

  List<Task> findByTitleContaining(String title);

  List<Task> findByCompleted(boolean completed);

  List<Task> findByDueDate(LocalDateTime dueDate);

  List<Task> findByDescription(String description);

  @EntityGraph(attributePaths = {"project", "categories", "reminders"})
  List<Task> findAll();
}