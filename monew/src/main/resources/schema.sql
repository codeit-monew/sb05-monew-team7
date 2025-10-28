-- ==============================
-- 0. 확장 기능 활성화 (가장 먼저)
-- ==============================
CREATE EXTENSION IF NOT EXISTS pg_trgm;


-- ==============================
-- 1. DROP (FK 의존성 역순으로 삭제)
-- ==============================

-- 앱 테이블 삭제 (가장 의존성이 많은 것부터)
DROP TABLE IF EXISTS comment_likes;
DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS article_views;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS subscriptions;
DROP TABLE IF EXISTS articles;
DROP TABLE IF EXISTS interests; -- (articles가 참조)
DROP TABLE IF EXISTS users;     -- (여러 테이블이 참조)

-- 배치 테이블 삭제
DROP TABLE IF EXISTS batch_job_execution_params;
DROP TABLE IF EXISTS batch_step_execution_context;
DROP TABLE IF EXISTS batch_job_execution_context;
DROP TABLE IF EXISTS batch_step_execution;
DROP TABLE IF EXISTS batch_job_execution;
DROP TABLE IF EXISTS batch_job_instance;

-- 배치 시퀀스 삭제
DROP SEQUENCE IF EXISTS BATCH_JOB_SEQ;
DROP SEQUENCE IF EXISTS BATCH_JOB_EXECUTION_SEQ;
DROP SEQUENCE IF EXISTS BATCH_STEP_EXECUTION_SEQ;

-- 앱 ENUM 타입 삭제 (테이블이 모두 삭제된 후)
DROP TYPE IF EXISTS user_role;
DROP TYPE IF EXISTS article_resource;
DROP TYPE IF EXISTS resource_type;


-- ==============================
-- 2. CREATE (이제부터 생성)
-- ==============================

-- ==============================
-- USERS
-- ==============================
CREATE TABLE users
(
    id         UUID PRIMARY KEY,
    email      VARCHAR(255) NOT NULL UNIQUE,
    nickname   VARCHAR(20)  NOT NULL,
    password   VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL,
    role       VARCHAR(20)  NOT NULL CHECK (role IN ('ADMIN', 'USER')),
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
    created_at          TIMESTAMPTZ  NOT NULL,
    keywords            TEXT         NOT NULL,
    subscriptions_count BIGINT       NOT NULL DEFAULT 0
);

