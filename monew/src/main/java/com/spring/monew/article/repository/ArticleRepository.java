package com.spring.monew.article.repository;

import com.spring.monew.article.domain.Article;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArticleRepository extends JpaRepository<Article, UUID>, ArticleRepositoryCustom {

  @Query("select a.title from Article a where a.id = :id")
  String findTitleOnlyById(@Param("id") UUID id);
}
