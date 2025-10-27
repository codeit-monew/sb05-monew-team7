package com.spring.monew.article.repository;

import com.spring.monew.article.domain.Article;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleRepository extends JpaRepository<Article, UUID> {
}
