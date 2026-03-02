-- 1. 기존의 credential 테이블을 삭제합니다. (구조 변경을 위해)
DROP TABLE IF EXISTS public.credential;

-- 2. 사용자님이 주신 최신 규격으로 테이블을 생성합니다.
CREATE TABLE public.credential (
    -- Supabase 내장 유저(auth.users)의 UUID를 참조
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    provider TEXT NOT NULL,           -- 'google'
    access_token TEXT NOT NULL,      -- 구글 드라이브 접근용 토큰 (ya29...)
    refresh_token TEXT,               -- (선택사항) 장기 권한용 리프레시 토큰
    expires_at TIMESTAMPTZ,          -- 토큰 만료 시간
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 3. 데이터 수정 시 updated_at을 자동으로 갱신하는 트리거 (관리용)
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER set_updated_at
    BEFORE UPDATE ON public.credential
    FOR EACH ROW
    EXECUTE PROCEDURE update_updated_at_column();