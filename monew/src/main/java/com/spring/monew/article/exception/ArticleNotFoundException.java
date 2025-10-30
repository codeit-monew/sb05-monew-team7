package com.spring.monew.article.exception;

import java.util.UUID;

public class ArticleNotFoundException extends RuntimeException {
    public ArticleNotFoundException(UUID articleId) {
        super("Article not found with id: " + articleId);
    }
}
