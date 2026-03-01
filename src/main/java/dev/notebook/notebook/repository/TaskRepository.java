package dev.notebook.notebook.repository;

import dev.notebook.notebook.entity.Task;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

  List<Task> findByCompleted(boolean completed);

  List<Task> findByDueDate(LocalDate dueDate);

}