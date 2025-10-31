package com.spring.monew.article.repository;

import com.spring.monew.article.domain.Article;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArticleRepository extends JpaRepository<Article, UUID>, ArticleRepositoryCustom {
  boolean existsBySourceUrl(String sourceUrl);

  @Query("select a.sourceUrl from Article a where a.sourceUrl in :urls")
  Set<String> findExistingSourceUrls(@Param("urls") Collection<String> urls);
}
