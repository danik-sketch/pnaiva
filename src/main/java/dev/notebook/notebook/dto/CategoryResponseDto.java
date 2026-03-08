package dev.notebook.notebook.dto;

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
public class CategoryResponseDto {

  private Long id;
  private String title;
  private List<String> tasks;
}

