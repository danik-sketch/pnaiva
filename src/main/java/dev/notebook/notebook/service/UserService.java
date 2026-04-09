package dev.notebook.notebook.service;

import dev.notebook.notebook.dto.UserRequestDto;
import dev.notebook.notebook.dto.UserResponseDto;
import dev.notebook.notebook.entity.User;
import dev.notebook.notebook.exception.NotFoundException;
import dev.notebook.notebook.mapper.UserMapper;
import dev.notebook.notebook.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

  private final UserRepository userRepository;

  @Transactional
  public UserResponseDto create(UserRequestDto dto) {
    User user = new User();
    user.setUsername(dto.username());
    user.setEmail(dto.email());
    user.setPassword(dto.password());
    User saved = userRepository.save(user);
    return UserMapper.toDto(saved);
  }

  @Transactional
  public UserResponseDto update(Long id, UserRequestDto dto) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("User not found"));

    user.setUsername(dto.username());
    user.setEmail(dto.email());
    user.setPassword(dto.password());

    User saved = userRepository.save(user);
    return UserMapper.toDto(saved);
  }

  @Transactional
  public void delete(Long id) {
    userRepository.deleteById(id);
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
