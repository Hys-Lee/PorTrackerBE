-- 그룹별 평균 지출액을 계산하는 뷰 생성
CREATE OR REPLACE VIEW group_averages AS
SELECT 
    year_month,
    category,
    age_group,
    job_type,
    AVG(total_amount) as avg_amount,
    COUNT(hashed_id) as contributor_count
FROM category_stats
GROUP BY year_month, category, age_group, job_type;