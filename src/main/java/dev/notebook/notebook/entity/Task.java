package dev.notebook.notebook.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tasks")
public class Task {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String title;
  private String description;
  private LocalDate dueDate;
  private boolean completed;

  public Task() {
  }

  public Task(String title, String description, LocalDate dueDate, boolean completed) {
    this.title = title;
    this.description = description;
    this.dueDate = dueDate;
    this.completed = completed;
  }

}