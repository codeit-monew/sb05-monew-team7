-- ==============================
-- USERS
-- ==============================
CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       nickname VARCHAR(20) NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       created_at TIMESTAMP NOT NULL,
                       role VARCHAR(20) NOT NULL,
                       is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
                       deleted_at TIMESTAMP NULL
);

-- ==============================
-- INTERESTS
-- ==============================
CREATE TABLE interests (
                           id UUID PRIMARY KEY,
                           name VARCHAR(255) NOT NULL UNIQUE,
                           created_at TIMESTAMP NOT NULL,
                           keywords TEXT NOT NULL,
                           subscriptions_count BIGINT NOT NULL DEFAULT 0,
                           updated_at TIMESTAMP NOT NULL,
                           is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
                           deleted_at TIMESTAMP
);

-- ==============================
-- SUBSCRIPTIONS
-- ==============================
CREATE TABLE subscriptions (
                               id UUID PRIMARY KEY,
                               user_id UUID NOT NULL,
                               interest_id UUID NOT NULL,
                               created_at TIMESTAMP NOT NULL,
                               CONSTRAINT fk_subscription_user FOREIGN KEY (user_id) REFERENCES users(id),
                               CONSTRAINT fk_subscription_interest FOREIGN KEY (interest_id) REFERENCES interests(id)
);

-- ==============================
-- ARTICLES
-- ==============================
CREATE TABLE articles (
                          id UUID PRIMARY KEY,
                          interest_id UUID NOT NULL,
                          source VARCHAR(20) NOT NULL,
                          source_url VARCHAR(255) NOT NULL UNIQUE,
                          title VARCHAR(255) NOT NULL,
                          publish_date TIMESTAMP NOT NULL,
                          summary TEXT NOT NULL,
                          comment_count BIGINT NOT NULL DEFAULT 0,
                          view_count BIGINT NOT NULL DEFAULT 0,
                          created_at TIMESTAMP NOT NULL,
                          updated_at TIMESTAMP NOT NULL,
                          is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
                          deleted_at TIMESTAMP NULL,
                          CONSTRAINT fk_article_interest FOREIGN KEY (interest_id) REFERENCES interests(id)
);

-- ==============================
-- ARTICLE_VIEWS
-- ==============================
CREATE TABLE article_views (
                               id UUID PRIMARY KEY,
                               article_id UUID NOT NULL,
                               user_id UUID NOT NULL,
                               created_at TIMESTAMP NOT NULL,
                               CONSTRAINT fk_view_article FOREIGN KEY (article_id) REFERENCES articles(id),
                               CONSTRAINT fk_view_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- ==============================
-- COMMENTS
-- ==============================
CREATE TABLE comments (
                          id UUID PRIMARY KEY,
                          article_id UUID NOT NULL,
                          user_id UUID NOT NULL,
                          content VARCHAR(255) NOT NULL,
                          created_at TIMESTAMP NOT NULL,
                          is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
                          deleted_at TIMESTAMP NULL,
                          like_count BIGINT NOT NULL DEFAULT 0,
                          CONSTRAINT fk_comment_article FOREIGN KEY (article_id) REFERENCES articles(id),
                          CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- ==============================
-- COMMENT_LIKES
-- ==============================
CREATE TABLE comment_likes (
                               id UUID PRIMARY KEY,
                               comment_id UUID NOT NULL,
                               user_id UUID NOT NULL,
                               created_at TIMESTAMP NOT NULL,
                               CONSTRAINT fk_like_comment FOREIGN KEY (comment_id) REFERENCES comments(id),
                               CONSTRAINT fk_like_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- ==============================
-- NOTIFICATIONS
-- ==============================
CREATE TABLE notifications (
                               id UUID PRIMARY KEY,
                               user_id UUID NOT NULL,
                               content VARCHAR(255) NOT NULL,
                               confirmed BOOLEAN NOT NULL,
                               resource_type VARCHAR(20) NOT NULL,
                               resource_id UUID NOT NULL,
                               created_at TIMESTAMP NOT NULL,
                               updated_at TIMESTAMP NOT NULL,
                               CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id)
);
