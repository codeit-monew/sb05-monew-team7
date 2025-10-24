package com.spring.monew.interest.repository;

import com.spring.monew.interest.domain.Interest;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterestRepository extends JpaRepository<Interest, UUID> {

  boolean existsByName(String name);
}