-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id          BIGSERIAL PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    email       VARCHAR(100) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(20)  NOT NULL DEFAULT 'USER',
    refresh_token VARCHAR(500),
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 法规表
CREATE TABLE IF NOT EXISTS legal_documents (
    id                BIGSERIAL PRIMARY KEY,
    title             VARCHAR(500) NOT NULL,
    document_number   VARCHAR(100),
    issuing_authority VARCHAR(200),
    publish_date      DATE,
    effective_date    DATE,
    category          VARCHAR(50),
    level             VARCHAR(50),
    status            VARCHAR(20),
    content           TEXT         NOT NULL,
    outline           TEXT,
    created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
