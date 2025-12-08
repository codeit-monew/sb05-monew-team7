package com.spring.monew.interest.repository;

import com.spring.monew.interest.domain.Interest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InterestRepository extends JpaRepository<Interest, UUID>, InterestRepositoryCustom {

  boolean existsByName(String name);

  @Query(value = "SELECT * FROM interests WHERE id = :interestId", nativeQuery = true)
  Optional<Interest> findIncludingDeleted(@Param("interestId") UUID interestId);

  @Query(value = "SELECT * FROM interests WHERE is_deleted = true ORDER BY deleted_at DESC LIMIT 1", nativeQuery = true)
  Optional<Interest> findFirstDeleted();
}
