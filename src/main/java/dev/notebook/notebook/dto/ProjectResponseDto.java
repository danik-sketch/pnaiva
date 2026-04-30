package dev.notebook.notebook.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(description = "Project data returned by the API")
public class ProjectResponseDto {

  @Schema(description = "Project identifier")
  private Long id;
  @Schema(description = "Project name")
  private String name;
  @Schema(description = "Project description")
  private String description;
  @Schema(description = "Owner username")
  private String username;
  @Schema(description = "Tasks included in the project")
  private List<TaskResponseDto> tasks;
}
