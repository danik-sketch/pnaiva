package dev.notebook.notebook.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI notebookOpenApi() {
    return new OpenAPI()
        .info(new Info()
            .title("Notebook API")
            .description("REST API for managing users, projects, tasks, categories and reminders")
            .version("v1")
            .license(new License().name("Internal use")));
  }
}
