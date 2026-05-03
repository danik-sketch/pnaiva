package dev.notebook.notebook.service;

import dev.notebook.notebook.dto.AuthResponse;
import dev.notebook.notebook.dto.LoginRequest;
import dev.notebook.notebook.dto.RegisterRequest;
import dev.notebook.notebook.dto.UserResponseDto;
import dev.notebook.notebook.entity.User;
import dev.notebook.notebook.exception.NotFoundException;
import dev.notebook.notebook.security.JwtUtils;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static dev.notebook.notebook.service.TestFixtures.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock
  private UserService userService;

  @Mock
  private JwtUtils jwtUtils;

  @InjectMocks
  private AuthService authService;

  @Test
  void loginShouldReturnAuthResponseWhenCredentialsValid() {
    LoginRequest request = new LoginRequest("john", "password123");
    User user = user(1L, "john", "john@mail.com", "password123");
    when(userService.findByUsernameOrEmailAndPassword("john", "password123"))
        .thenReturn(Optional.of(user));
    when(jwtUtils.generateToken(1L, "john")).thenReturn("jwt-token");

    AuthResponse response = authService.login(request);

    assertThat(response.token()).isEqualTo("jwt-token");
    assertThat(response.userId()).isEqualTo(1L);
    assertThat(response.username()).isEqualTo("john");
    assertThat(response.email()).isEqualTo("john@mail.com");
  }

  @Test
  void loginShouldThrowWhenCredentialsInvalid() {
    LoginRequest request = new LoginRequest("john", "wrongpassword");
    when(userService.findByUsernameOrEmailAndPassword("john", "wrongpassword"))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> authService.login(request))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("Invalid login or password");
  }

  @Test
  void registerShouldCreateUserAndReturnAuthResponse() {
    RegisterRequest request = new RegisterRequest("john", "john@mail.com", "password123");
    UserResponseDto userDto = new UserResponseDto(1L, "john", "john@mail.com", null);
    when(userService.create(any())).thenReturn(userDto);
    when(jwtUtils.generateToken(1L, "john")).thenReturn("jwt-token");

    AuthResponse response = authService.register(request);

    assertThat(response.token()).isEqualTo("jwt-token");
    assertThat(response.userId()).isEqualTo(1L);
    assertThat(response.username()).isEqualTo("john");
    assertThat(response.email()).isEqualTo("john@mail.com");
  }
}
