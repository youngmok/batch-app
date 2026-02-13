CREATE TABLE IF NOT EXISTS financial_data (
    id              BIGSERIAL PRIMARY KEY,
    data_source     VARCHAR(20)  NOT NULL,
    data_category   VARCHAR(30)  NOT NULL,
    market_region   VARCHAR(20)  NOT NULL,
    item_name       VARCHAR(200) NOT NULL,
    current_value   VARCHAR(50),
    change_value    VARCHAR(50),
    change_percent  VARCHAR(20),
    extra_info      TEXT,
    scraped_date    DATE         NOT NULL DEFAULT CURRENT_DATE,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    UNIQUE (data_source, data_category, item_name, scraped_date)
);

CREATE TABLE IF NOT EXISTS ai_summary (
    id              BIGSERIAL PRIMARY KEY,
    summary_date    DATE         NOT NULL,
    summary_text    TEXT         NOT NULL,
    model_used      VARCHAR(50)  NOT NULL,
    token_used      INT,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    UNIQUE (summary_date)
);

CREATE TABLE IF NOT EXISTS kakao_token (
    id              BIGSERIAL PRIMARY KEY,
    access_token    TEXT         NOT NULL,
    refresh_token   TEXT         NOT NULL,
    token_type      VARCHAR(20)  NOT NULL DEFAULT 'bearer',
    expires_at      TIMESTAMP    NOT NULL,
    refresh_expires_at TIMESTAMP NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_financial_data_date ON financial_data (scraped_date);
CREATE INDEX IF NOT EXISTS idx_financial_data_source_date ON financial_data (data_source, scraped_date);
CREATE INDEX IF NOT EXISTS idx_ai_summary_date ON ai_summary (summary_date);
