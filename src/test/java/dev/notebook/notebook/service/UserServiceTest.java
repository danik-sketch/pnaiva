package dev.notebook.notebook.service;

import static dev.notebook.notebook.service.TestFixtures.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.notebook.notebook.dto.UserRequestDto;
import dev.notebook.notebook.dto.UserResponseDto;
import dev.notebook.notebook.entity.User;
import dev.notebook.notebook.exception.EmailAlreadyExistsException;
import dev.notebook.notebook.exception.NotFoundException;
import dev.notebook.notebook.exception.OperationFailedException;
import dev.notebook.notebook.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.EmptyResultDataAccessException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private UserService userService;

  @Test
  void createShouldHandleUniqueEmailAndFailure() {
    UserRequestDto request = new UserRequestDto("john", "john@mail.com", "password123");
    when(userRepository.existsByEmail(request.email())).thenReturn(true);
    assertThatThrownBy(() -> userService.create(request))
        .isInstanceOf(EmailAlreadyExistsException.class)
        .hasMessage("Email already exists");

    when(userRepository.existsByEmail(request.email())).thenReturn(false);
    when(userRepository.save(any(User.class))).thenReturn(user(1L, "john", "john@mail.com", "password123"));
    UserResponseDto ok = userService.create(request);
    assertThat(ok.getId()).isEqualTo(1L);

    when(userRepository.save(any(User.class))).thenThrow(new DataAccessResourceFailureException("db down"));
    assertThatThrownBy(() -> userService.create(request))
        .isInstanceOf(OperationFailedException.class)
        .hasMessage("Failed to create user");
  }

  @Test
  void updateShouldCoverAllEmailBranches() {
    User existing = user(42L, "john", "old@mail.com", "password123");
    when(userRepository.findById(42L)).thenReturn(Optional.of(existing));
    UserRequestDto takenEmailRequest = new UserRequestDto("john", "new@mail.com", "password123");
    UserRequestDto validUpdateRequest = new UserRequestDto("johnny", "new@mail.com", "password123");

    when(userRepository.existsByEmail("new@mail.com")).thenReturn(true);
    assertThatThrownBy(() -> userService.update(42L, takenEmailRequest))
        .isInstanceOf(EmailAlreadyExistsException.class)
        .hasMessage("Email already exists");

    when(userRepository.existsByEmail("new@mail.com")).thenReturn(false);
    when(userRepository.save(existing)).thenReturn(existing);
    UserResponseDto updated = userService.update(42L, validUpdateRequest);
    assertThat(updated.getEmail()).isEqualTo("new@mail.com");

    when(userRepository.save(existing)).thenThrow(new DataAccessResourceFailureException("db down"));
    assertThatThrownBy(() -> userService.update(42L, validUpdateRequest))
        .isInstanceOf(OperationFailedException.class)
        .hasMessage("Failed to update user");
  }

  @Test
  void updateShouldThrowWhenUserNotFound() {
    when(userRepository.findById(999L)).thenReturn(Optional.empty());
    UserRequestDto requestDto = new UserRequestDto("john", "john@mail.com", "password123");

    assertThatThrownBy(() -> userService.update(999L, requestDto))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("User not found");
  }

  @Test
  void deleteShouldMapExceptions() {
    doThrow(new EmptyResultDataAccessException(1)).when(userRepository).deleteById(99L);
    assertThatThrownBy(() -> userService.delete(99L))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("User not found");

    doThrow(new DataAccessResourceFailureException("db down")).when(userRepository).deleteById(100L);
    assertThatThrownBy(() -> userService.delete(100L))
        .isInstanceOf(OperationFailedException.class)
        .hasMessage("Failed to delete user");
  }

  @Test
  void deleteShouldCallRepository() {
    userService.delete(101L);
    verify(userRepository).deleteById(101L);
  }

  @Test
  void getByIdShouldMapFoundAndNotFound() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L, "john", "john@mail.com", "password123")));
    assertThat(userService.getById(1L).getUsername()).isEqualTo("john");

    when(userRepository.findById(2L)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> userService.getById(2L))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("User not found");
  }

  @Test
  void getAllShouldMapUsers() {
    when(userRepository.findAll()).thenReturn(List.of(
        user(1L, "john", "john@mail.com", "password123"),
        user(2L, "alice", "alice@mail.com", "password123")));

    List<UserResponseDto> result = userService.getAll();

    assertThat(result).extracting(UserResponseDto::getUsername).containsExactly("john", "alice");
  }
}
