-- 1. user_profiles 테이블 생성
CREATE TABLE user_profiles (
    user_id uuid PRIMARY KEY, -- auth.users(id) 참조는 Supabase 환경에서만 유효하므로 생략하거나 유지
    age_group text,    -- '20s', '30s' ...
    job_type text,     -- 'student', 'developer' ...
    region text,       -- 'seoul', 'busan' ...
    updated_at timestamp with time zone DEFAULT now()
);

-- 2. category_stats 테이블 생성
CREATE TABLE category_stats (
    id integer PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    hashed_id text NOT NULL,
    year_month text NOT NULL,
    category text NOT NULL,
    total_amount numeric DEFAULT 0, -- 사용자님 SQL의 total_amount 유지
    age_group text,
    job_type text,
    updated_at timestamp with time zone DEFAULT now(),
    
    -- 최종 유니크 제약 조건
    CONSTRAINT unique_stats_entry UNIQUE (hashed_id, year_month, category, age_group, job_type)
);


-- -- 1. category_stats 테이블 생성 (모든 컬럼과 제약조건을 한 번에 정의)
-- CREATE TABLE category_stats (
--     id integer PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
--     hashed_id text NOT NULL,                -- user_id 대신 해시값 사용
--     year_month text NOT NULL,               -- '2026-02'
--     category text NOT NULL,                 -- '식비', '교통비' 등
--     amount bigint DEFAULT 0,                -- Java Schema의 AMOUNT 상수에 맞춰 amount로 명명 (기존 total_amount)
--     age_group text,                         -- 나이 그룹
--     job_type text,                          -- 직업 유형
--     updated_at timestamp with time zone DEFAULT now(),
    
--     -- 최종 유니크 제약 조건 (5개 컬럼 조합)
--     CONSTRAINT unique_stats_entry UNIQUE (hashed_id, year_month, category, age_group, job_type)
-- );
-- ------------------

-- CREATE TABLE user_profiles (
--     user_id uuid PRIMARY KEY REFERENCES auth.users(id),
--     age_group text,    -- '20s', '30s' ...
--     job_type text,     -- 'student', 'developer' ...
--     region text,       -- 'seoul', 'busan' ...
--     updated_at timestamp with time zone DEFAULT now()
-- );


-- -------------------------
-- -- 그룹별(연령대, 직업, 카테고리, 월) 평균 지출액을 계산하는 뷰 생성
-- CREATE OR REPLACE VIEW group_averages AS
-- SELECT 
--     year_month,
--     category,
--     age_group,
--     job_type,
--     AVG(total_amount) as avg_amount,
--     COUNT(hashed_id) as contributor_count
-- FROM category_stats
-- GROUP BY year_month, category, age_group, job_type;