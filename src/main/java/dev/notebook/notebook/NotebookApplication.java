package dev.notebook.notebook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NotebookApplication {

  private NotebookApplication() {
  }

  public static void main(String[] args) {
    SpringApplication.run(NotebookApplication.class, args);
  }
}