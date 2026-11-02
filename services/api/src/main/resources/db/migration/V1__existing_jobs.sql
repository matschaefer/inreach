-- Sprint 2: Eine unabhängige, einfache Staging-Tabelle für echte Crawldaten.
-- Keine JobSeek-Produktionstabellen übernehmen.
CREATE TABLE IF NOT EXISTS crawled_jobs (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    provider TEXT NOT NULL,
    board_token TEXT NOT NULL,
    external_job_id TEXT NOT NULL,
    title TEXT NOT NULL,
    source_url TEXT NOT NULL,
    raw_payload JSONB NOT NULL,
    status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active','suspected_closed','closed')),
    first_seen_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_seen_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT crawled_jobs_source_identity UNIQUE (provider, board_token, external_job_id)
);
CREATE INDEX IF NOT EXISTS idx_crawled_jobs_board_status
    ON crawled_jobs (provider, board_token, status);

CREATE TABLE IF NOT EXISTS crawl_imports (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    provider TEXT NOT NULL,
    board_token TEXT NOT NULL,
    imported_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    raw_job_count INTEGER NOT NULL,
    valid_job_count INTEGER NOT NULL,
    duplicate_in_feed_count INTEGER NOT NULL,
    truncated BOOLEAN NOT NULL,
    source_status TEXT
);
