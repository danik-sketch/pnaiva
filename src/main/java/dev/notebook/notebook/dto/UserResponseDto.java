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
public class UserResponseDto {

  private Long id;
  private String username;
  private String email;
  private List<String> projects;
}

