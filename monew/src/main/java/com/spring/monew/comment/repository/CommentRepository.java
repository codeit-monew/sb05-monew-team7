package com.spring.monew.comment.repository;

import com.spring.monew.comment.domain.Comment;
import com.spring.monew.user.domain.User;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, UUID>, CommentRepositoryCustom{

  @Modifying(clearAutomatically = true)
  @Query(value = "DELETE FROM comments WHERE id = :commentId", nativeQuery = true)
  void deletePhysicalById(@Param("commentId") UUID commentId);

  @Modifying(clearAutomatically = true)
  @Query(value = """
    DELETE FROM comments
    WHERE is_deleted = true
      AND deleted_at IS NOT NULL
      AND deleted_at < :threshold
    """, nativeQuery = true)
  int deleteSoftDeletedBefore(@Param("threshold") Instant threshold);

  @Query(value = "SELECT * FROM comments c WHERE c.id = :commentId AND c.is_deleted = true", nativeQuery = true)
  Optional<Comment> findIncludingDeleted(@Param("commentId") UUID commentId);
}