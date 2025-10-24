-- ==============================
-- USERS
-- ==============================
CREATE TYPE user_role AS ENUM ('ADMIN','USER');
CREATE TABLE users
(
    id         UUID PRIMARY KEY,
    email      VARCHAR(255) NOT NULL UNIQUE,
    nickname   VARCHAR(20)  NOT NULL,
    password   VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL,
    role       user_role    NOT NULL,
    is_deleted BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMPTZ  NULL
);

-- ==============================
-- INTERESTS
-- ==============================
CREATE TABLE interests
(
    id                  UUID PRIMARY KEY,
    name                VARCHAR(255) NOT NULL UNIQUE,
    keywords            TEXT         NOT NULL,
    subscriptions_count BIGINT       NOT NULL DEFAULT 0
);

-- ==============================
-- SUBSCRIPTIONS
-- ==============================
CREATE TABLE subscriptions
(
    id          UUID PRIMARY KEY,
    user_id     UUID        NOT NULL,
    interest_id UUID        NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_subscription_user FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_subscription_interest FOREIGN KEY (interest_id)
        REFERENCES interests (id)
        ON DELETE CASCADE ON UPDATE CASCADE
);

-- ==============================
-- ARTICLES
-- ==============================
-- Enum 타입에 맞춰서 변경하면 됨
CREATE TYPE article_resource AS ENUM ('NAVER', 'HANKYUNG', 'CHOSUN', 'YEONHAP');

CREATE TABLE articles
(
    id            UUID PRIMARY KEY,
    interest_id   UUID             NOT NULL,
    source        article_resource NOT NULL,
    source_url    VARCHAR(255)     NOT NULL UNIQUE,
    title         VARCHAR(255)     NOT NULL,
    publish_date  TIMESTAMPTZ      NOT NULL,
    summary       TEXT             NOT NULL,
    comment_count BIGINT           NOT NULL DEFAULT 0,
    article_count BIGINT           NOT NULL DEFAULT 0,
    created_at    TIMESTAMPTZ      NOT NULL,
    updated_at    TIMESTAMPTZ      NOT NULL,
    is_deleted    BOOLEAN          NOT NULL DEFAULT FALSE,
    deleted_at    TIMESTAMPTZ      NULL,
    CONSTRAINT fk_article_interest FOREIGN KEY (interest_id)
        REFERENCES interests (id)
        ON DELETE CASCADE ON UPDATE CASCADE
);

-- ==============================
-- ARTICLE_VIEWS
-- ==============================
CREATE TABLE article_views
(
    id         UUID PRIMARY KEY,
    article_id UUID        NOT NULL,
    user_id    UUID        NOT NULL,
    create_at  TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_view_article FOREIGN KEY (article_id)
        REFERENCES articles (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_view_user FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE ON UPDATE CASCADE
);

-- ==============================
-- COMMENTS
-- ==============================
CREATE TABLE comments
(
    id         UUID PRIMARY KEY,
    article_id UUID         NOT NULL,
    user_id    UUID         NOT NULL,
    content    VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL,
    is_deleted BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMPTZ  NULL,
    like_count BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT fk_comment_article FOREIGN KEY (article_id)
        REFERENCES articles (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_comment_user FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE ON UPDATE CASCADE
);

-- ==============================
-- COMMENT_LIKE
-- ==============================
CREATE TABLE comment_likes
(
    id         UUID PRIMARY KEY,
    comment_id UUID        NOT NULL,
    user_id    UUID        NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_like_comment FOREIGN KEY (comment_id)
        REFERENCES comments (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_like_user FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE ON UPDATE CASCADE
);

-- ==============================
-- NOTIFICATIONS
-- ==============================
-- Enum 타입에 맞춰서 변경하면 됨
CREATE TYPE resource_type AS ENUM ('ARTICLE', 'COMMENT', 'LIKE', 'SUBSCRIPTION');

CREATE TABLE notifications
(
    id            UUID PRIMARY KEY,
    user_id       UUID          NOT NULL,
    content       VARCHAR(255)  NOT NULL,
    confirmed     BOOLEAN       NOT NULL,
    resource_type resource_type NOT NULL,
    resource_id   UUID          NULL,
    created_at    TIMESTAMPTZ   NOT NULL,
    updated_at    TIMESTAMPTZ   NULL,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE ON UPDATE CASCADE
);
