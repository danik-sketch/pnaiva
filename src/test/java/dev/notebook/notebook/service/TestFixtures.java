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

  private TestFixtures() {}

  static User user(Long id) {
    User user = new User();
    user.setId(id);
    user.setUsername("user" + id);
    user.setEmail("user" + id + "@mail.com");
    user.setPassword("password");
    return user;
  }

  static User user(Long id, String username) {
    User user = user(id);
    user.setUsername(username);
    return user;
  }

  static User user(Long id, String username, String email, String password) {
    User user = new User();
    user.setId(id);
    user.setUsername(username);
    user.setEmail(email);
    user.setPassword(password);
    return user;
  }

  static Project project(Long id) {
    Project project = new Project();
    project.setId(id);
    project.setName("project" + id);
    project.setDescription("desc" + id);
    project.setUser(user(1L));
    return project;
  }

  static Project project(Long id, User user) {
    Project project = project(id);
    project.setUser(user);
    return project;
  }

  static Project project(Long id, String name, String description, User user) {
    Project project = new Project();
    project.setId(id);
    project.setName(name);
    project.setDescription(description);
    project.setUser(user);
    return project;
  }

  static Task task(Long id) {
    Task task = new Task();
    task.setId(id);
    task.setTitle("task" + id);
    task.setDescription("desc" + id);
    task.setDueDate(FIXED_TIME);
    task.setProject(project(1L));
    return task;
  }

  static Task task(Long id, Project project) {
    Task task = task(id);
    task.setProject(project);
    return task;
  }

  static Task task(
      Long id,
      String title,
      String description,
      LocalDateTime dueDate,
      LocalDateTime completed,
      Project project
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

  static Reminder reminder(Long id, LocalDateTime time, String message, Task task) {
    Reminder reminder = new Reminder();
    reminder.setId(id);
    reminder.setTime(time);
    reminder.setMessage(message);
    reminder.setTask(task);
    return reminder;
  }

  static Category category(Long id) {
    Category category = new Category();
    category.setId(id);
    category.setTitle("category" + id);
    return category;
  }

  static Category category(Long id, String title) {
    Category category = new Category();
    category.setId(id);
    category.setTitle(title);
    return category;
  }
}