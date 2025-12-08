package com.spring.monew.article.repository;

import com.spring.monew.article.domain.Article;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArticleRepository extends JpaRepository<Article, UUID>, ArticleRepositoryCustom {

  @Query("select a.sourceUrl from Article a where a.sourceUrl in :urls")
  Set<String> findExistingSourceUrls(@Param("urls") Collection<String> urls);

  @Modifying(clearAutomatically = true)
  @Query(value = "DELETE FROM articles WHERE id = :articleId", nativeQuery = true)
  void hardDelete(@Param("articleId") UUID articleId);

  @Query(value = "SELECT EXISTS(SELECT 1 FROM articles WHERE id = :articleId)", nativeQuery = true)
  boolean existsIncludingDeleted(@Param("articleId") UUID articleId);

  @Query(value = "SELECT * FROM articles WHERE id = :articleId AND is_deleted = true", nativeQuery = true)
  Optional<Article> findIncludingDeleted(@Param("articleId") UUID articleId);

  @Query("SELECT a.sourceUrl FROM Article a")
  Set<String> findAllSourceUrls();

  @Query(value = """
    SELECT id FROM articles
    WHERE is_deleted = true
      AND deleted_at IS NOT NULL
      AND deleted_at < :threshold
    """, nativeQuery = true)
  List<UUID> findSoftDeletedBefore(@Param("threshold") Instant threshold);

  @Query(value = """
    SELECT id FROM articles
    WHERE is_deleted = true
      AND deleted_at IS NOT NULL
      AND deleted_at >= :startTime
      AND deleted_at <= :endTime
    """, nativeQuery = true)
  List<UUID> findSoftDeletedBetween(@Param("startTime") Instant startTime, @Param("endTime") Instant endTime);

  List<Article> findAllByInterestId(UUID interestId);
}
