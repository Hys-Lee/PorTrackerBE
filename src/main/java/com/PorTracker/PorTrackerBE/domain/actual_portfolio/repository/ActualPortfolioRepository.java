// package com.PorTracker.PorTrackerBE.domain.actual_portfolio.repository;

// import java.util.List;
// import java.util.Map;
// import org.springframework.jdbc.core.JdbcTemplate;
// import org.springframework.stereotype.Repository;
// import com.PorTracker.PorTrackerBE.global.constant.SqliteSchema;
// import lombok.RequiredArgsConstructor;



// @Repository
// @RequiredArgsConstructor
// public class ActualPortfolioRepository {

// // private static final String SELECT_WITH_RUNNING_TOTAL = String.format("select *, "
// // + "sum(%s * %s * %s * 10000 / 10000 / 10000 / 10000 ) over (partition by %s order by %s, %s)
// // as asset_running_sum_bp, "
// // + "sum(%s * %s * %s * 10000 / 10000 / 10000 / 10000) over (order by %s, %s) as
// // total_running_sum_bp "
// // + "from %s " + "where %s > ? " + "order by %s desc, %s desc limit ?",
// // // 자산별 누적
// // SqliteSchema.COL_AMOUNT_BP, SqliteSchema.COL_PRICE_BP,
// // SqliteSchema.COL_EXCHANGE_RATE_BP, SqliteSchema.COL_ASSET_ID, SqliteSchema.COL_DATE,
// // SqliteSchema.COL_ID,
// // // 전체 누적
// // SqliteSchema.COL_AMOUNT_BP, SqliteSchema.COL_PRICE_BP,
// // SqliteSchema.COL_EXCHANGE_RATE_BP, SqliteSchema.COL_DATE, SqliteSchema.COL_ID, //
// // SqliteSchema.TABLE_ACTUAL_PORTFOLIO, SqliteSchema.COL_DATE, SqliteSchema.COL_DATE,
// // SqliteSchema.COL_ID);

// public List<Map<String, Object>> getActualPortfolioAssetValue(JdbcTemplate jdbcTmeplate,
// Long assetId) {
// String sql = """
// with StartingPoint as (
// -- 가장 최근 스냅샷에서 해당 자산의 가치 가져오기
// select
// ps.%s as last_id
// psi.%s as start_value
// from %s ps
// join %s psi on ps.%s = psi.%s
// where psi.%s = ?
// order by ps.%s desc limit 1
// ),
// TransactionsAfter as (
// -- 스냅샷 이후의 거래 내역들
// select
// ap.%s,
// ap.%s,
// ap.%s * ap.%s * ap.%s * 10000 / 10000/ 10000/ 10000 as change_value
// from %s ap
// where ap.%s = ?
// and ap.%s > (select coalesce(last_id, 0) from StartingPoint)
// )

// select
// %s,
// %s,
// change_value,
// -- 누적 합
// (select COALESCE(start_value, 0) from StartingPoint)+
// sum(change_value) over (order by %s, %s) as running_asset_value
// from %s
// order by %s desc, %s desc

// """.formatted(
// // StartingPoint
// SqliteSchema.COL_LAST_COVERED_ACTUAL_PORTFOLIO_ID, SqliteSchema.COL_ASSET_VALUE_BP,
// SqliteSchema.TABLE_ACTUAL_PORTFOLIO_SNAPSHOT,
// SqliteSchema.TABLE_ACTUAL_PORTFOLIO_SNAPSHOT_ITEM, SqliteSchema.COL_ID,
// SqliteSchema.COL_SNAPSHOT_ID, SqliteSchema.COL_ASSET_ID,
// SqliteSchema.COL_SNAPSHOT_DATE,
// // TransactionsAfter
// SqliteSchema.COL_ID, SqliteSchema.COL_DATE, SqliteSchema.COL_AMOUNT_BP,
// SqliteSchema.COL_PRICE_BP, SqliteSchema.COL_EXCHANGE_RATE_BP,
// SqliteSchema.TABLE_ACTUAL_PORTFOLIO, SqliteSchema.COL_ASSET_ID, SqliteSchema.COL_ID,
// // select문
// SqliteSchema.COL_ID, SqliteSchema.COL_DATE, SqliteSchema.COL_DATE,
// SqliteSchema.COL_ID

// );

// return jdbcTmeplate.queryForList(sql)
// }

// }
