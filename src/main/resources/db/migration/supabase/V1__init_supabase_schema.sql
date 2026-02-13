-- supabase용 사용자 메타 + 통계 데이터 스키마 V1  -> 참고로 제대로 migration하려면 supabase cli를 사용해야 한다는데, 일단 귀찮아서 저장용으로 여기 둠.

-- 1. 마스터 테이블 (currency_type)
CREATE TABLE IF NOT EXISTS public.currency_type (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    code VARCHAR(3) UNIQUE NOT NULL -- KRW, USD 등
);

-- 초기 데이터 삽입 (profile 생성을 위해 필요)
INSERT INTO public.currency_type (code) VALUES ('KRW'), ('USD') ON CONFLICT DO NOTHING;

-- 2. 사용자 프로필 테이블 (public.profile)
CREATE TABLE IF NOT EXISTS public.profile (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    email TEXT UNIQUE NOT NULL,
    role TEXT NOT NULL DEFAULT 'USER',
    nickname TEXT NOT NULL,
    base_currency_id BIGINT DEFAULT 1, -- 기본값 KRW(1)로 설정 (Trigger 에러 방지)
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    
    FOREIGN KEY (base_currency_id) REFERENCES public.currency_type(id)
);

-- 3. 인증 정보 테이블 (public.credential)
CREATE TABLE IF NOT EXISTS public.credential (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    provider TEXT NOT NULL,
    refresh_token TEXT UNIQUE NOT NULL,
    encrypted_at TIMESTAMPTZ DEFAULT NOW()
);

-- 4. 그룹 통계 테이블 (public.group_statistic)
CREATE TABLE IF NOT EXISTS public.group_statistic (
    id BIGSERIAL PRIMARY KEY,
    stat_type TEXT NOT NULL, -- 예: 'ASSET_RATIO'
    period TEXT NOT NULL,    -- 예: '2024-02'
    sample_count INTEGER NOT NULL DEFAULT 0,
    sum_amount_bp BIGINT DEFAULT 0, -- 오타 수정: sum_amount)bp -> sum_amount_bp
    last_updated_at TIMESTAMPTZ DEFAULT NOW(),
    
    UNIQUE(stat_type, period)
);

-- 5. 통계 기여 확인 테이블 (public.stat_contribution)
CREATE TABLE IF NOT EXISTS public.stat_contribution (
    contribution_key TEXT PRIMARY KEY, -- userId+period+stat_type 의 해시
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 6. View 생성 (평균 계산)
CREATE OR REPLACE VIEW public.v_group_averages AS 
SELECT 
    id, 
    stat_type, 
    period, 
    sample_count, 
    -- 평균 계산: 합계 / 샘플수 (0 나누기 방지)
    CAST(sum_amount_bp AS FLOAT) / NULLIF(sample_count, 0) AS avg_amount_bp,
    last_updated_at
FROM public.group_statistic;

-- 7. Trigger 함수 (신규 유저 생성 시 profile 자동 생성)
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.profile (id, email, nickname, base_currency_id)
    VALUES (
        NEW.id, 
        NEW.email, 
        COALESCE(NEW.raw_user_meta_data ->> 'full_name', 'New User'),
        1 -- 기본 통화 ID (KRW)
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 8. Trigger 등록 (기존 트리거가 있으면 삭제 후 재생성)
DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE PROCEDURE public.handle_new_user();