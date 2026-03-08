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

package com.PorTracker.PorTrackerBE.domain.actual_portfolio.repository;

import com.PorTracker.PorTrackerBE.domain.actual_portfolio.dto.ActualPortfolioCreateRequest;
import com.PorTracker.PorTrackerBE.domain.actual_portfolio.entity.ActualPortfolioRecord;
import com.PorTracker.PorTrackerBE.global.constant.SqliteSchema;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ActualPortfolioRepository {

    private static final String assetPublicIdName = "asset_public_id";
    private static final String currencyPublicIdName = "currency_type_public_id";

    private static final String CORE_SELECT_SQL =
            String.format(
                    "SELECT ap.%s, ap.%s, ap.%s, ap.%s, ap.%s, ap.%s, ap.%s, ap.%s, ap.%s",
                    SqliteSchema.COL_ID,
                    SqliteSchema.COL_PUBLIC_ID,
                    SqliteSchema.COL_ASSET_ID,
                    SqliteSchema.COL_DATE,
                    SqliteSchema.COL_TRANSACTION_TYPE,
                    SqliteSchema.COL_CURRENCY_ID,
                    SqliteSchema.COL_PRICE_BP,
                    SqliteSchema.COL_AMOUNT_BP,
                    SqliteSchema.COL_EXCHANGE_RATE_BP);

    private static final String BASE_SELECT_SQL =
            String.format(
                    CORE_SELECT_SQL + ", a.%s as %s, c.%s as %s"
                            + " FROM %s ap JOIN %s a ON a.%s=ap.%s JOIN %s c ON c.%s=ap.%s",
                    // select
                    SqliteSchema.COL_PUBLIC_ID,
                    assetPublicIdName,
                    SqliteSchema.COL_PUBLIC_ID,
                    currencyPublicIdName,

                    // from
                    SqliteSchema.TABLE_ACTUAL_PORTFOLIO,
                    SqliteSchema.TABLE_ASSET,
                    SqliteSchema.COL_ID,
                    SqliteSchema.COL_ASSET_ID,
                    SqliteSchema.TABLE_CURRENCY_TYPE,
                    SqliteSchema.COL_ID,
                    SqliteSchema.COL_CURRENCY_ID);

    private static final String BULK_SELECT_SQL =
            String.format(
                    CORE_SELECT_SQL + " FROM %s ap WHERE ap.%s IN (:bulkIds) AND ap.%s IS NULL",
                    SqliteSchema.TABLE_ACTUAL_PORTFOLIO,
                    SqliteSchema.COL_PUBLIC_ID,
                    SqliteSchema.COL_DELETED_AT);

    public List<ActualPortfolioRecord> findAll(JdbcTemplate jdbcTemplate) {
        return jdbcTemplate.query(BASE_SELECT_SQL, actualPortfolioMapper);
    }

    public Optional<ActualPortfolioRecord> findByPublicId(
            JdbcTemplate jdbcTemplate, String publicId) {
        String sql = BASE_SELECT_SQL + " WHERE ap.public_id=?";
        return jdbcTemplate
                .query(sql, ps -> ps.setString(1, publicId), actualPortfolioMapper)
                .stream()
                .findFirst();
    }

    private final RowMapper<ActualPortfolioRecord> actualPortfolioMapper =
            (rs, rowNum) ->
                    ActualPortfolioRecord.builder()
                            .id(rs.getLong(SqliteSchema.COL_ID))
                            .publicId(rs.getString(SqliteSchema.COL_PUBLIC_ID))
                            .assetId(rs.getLong(SqliteSchema.COL_ASSET_ID))
                            .assetPublicId(rs.getString(assetPublicIdName))
                            .date(rs.getString(SqliteSchema.COL_DATE))
                            .transactionType(rs.getString(SqliteSchema.COL_TRANSACTION_TYPE))
                            .currencyId(rs.getLong(SqliteSchema.COL_CURRENCY_ID))
                            .currencyPublicId(rs.getString(currencyPublicIdName))
                            .priceBp(rs.getLong(SqliteSchema.COL_PRICE_BP))
                            .amountBp(rs.getLong(SqliteSchema.COL_AMOUNT_BP))
                            .exchangeRateBp(rs.getLong(SqliteSchema.COL_EXCHANGE_RATE_BP))
                            .build();

    public void save(
            JdbcTemplate jdbcTemplate,
            ActualPortfolioCreateRequest request,
            Long assetId,
            Long currencyId) {
        String sql =
                String.format(
                        "INSERT INTO %s (%s, %s, %s, %s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                        SqliteSchema.TABLE_ACTUAL_PORTFOLIO,
                        SqliteSchema.COL_PUBLIC_ID,
                        SqliteSchema.COL_ASSET_ID,
                        SqliteSchema.COL_DATE,
                        SqliteSchema.COL_TRANSACTION_TYPE,
                        SqliteSchema.COL_CURRENCY_ID,
                        SqliteSchema.COL_PRICE_BP,
                        SqliteSchema.COL_AMOUNT_BP,
                        SqliteSchema.COL_EXCHANGE_RATE_BP);

        String publicId = UUID.randomUUID().toString();

        jdbcTemplate.update(
                sql,
                ps -> {
                    ps.setString(1, publicId);
                    ps.setLong(2, assetId);
                    //     ps.setString(3, request.getDate());
                    ps.setString(3, request.getDate().toString());
                    ps.setString(4, request.getTransactionType().getValue());
                    ps.setLong(5, currencyId);
                    ps.setLong(6, request.getPriceBp());
                    ps.setLong(7, request.getAmountBp());
                    ps.setLong(8, request.getExchangeRateBp());
                });
    }

    public void updateByPublicId(
            JdbcTemplate jdbcTemplate,
            String publicId,
            ActualPortfolioCreateRequest request,
            Long assetId,
            Long currencyId) {
        String sql =
                String.format(
                        "UPDATE %s SET %s=?, %s=?, %s=?, %s=?, %s=?, %s=?, %s=?, %s=datetime('now') WHERE %s=? AND %s IS NULL",
                        SqliteSchema.TABLE_ACTUAL_PORTFOLIO,
                        SqliteSchema.COL_ASSET_ID,
                        SqliteSchema.COL_DATE,
                        SqliteSchema.COL_TRANSACTION_TYPE,
                        SqliteSchema.COL_CURRENCY_ID,
                        SqliteSchema.COL_PRICE_BP,
                        SqliteSchema.COL_AMOUNT_BP,
                        SqliteSchema.COL_EXCHANGE_RATE_BP,
                        SqliteSchema.COL_UPDATED_AT,
                        SqliteSchema.COL_PUBLIC_ID,
                        SqliteSchema.COL_DELETED_AT);

        jdbcTemplate.update(
                sql,
                ps -> {
                    ps.setLong(1, assetId);
                    //     ps.setString(2, request.getDate());
                    ps.setString(2, request.getDate().toString());
                    ps.setString(3, request.getTransactionType().getValue());
                    ps.setLong(4, currencyId);
                    ps.setLong(5, request.getPriceBp());
                    ps.setLong(6, request.getAmountBp());
                    ps.setLong(7, request.getExchangeRateBp());
                    ps.setString(8, publicId);
                });
    }

    public void deleteByPublicId(JdbcTemplate jdbcTemplate, String publicId) {
        String sql =
                String.format(
                        "UPDATE %s SET %s=datetime('now') WHERE %s=? AND %s IS NULL",
                        SqliteSchema.TABLE_ACTUAL_PORTFOLIO,
                        SqliteSchema.COL_DELETED_AT,
                        SqliteSchema.COL_PUBLIC_ID,
                        SqliteSchema.COL_DELETED_AT);

        jdbcTemplate.update(sql, ps -> ps.setString(1, publicId));
    }

    public List<ActualPortfolioRecord> findByPublicIds(
            NamedParameterJdbcTemplate jdbcTemplate, List<String> publicIds) {
        if (publicIds == null || publicIds.isEmpty()) return List.of();

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("bulkIds", publicIds);

        return jdbcTemplate.query(BULK_SELECT_SQL, parameters, actualPortfolioMapper);
    }
}
