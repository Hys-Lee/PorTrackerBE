package com.PorTracker.PorTrackerBE.global.constant;

public class SqliteSchema {
    // 테이블
    public static final String TABLE_CURRENCY_TYPE = "currency_type";
    public static final String TABLE_ASSET_TYPE = "asset_type";
    public static final String TABLE_ASSET = "asset";
    public static final String TABLE_ACTUAL_PORTFOLIO = "actual_portfolio";
    public static final String TABLE_TARGET_PORTFOLIO = "target_portfolio";
    public static final String TABLE_TARGET_PORTFOLIO_SNAPSHOT = "target_portfolio_snapshot";
    public static final String TABLE_TARGET_PORTFOLIO_ITEM = "target_portfolio_item";
    public static final String TABLE_MEMO = "memo";
    public static final String TABLE_TAG = "tag";
    public static final String TABLE_MEMO_TAG = "memo_tag";
    public static final String TABLE_ACTUAL_PORTFOLIO_SNAPSHOT = "actual_portfolio_snapshot";
    public static final String TABLE_ACTUAL_PORTFOLIO_SNAPSHOT_ITEM =
            "actual_portfolio_snapshot_item";

    // 컬럼
    public static final String COL_ID = "id";
    public static final String COL_CODE = "code";
    public static final String COL_PUBLIC_ID = "public_id";
    public static final String COL_CREATED_AT = "created_at";
    public static final String COL_UPDATED_AT = "updated_at";
    public static final String COL_DELETED_ID = "deleted_at";
    public static final String COL_NAME = "name";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_CURRENCY_ID = "currency_id";
    public static final String COL_ASSET_ID = "asset_id";
    public static final String COL_TYPE_ID = "type_id";
    public static final String COL_DATE = "date";
    public static final String COL_TRANSACTION_TYPE = "transaction_type";
    public static final String COL_CHANGE_RATIO_BP = "change_ratio_bp";
    public static final String COL_ACCUMULATED_RATIO_BP = "accumulated_ratio_bp";
    public static final String COL_PRICE_BP = "price_bp";
    public static final String COL_AMOUNT_BP = "amount_bp";
    public static final String COL_EXCHANGE_RATE_BP = "exchange_rate_bp";
    public static final String COL_SNAPSHOT_ID = "snapshot_id";
    public static final String COL_IMPORTANCE = "importance";
    public static final String COL_TITLE = "title";
    public static final String COL_CONTENT = "content";
    public static final String COL_EVALUATION = "evaluation";
    public static final String COL_MEMO_TYPE = "memo_type";
    public static final String COL_ACTUAL_ID = "actual_id";
    public static final String COL_TARGET_ID = "target_id";
    public static final String COL_MEMO_ID = "memo_id";
    public static final String COL_TAG_ID = "tag_id";
    public static final String COL_LAST_COVERED_ACTUAL_PORTFOLIO_ID =
            "last_covered_actual_portfolio_id";
    public static final String COL_SNAPSHOT_DATE = "snapshot_date";
    public static final String COL_TOTAL_ASSETS_VALUE_BP = "total_assets_value_bp";
    public static final String COL_ASSET_VALUE_BP = "asset_value_bp";
}