-- name 컬럼에 트라이그램 인덱스 생성
CREATE INDEX idx_interests_name_trgm
    ON interests USING gin (name gin_trgm_ops);

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
-- CREATE TYPE 삭제
CREATE TABLE articles
(
    id            UUID PRIMARY KEY,
    interest_id   UUID             NOT NULL,
    source        VARCHAR(20)      NOT NULL CHECK (source IN ('NAVER', 'HANKYUNG', 'CHOSUN', 'YEONHAP')),
    source_url    VARCHAR(255)     NOT NULL UNIQUE,
    title         VARCHAR(255)     NOT NULL,
    publish_date  TIMESTAMPTZ      NOT NULL,
    summary       TEXT             NOT NULL,
    comment_count BIGINT           NOT NULL DEFAULT 0,
    view_count    BIGINT           NOT NULL DEFAULT 0,
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
    created_at TIMESTAMPTZ NOT NULL,
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
CREATE TABLE notifications
(
    id            UUID PRIMARY KEY,
    user_id       UUID          NOT NULL,
    content       VARCHAR(255)  NOT NULL,
    confirmed     BOOLEAN       NOT NULL,
    resource_type VARCHAR(20)   NOT NULL CHECK (resource_type IN ('ARTICLE', 'COMMENT', 'LIKE', 'SUBSCRIPTION')),
    resource_id   UUID          NULL,
    created_at    TIMESTAMPTZ   NOT NULL,
    updated_at    TIMESTAMPTZ   NULL,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE ON UPDATE CASCADE
);

-- ==============================
-- SPRING BATCH
-- ==============================
CREATE SEQUENCE BATCH_JOB_SEQ MAXVALUE 9223372036854775807 NO CYCLE;
CREATE SEQUENCE BATCH_JOB_EXECUTION_SEQ MAXVALUE 9223372036854775807 NO CYCLE;
CREATE SEQUENCE BATCH_STEP_EXECUTION_SEQ MAXVALUE 9223372036854775807 NO CYCLE;

CREATE TABLE batch_job_instance (
                                    job_instance_id BIGINT NOT NULL,
                                    version BIGINT,
                                    job_name VARCHAR(100) NOT NULL,
                                    job_key VARCHAR(32) NOT NULL,
                                    CONSTRAINT batch_job_instance_pkey PRIMARY KEY (job_instance_id),
                                    CONSTRAINT job_inst_un UNIQUE (job_name, job_key)
);

CREATE TABLE batch_job_execution (
                                     job_execution_id BIGINT NOT NULL,
                                     version BIGINT,
                                     job_instance_id BIGINT NOT NULL,
                                     create_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,
                                     start_time TIMESTAMP WITHOUT TIME ZONE,
                                     end_time TIMESTAMP WITHOUT TIME ZONE,
                                     status VARCHAR(10),
                                     exit_code VARCHAR(2500),
                                     exit_message VARCHAR(2500),
                                     last_updated TIMESTAMP WITHOUT TIME ZONE,
                                     job_configuration_location VARCHAR(2500) NULL,
                                     CONSTRAINT batch_job_execution_pkey PRIMARY KEY (job_execution_id)
);

CREATE TABLE batch_step_execution (
                                      step_execution_id BIGINT NOT NULL,
                                      version BIGINT NOT NULL,
                                      step_name VARCHAR(100) NOT NULL,
                                      job_execution_id BIGINT NOT NULL,
                                      create_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,
                                      start_time TIMESTAMP WITHOUT TIME ZONE,
                                      end_time TIMESTAMP WITHOUT TIME ZONE,
                                      status VARCHAR(10),
                                      commit_count BIGINT,
                                      read_count BIGINT,
                                      filter_count BIGINT,
                                      write_count BIGINT,
                                      read_skip_count BIGINT,
                                      write_skip_count BIGINT,
                                      process_skip_count BIGINT,
                                      rollback_count BIGINT,
                                      exit_code VARCHAR(2500),
                                      exit_message VARCHAR(2500),
                                      last_updated TIMESTAMP WITHOUT TIME ZONE,
                                      CONSTRAINT batch_step_execution_pkey PRIMARY KEY (step_execution_id)
);

CREATE TABLE batch_job_execution_context (
                                             job_execution_id BIGINT NOT NULL,
                                             short_context VARCHAR(2500) NOT NULL,
                                             serialized_context TEXT,
                                             CONSTRAINT batch_job_execution_context_pkey PRIMARY KEY (job_execution_id)
);

CREATE TABLE batch_step_execution_context (
                                              step_execution_id BIGINT NOT NULL,
                                              short_context VARCHAR(2500) NOT NULL,
                                              serialized_context TEXT,
                                              CONSTRAINT batch_step_execution_context_pkey PRIMARY KEY (step_execution_id)
);

CREATE TABLE batch_job_execution_params (
                                            job_execution_id BIGINT NOT NULL,
                                            parameter_name VARCHAR(100) NOT NULL,
                                            parameter_type VARCHAR(100) NOT NULL,
                                            parameter_value VARCHAR(2500),
                                            identifying CHAR(1) NOT NULL
);


-- 외래 키(Foreign Key) 제약 조건 (마지막에 몰아서)
ALTER TABLE batch_job_execution
    ADD CONSTRAINT job_inst_exec_fk FOREIGN KEY (job_instance_id) REFERENCES batch_job_instance(job_instance_id);

ALTER TABLE batch_job_execution_params
    ADD CONSTRAINT job_exec_params_fk FOREIGN KEY (job_execution_id) REFERENCES batch_job_execution(job_execution_id);

ALTER TABLE batch_step_execution
    ADD CONSTRAINT job_exec_step_fk FOREIGN KEY (job_execution_id) REFERENCES batch_job_execution(job_execution_id);

ALTER TABLE batch_job_execution_context
    ADD CONSTRAINT job_exec_ctx_fk FOREIGN KEY (job_execution_id) REFERENCES batch_job_execution(job_execution_id);

ALTER TABLE batch_step_execution_context
    ADD CONSTRAINT step_exec_ctx_fk FOREIGN KEY (step_execution_id) REFERENCES batch_step_execution(step_execution_id);