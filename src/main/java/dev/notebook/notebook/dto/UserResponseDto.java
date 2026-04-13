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
@Schema(description = "User data returned by the API")
public class UserResponseDto {

  @Schema(description = "User identifier")
  private Long id;
  @Schema(description = "Username")
  private String username;
  @Schema(description = "Email address")
  private String email;
  @Schema(description = "Names of projects assigned to the user")
  private List<String> projects;
}
