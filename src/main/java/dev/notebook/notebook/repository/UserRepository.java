package dev.notebook.notebook.repository;

import dev.notebook.notebook.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

  boolean existsByEmail(String email);

  boolean existsByUsername(String username);

  Optional<User> findByUsername(String username);

  Optional<User> findByEmail(String email);

  @Override
  @EntityGraph(attributePaths = {"projects"})
  List<User> findAll();
}
