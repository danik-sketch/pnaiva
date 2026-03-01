package dev.notebook.notebook.dataloader;

import dev.notebook.notebook.entity.Task;
import dev.notebook.notebook.repository.TaskRepository;
import java.time.LocalDate;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@SuppressWarnings("checkstyle:MissingJavadocType")
@Component
public class DataLoader implements CommandLineRunner {

  private final TaskRepository repository;

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public DataLoader(TaskRepository repository) {
    this.repository = repository;
  }

  @Override
  public void run(String @NonNull ... args) throws Exception {
    repository.save(new Task("Купить молоко", "В магазине у дома",
        LocalDate.of(2026, 3, 1), false));
    repository.save(new Task("Сдать отчёт", "До конца недели", LocalDate.of(2026, 3, 3), false));
    repository.save(new Task("Позвонить маме", "", LocalDate.of(2026, 2, 28), true));
  }
}