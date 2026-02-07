package com.PorTracker.PorTrackerBE.constant;

public class StatsSchema {
    public static final String TABLE_NAME = "category_stats";
    public static final String VIEW_NAME = "group_averages";

    public static final String TOTAL_AMOUNT = "total_amount";

    // upsert 충돌 판단 위한 컬럼들
    public static final String YEAR_MONTH = "year_month";
    public static final String CATEGORY = "category";
    public static final String HASHED_ID = "hashed_id";
    public static final String AGE_GROUP = "age_group";
    public static final String JOB_TYPE = "job_type";
}
