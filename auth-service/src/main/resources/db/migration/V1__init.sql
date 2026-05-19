CREATE SCHEMA IF NOT EXISTS auth;

CREATE TABLE IF NOT EXISTS auth."user" (
    id          UUID PRIMARY KEY,

    email       VARCHAR(50) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(20) DEFAULT 'CUSTOMER',

    first_name  VARCHAR(100) NOT NULL,
    last_name   VARCHAR(100) NOT NULL,

    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()

);

CREATE TABLE IF NOT EXISTS auth.refresh_token (
    id          UUID PRIMARY KEY,

    user_id     UUID NOT NULL,
    token       UUID NOT NULL,
    revoked     BOOLEAN NOT NULL DEFAULT false,

    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at  TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_refresh_token_user_id FOREIGN KEY (user_id)
        REFERENCES auth."user" (id)
);