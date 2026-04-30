package dev.notebook.notebook.repository;

import dev.notebook.notebook.entity.Reminder;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {

  @EntityGraph(attributePaths = {"task", "task.project", "task.project.user"})
  List<Reminder> findByTask_Project_UserId(Long userId);

  @Query("SELECT r FROM Reminder r JOIN r.task t JOIN t.project p WHERE p.user.id = :userId")
  List<Reminder> findRemindersByUserId(@Param("userId") Long userId);
}
