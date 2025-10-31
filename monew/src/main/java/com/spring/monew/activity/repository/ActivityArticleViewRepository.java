package com.spring.monew.activity.repository;

import com.spring.monew.activity.domain.ActivityArticleViewDoc;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ActivityArticleViewRepository extends MongoRepository<ActivityArticleViewDoc, String> {
    Optional<ActivityArticleViewDoc> findByUserIdAndArticleId(UUID userId, UUID articleId);
}
