package dev.notebook.notebook.service;

import dev.notebook.notebook.entity.Category;
import dev.notebook.notebook.entity.Project;
import dev.notebook.notebook.entity.Reminder;
import dev.notebook.notebook.entity.Task;
import dev.notebook.notebook.entity.User;
import java.time.LocalDateTime;

final class TestFixtures {

  static final LocalDateTime FIXED_TIME = LocalDateTime.of(2026, 4, 16, 12, 0);
  static final LocalDateTime OTHER_TIME = LocalDateTime.of(2026, 4, 17, 10, 0);

  private TestFixtures() {
  }

  static User user(Long id, String username, String email, String password) {
    User user = new User();
    user.setId(id);
    user.setUsername(username);
    user.setEmail(email);
    user.setPassword(password);
    return user;
  }

  static User user(Long id, String username) {
    User user = new User();
    user.setId(id);
    user.setUsername(username);
    return user;
  }

  static Project project(Long id, String name, String description, User user) {
    Project project = new Project();
    project.setId(id);
    project.setName(name);
    project.setDescription(description);
    project.setUser(user);
    return project;
  }

  static Project project(Long id, String name) {
    Project project = new Project();
    project.setId(id);
    project.setName(name);
    return project;
  }

  static Task task(
      Long id, String title, String description,
      LocalDateTime dueDate, LocalDateTime completed, Project project
  ) {
    Task task = new Task();
    task.setId(id);
    task.setTitle(title);
    task.setDescription(description);
    task.setDueDate(dueDate);
    task.setCompleted(completed);
    task.setProject(project);
    return task;
  }

  static Task task(Long id) {
    Task task = new Task();
    task.setId(id);
    return task;
  }

  static Reminder reminder(Long id, LocalDateTime time, String message, Task task) {
    Reminder reminder = new Reminder();
    reminder.setId(id);
    reminder.setTime(time);
    reminder.setMessage(message);
    reminder.setTask(task);
    return reminder;
  }

  static Category category(Long id, String title) {
    Category category = new Category();
    category.setId(id);
    category.setTitle(title);
    return category;
  }
}
