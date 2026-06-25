-- profile 테이블에 user_db_version 컬럼 추가
ALTER TABLE public.profile ADD COLUMN IF NOT EXISTS user_db_version INTEGER NOT NULL DEFAULT 0;
