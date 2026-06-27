-- profile 테이블에 user_db_version 및 last_sync_offset 컬럼 추가
ALTER TABLE public.profile ADD COLUMN IF NOT EXISTS user_db_version INTEGER NOT NULL DEFAULT 0;
ALTER TABLE public.profile ADD COLUMN IF NOT EXISTS last_sync_offset BIGINT NOT NULL DEFAULT -1;

