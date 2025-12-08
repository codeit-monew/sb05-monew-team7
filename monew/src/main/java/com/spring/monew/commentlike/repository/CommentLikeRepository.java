package com.spring.monew.commentlike.repository;

import com.spring.monew.commentlike.domain.CommentLike;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentLikeRepository extends JpaRepository<CommentLike, UUID> {

  boolean existsByComment_IdAndUser_Id(UUID commentId, UUID userId);

  Optional<CommentLike> findByComment_IdAndUser_Id(UUID commentId, UUID userId);
}