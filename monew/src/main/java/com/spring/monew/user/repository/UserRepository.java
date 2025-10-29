package com.spring.monew.user.repository;

import com.spring.monew.user.domain.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {
  boolean existsByEmail(String email);

  Optional<User> findByEmail(String email);
}
