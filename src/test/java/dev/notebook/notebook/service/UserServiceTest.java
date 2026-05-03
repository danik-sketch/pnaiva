package dev.notebook.notebook.service;

import dev.notebook.notebook.dto.UserRequestDto;
import dev.notebook.notebook.dto.UserResponseDto;
import dev.notebook.notebook.entity.User;
import dev.notebook.notebook.exception.EmailAlreadyExistsException;
import dev.notebook.notebook.exception.NotFoundException;
import dev.notebook.notebook.exception.OperationFailedException;
import dev.notebook.notebook.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.List;
import java.util.Optional;

import static dev.notebook.notebook.service.TestFixtures.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private UserService userService;

  @Test
  void create_success() {
    UserRequestDto dto = new UserRequestDto("john", "john@mail.com", "123");

    when(userRepository.existsByEmail(dto.email())).thenReturn(false);
    when(userRepository.save(any(User.class)))
        .thenReturn(user(1L, "john", "john@mail.com", "123"));

    UserResponseDto result = userService.create(dto);

    assertThat(result.getId()).isEqualTo(1L);
    verify(userRepository).save(any(User.class));
  }

  @Test
  void create_emailExists() {
    UserRequestDto dto = new UserRequestDto("john", "john@mail.com", "123");

    when(userRepository.existsByEmail(dto.email())).thenReturn(true);

    assertThatThrownBy(() -> userService.create(dto))
        .isInstanceOf(EmailAlreadyExistsException.class);

    verify(userRepository, never()).save(any());
  }

  @Test
  void create_repositoryError() {
    UserRequestDto dto = new UserRequestDto("john", "john@mail.com", "123");

    when(userRepository.existsByEmail(dto.email())).thenReturn(false);
    when(userRepository.save(any()))
        .thenThrow(new RuntimeException("db error"));

    assertThatThrownBy(() -> userService.create(dto))
        .isInstanceOf(OperationFailedException.class);
  }

  @Test
  void update_success() {
    User existing = user(1L, "john", "old@mail.com", "123");

    when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
    when(userRepository.existsByEmail("new@mail.com")).thenReturn(false);
    when(userRepository.save(any(User.class))).thenReturn(existing);

    UserRequestDto dto = new UserRequestDto("johnny", "new@mail.com", "123");

    UserResponseDto result = userService.update(1L, dto);

    assertThat(result.getEmail()).isEqualTo("new@mail.com");
  }

  @Test
  void update_notFound() {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() ->
        userService.update(1L, new UserRequestDto("a", "b", "c")))
        .isInstanceOf(NotFoundException.class);
  }


  @Test
  void update_repositoryError() {
    User existing = user(1L, "john", "old@mail.com", "123");

    when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
    when(userRepository.existsByEmail("new@mail.com")).thenReturn(false);
    when(userRepository.save(any(User.class)))
        .thenThrow(new RuntimeException("db error"));

    assertThatThrownBy(() ->
        userService.update(1L, new UserRequestDto("john", "new@mail.com", "123")))
        .isInstanceOf(OperationFailedException.class);
  }

  @Test
  void delete_success() {
    userService.delete(1L);

    verify(userRepository).deleteById(1L);
  }

  @Test
  void delete_notFound() {
    doThrow(new EmptyResultDataAccessException(1))
        .when(userRepository).deleteById(1L);

    assertThatThrownBy(() -> userService.delete(1L))
        .isInstanceOf(NotFoundException.class);
  }

  @Test
  void delete_repositoryError() {
    doThrow(new RuntimeException("db error"))
        .when(userRepository).deleteById(1L);

    assertThatThrownBy(() -> userService.delete(1L))
        .isInstanceOf(OperationFailedException.class);
  }

  @Test
  void getById_success() {
    when(userRepository.findById(1L))
        .thenReturn(Optional.of(user(1L, "john", "mail", "pass")));

    assertThat(userService.getById(1L).getUsername()).isEqualTo("john");
  }

  @Test
  void getById_notFound() {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.getById(1L))
        .isInstanceOf(NotFoundException.class);
  }

  @Test
  void getAll_success() {
    when(userRepository.findAll()).thenReturn(List.of(
        user(1L, "john", "a", "p"),
        user(2L, "alice", "b", "p")
    ));

    List<UserResponseDto> result = userService.getAll();

    assertThat(result).hasSize(2);
  }

  @Test
  void login_byUsername_success() {
    User u = user(1L, "john", "john@mail.com", "123");

    when(userRepository.findByUsername("john"))
        .thenReturn(Optional.of(u));

    assertThat(userService.findByUsernameOrEmailAndPassword("john", "123"))
        .isPresent()
        .contains(u);
  }

  @Test
  void login_byEmail_success() {
    User u = user(1L, "john", "john@mail.com", "123");

    when(userRepository.findByEmail("john@mail.com"))
        .thenReturn(Optional.of(u));

    assertThat(userService.findByUsernameOrEmailAndPassword("john@mail.com", "123"))
        .isPresent()
        .contains(u);
  }

  @Test
  void login_wrongPassword_returnsEmpty() {
    User u = user(1L, "john", "mail", "123");

    when(userRepository.findByUsername("john"))
        .thenReturn(Optional.of(u));

    assertThat(userService.findByUsernameOrEmailAndPassword("john", "wrong"))
        .isEmpty();
  }

  @Test
  void login_notFound_returnsEmpty() {
    when(userRepository.findByUsername("x"))
        .thenReturn(Optional.empty());

    assertThat(userService.findByUsernameOrEmailAndPassword("x", "123"))
        .isEmpty();
  }

  @Test
  void update_emailExists() {
    User existing = user(1L, "john", "old@mail.com", "123");

    when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
    when(userRepository.existsByEmail("new@mail.com")).thenReturn(true);

    assertThatThrownBy(() ->
        userService.update(1L, new UserRequestDto("john", "new@mail.com", "123")))
        .isInstanceOf(EmailAlreadyExistsException.class);
  }
}