package com.spring.monew.comment.repository;

import com.spring.monew.comment.domain.Comment;
import com.spring.monew.user.domain.User;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, UUID>, CommentRepositoryCustom{

  @Modifying
  @Query("DELETE FROM Comment c where c.id = :commentId")
  void deletePhysicalById(@Param("commentId") UUID commentId);

  UUID user(User user);
}