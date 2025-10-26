package com.spring.monew.user.repository;

import com.spring.monew.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
  boolean existsByEmail(String email);
}
