package dev.notebook.notebook.service;

import dev.notebook.notebook.dto.AuthResponse;
import dev.notebook.notebook.dto.LoginRequest;
import dev.notebook.notebook.dto.RegisterRequest;
import dev.notebook.notebook.dto.UserRequestDto;
import dev.notebook.notebook.entity.User;
import dev.notebook.notebook.exception.NotFoundException;
import dev.notebook.notebook.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

  private final UserService userService;
  private final JwtUtils jwtUtils;

  public AuthResponse login(LoginRequest request) {
    User user = userService.findByUsernameOrEmailAndPassword(request.login(), request.password())
        .orElseThrow(() -> new NotFoundException("Invalid login or password"));

    String token = jwtUtils.generateToken(user.getId(), user.getUsername());
    return new AuthResponse(token, user.getId(), user.getUsername(), user.getEmail());
  }

  @Transactional
  public AuthResponse register(RegisterRequest request) {
    UserRequestDto userDto = new UserRequestDto(request.username(), request.email(),
        request.password());
    var savedUser = userService.create(userDto);

    String token = jwtUtils.generateToken(savedUser.getId(), savedUser.getUsername());
    return new AuthResponse(token, savedUser.getId(), savedUser.getUsername(),
        savedUser.getEmail());
  }
}
