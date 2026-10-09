ALTER TABLE user_records ADD COLUMN role VARCHAR(16) NOT NULL DEFAULT 'CUSTOMER';
ALTER TABLE user_records ADD CONSTRAINT ck_user_role CHECK (role IN ('CUSTOMER', 'ADMIN'));

CREATE TABLE access_token_records (
    token_hash VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(128) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_access_token_user FOREIGN KEY (user_id) REFERENCES user_records(id)
) ${tableOptions};
CREATE INDEX ix_access_token_user ON access_token_records(user_id);
CREATE INDEX ix_access_token_expiry ON access_token_records(expires_at);
