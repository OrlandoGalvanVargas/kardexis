CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE UNIQUE INDEX ux_refresh_tokens_token_hash ON refresh_tokens (token_hash) WHERE deleted = FALSE;

CREATE INDEX ix_refresh_tokens_user_id ON refresh_tokens (user_id) WHERE deleted = FALSE AND revoked = FALSE;

CREATE INDEX ix_refresh_tokens_expires_at ON refresh_tokens (expires_at) WHERE deleted = FALSE AND revoked = FALSE;