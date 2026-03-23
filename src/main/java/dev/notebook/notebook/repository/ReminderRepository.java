package dev.notebook.notebook.repository;

import dev.notebook.notebook.entity.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {

}
