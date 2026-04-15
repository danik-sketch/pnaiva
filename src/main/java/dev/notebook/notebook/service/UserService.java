package dev.notebook.notebook.service;

import dev.notebook.notebook.dto.UserRequestDto;
import dev.notebook.notebook.dto.UserResponseDto;
import dev.notebook.notebook.entity.User;
import dev.notebook.notebook.exception.EmailAlreadyExistsException;
import dev.notebook.notebook.exception.NotFoundException;
import dev.notebook.notebook.exception.OperationFailedException;
import dev.notebook.notebook.mapper.UserMapper;
import dev.notebook.notebook.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

  private final UserRepository userRepository;

  @Transactional
  public UserResponseDto create(UserRequestDto dto) {
    if (userRepository.existsByEmail(dto.email())) {
      throw new EmailAlreadyExistsException("Email already exists");
    }

    try {
      User user = new User();
      user.setUsername(dto.username());
      user.setEmail(dto.email());
      user.setPassword(dto.password());
      User saved = userRepository.save(user);
      return UserMapper.toDto(saved);
    } catch (RuntimeException exception) {
      throw new OperationFailedException("Failed to create user", exception);
    }
  }

  @Transactional
  public UserResponseDto update(Long id, UserRequestDto dto) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("User not found"));

    if (!user.getEmail().equals(dto.email()) && userRepository.existsByEmail(dto.email())) {
      throw new EmailAlreadyExistsException("Email already exists");
    }

    try {
      user.setUsername(dto.username());
      user.setEmail(dto.email());
      user.setPassword(dto.password());

      User saved = userRepository.save(user);
      return UserMapper.toDto(saved);
    } catch (RuntimeException exception) {
      throw new OperationFailedException("Failed to update user", exception);
    }
  }

  @Transactional
  public void delete(Long id) {
    try {
      userRepository.deleteById(id);
    } catch (EmptyResultDataAccessException _) {
      throw new NotFoundException("User not found");
    } catch (RuntimeException exception) {
      throw new OperationFailedException("Failed to delete user", exception);
    }
  }

  public UserResponseDto getById(Long id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("User not found"));
    return UserMapper.toDto(user);
  }

  public List<UserResponseDto> getAll() {
    List<User> users = userRepository.findAll();
    List<UserResponseDto> result = new ArrayList<>();
    for (User user : users) {
      result.add(UserMapper.toDto(user));
    }
    return result;
  }
}
