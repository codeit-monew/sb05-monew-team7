package com.spring.monew.commentlike.repository;

import com.spring.monew.commentlike.domain.CommentLike;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentLikeRepository extends JpaRepository<CommentLike, UUID> {

}