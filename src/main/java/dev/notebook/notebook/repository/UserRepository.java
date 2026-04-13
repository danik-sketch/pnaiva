package dev.notebook.notebook.repository;

import dev.notebook.notebook.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

  boolean existsByEmail(String email);

  @Override
  @EntityGraph(attributePaths = {"projects"})
  List<User> findAll();
}
