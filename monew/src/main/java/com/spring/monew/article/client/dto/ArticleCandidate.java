package com.spring.monew.article.client.dto;

import com.spring.monew.article.domain.ArticleSource;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ArticleCandidate {

    private ArticleSource source;
    private String sourceUrl;
    private String title;
    private Instant publishDate;
    private String summary;
}
