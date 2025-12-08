package com.spring.monew.user.repository;

import com.spring.monew.user.domain.User;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, UUID> {
  boolean existsByEmail(String email);

  Optional<User> findByEmail(String email);

  //1일 보존 후 자동 삭제
  @Modifying
  @Query(value = """
  DELETE FROM users
  WHERE is_deleted = true
    AND deleted_at IS NOT NULL
    AND deleted_at < :threshold
  """, nativeQuery = true)
  int deletedSoftUsers(@Param("threshold") Instant threshold);

  //즉시 물리 삭제
  @Modifying
  @Query(value = "DELETE FROM users WHERE id = :userId", nativeQuery = true)
  void deletePhysicalUsers(@Param("userId") UUID userId);
}

