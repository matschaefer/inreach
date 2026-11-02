CREATE TABLE companies (id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, name TEXT NOT NULL UNIQUE);
CREATE TABLE career_sources (id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, company_id BIGINT REFERENCES companies(id), provider TEXT NOT NULL, board_token TEXT NOT NULL, UNIQUE(provider,board_token));
INSERT INTO companies(name) SELECT DISTINCT board_token FROM crawled_jobs;
INSERT INTO career_sources(company_id,provider,board_token) SELECT DISTINCT c.id,j.provider,j.board_token FROM crawled_jobs j JOIN companies c ON c.name=j.board_token;
ALTER TABLE crawl_imports ADD COLUMN complete BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE crawl_imports ADD COLUMN skipped_count INTEGER NOT NULL DEFAULT 0;
CREATE TABLE job_status_history (id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, job_id BIGINT NOT NULL REFERENCES crawled_jobs(id), old_status TEXT NOT NULL, new_status TEXT NOT NULL, changed_at TIMESTAMPTZ NOT NULL DEFAULT now(), reason TEXT NOT NULL);
CREATE TABLE import_errors (id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, provider TEXT NOT NULL, board_token TEXT NOT NULL, code TEXT NOT NULL, created_at TIMESTAMPTZ NOT NULL DEFAULT now());
