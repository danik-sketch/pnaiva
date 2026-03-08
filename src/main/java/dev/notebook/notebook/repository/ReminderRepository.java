package dev.notebook.notebook.repository;

import dev.notebook.notebook.entity.Reminder;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {

  List<Reminder> findByTaskId(Long taskId);

  List<Reminder> findByReminderTimeBefore(LocalDateTime time);
}

