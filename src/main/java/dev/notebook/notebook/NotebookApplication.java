package dev.notebook.notebook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SuppressWarnings("checkstyle:MissingJavadocType")
@SpringBootApplication
public class NotebookApplication {

  private NotebookApplication() {
  }

  static void main(String[] args) {
    SpringApplication.run(NotebookApplication.class, args);
  }
}