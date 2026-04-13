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
@Schema(description = "Category data returned by the API")
public class CategoryResponseDto {

  @Schema(description = "Category identifier")
  private Long id;
  @Schema(description = "Category title")
  private String title;
  @Schema(description = "Titles of tasks assigned to the category")
  private List<String> tasks;
}
