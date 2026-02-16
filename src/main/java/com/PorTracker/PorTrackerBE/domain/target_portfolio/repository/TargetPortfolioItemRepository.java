package com.PorTracker.PorTrackerBE.domain.target_portfolio.repository;

import com.PorTracker.PorTrackerBE.domain.target_portfolio.entity.TargetPortfolioItemRecord;
import com.PorTracker.PorTrackerBE.global.constant.SqliteSchema;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TargetPortfolioItemRepository {

        private static final String assetPublicIdName = "asset_public_id";

        private final RowMapper<TargetPortfolioItemRecord> itemMapper =
                        (rs, rowNum) -> TargetPortfolioItemRecord.builder()
                                        .assetId(rs.getLong(SqliteSchema.COL_ASSET_ID))
                                        .assetPublicId(rs.getString(assetPublicIdName))
                                        .snapshotId(rs.getLong(SqliteSchema.COL_SNAPSHOT_ID))
                                        .currentRatioBp(rs
                                                        .getLong(SqliteSchema.COL_CURRENT_RATIO_BP))
                                        // .ratioDeltaBp(rs.getLong(SqliteSchema.COL_RATIO_DELTA_BP))
                                        .build();

        public void save(JdbcTemplate jdbcTemplate, Long snapshotId, Long assetId,
                        Long currentRatioBp
        // Long ratioDeltaBp
        ) {
                String sql = String.format("INSERT INTO %s (%s, %s, %s) VALUES (?, ?, ?)",
                                SqliteSchema.TABLE_TARGET_PORTFOLIO_ITEM, SqliteSchema.COL_ASSET_ID,
                                SqliteSchema.COL_SNAPSHOT_ID, SqliteSchema.COL_CURRENT_RATIO_BP
                // SqliteSchema.COL_RATIO_DELTA_BP
                );

                jdbcTemplate.update(sql, ps -> {
                        ps.setLong(1, assetId);
                        ps.setLong(2, snapshotId);
                        ps.setLong(3, currentRatioBp);
                        // ps.setLong(4, ratioDeltaBp);
                });
        }

        /**
         * 특정 포트폴리오의 최신 스냅샷에 속한 아이템들을 JOIN으로 한번에 조회. target_portfolio_item →
         * target_portfolio_snapshot → target_portfolio 순으로 JOIN하고, asset 테이블도 JOIN하여 asset의
         * public_id를 함께 가져옴.
         */
        public List<TargetPortfolioItemRecord> findItemsByLatestSnapshot(JdbcTemplate jdbcTemplate,
                        Long portfolioId) {
                String sql = String.format("SELECT tpi.%s, tpi.%s,  tpi.%s, a.%s as %s"
                                + " FROM %s tpi" + " JOIN %s a ON a.%s = tpi.%s"
                                + " WHERE tpi.%s = (" + "   SELECT tps.%s FROM %s tps"
                                + "   WHERE tps.%s = ?" + "   ORDER BY tps.%s DESC LIMIT 1" + " )",
                                // SELECT
                                SqliteSchema.COL_ASSET_ID, SqliteSchema.COL_SNAPSHOT_ID,
                                SqliteSchema.COL_CURRENT_RATIO_BP,
                                // SqliteSchema.COL_RATIO_DELTA_BP,
                                SqliteSchema.COL_PUBLIC_ID, assetPublicIdName,
                                // FROM target_portfolio_item
                                SqliteSchema.TABLE_TARGET_PORTFOLIO_ITEM,
                                // JOIN asset
                                SqliteSchema.TABLE_ASSET, SqliteSchema.COL_ID,
                                SqliteSchema.COL_ASSET_ID,
                                // WHERE snapshot_id = (서브쿼리: 해당 portfolio의 최신 snapshot id)
                                SqliteSchema.COL_SNAPSHOT_ID, SqliteSchema.COL_ID,
                                SqliteSchema.TABLE_TARGET_PORTFOLIO_SNAPSHOT,
                                SqliteSchema.COL_PORTFOLIO_ID, SqliteSchema.COL_ID);

                return jdbcTemplate.query(sql, ps -> ps.setLong(1, portfolioId), itemMapper);
        }

        /** 특정 스냅샷 id로 아이템들 조회 (이력 조회 등에 사용) */
        public List<TargetPortfolioItemRecord> findItemsBySnapshotId(JdbcTemplate jdbcTemplate,
                        Long snapshotId) {
                String sql = String.format(
                                "SELECT tpi.%s, tpi.%s, tpi.%s,  a.%s as %s" + " FROM %s tpi"
                                                + " JOIN %s a ON a.%s = tpi.%s"
                                                + " WHERE tpi.%s = ?",
                                SqliteSchema.COL_ASSET_ID, SqliteSchema.COL_SNAPSHOT_ID,
                                SqliteSchema.COL_CURRENT_RATIO_BP,
                                // SqliteSchema.COL_RATIO_DELTA_BP,
                                SqliteSchema.COL_PUBLIC_ID, assetPublicIdName,
                                SqliteSchema.TABLE_TARGET_PORTFOLIO_ITEM, SqliteSchema.TABLE_ASSET,
                                SqliteSchema.COL_ID, SqliteSchema.COL_ASSET_ID,
                                SqliteSchema.COL_SNAPSHOT_ID);

                return jdbcTemplate.query(sql, ps -> ps.setLong(1, snapshotId), itemMapper);
        }

        /** 여러 포트폴리오의 최신 스냅샷 아이템들을 한 번에 조회하여, 포트폴리오 ID별로 그룹화하여 반환. N+1 문제 해결을 위한 핵심 메서드. */
        public java.util.Map<Long, List<TargetPortfolioItemRecord>> findLatestItemsByPortfolioIds(
                        JdbcTemplate jdbcTemplate, List<Long> portfolioIds) {
                if (portfolioIds == null || portfolioIds.isEmpty()) {
                        return java.util.Collections.emptyMap();
                }

                // IN 절 동적 생성
                String inSql = String.join(",",
                                java.util.Collections.nCopies(portfolioIds.size(), "?"));

                String sql = String.format(
                                "SELECT tpi.%s, tpi.%s, tpi.%s, a.%s as %s, tps.%s" + " FROM %s tpi"
                                                + " JOIN %s tps ON tpi.%s = tps.%s"
                                                + " JOIN %s a ON a.%s = tpi.%s"
                                                + " WHERE tps.%s IN (" + "   SELECT MAX(id) FROM %s"
                                                + "   WHERE %s IN (%s)" + "   GROUP BY %s" + " )",
                                // SELECT
                                SqliteSchema.COL_ASSET_ID, SqliteSchema.COL_SNAPSHOT_ID,
                                SqliteSchema.COL_CURRENT_RATIO_BP
                                // , SqliteSchema.COL_RATIO_DELTA_BP
                                , SqliteSchema.COL_PUBLIC_ID, assetPublicIdName,
                                SqliteSchema.COL_PORTFOLIO_ID, // 그룹핑을
                                // 위해
                                // 추가
                                // 조회
                                // FROM & JOIN
                                SqliteSchema.TABLE_TARGET_PORTFOLIO_ITEM,
                                SqliteSchema.TABLE_TARGET_PORTFOLIO_SNAPSHOT,
                                SqliteSchema.COL_SNAPSHOT_ID, SqliteSchema.COL_ID,
                                SqliteSchema.TABLE_ASSET, SqliteSchema.COL_ID,
                                SqliteSchema.COL_ASSET_ID,
                                // WHERE IN Subquery
                                SqliteSchema.COL_ID, SqliteSchema.TABLE_TARGET_PORTFOLIO_SNAPSHOT,
                                SqliteSchema.COL_PORTFOLIO_ID, inSql,
                                SqliteSchema.COL_PORTFOLIO_ID);

                // 쿼리 실행 및 매핑
                return jdbcTemplate.query(sql, (java.sql.PreparedStatement ps) -> {
                        for (int i = 0; i < portfolioIds.size(); i++) {
                                ps.setLong(i + 1, portfolioIds.get(i));
                        }
                }, (java.sql.ResultSet rs) -> {
                        java.util.Map<Long, List<TargetPortfolioItemRecord>> map =
                                        new java.util.HashMap<>();
                        while (rs.next()) {
                                Long pId = rs.getLong(SqliteSchema.COL_PORTFOLIO_ID);
                                TargetPortfolioItemRecord item = TargetPortfolioItemRecord.builder()
                                                .assetId(rs.getLong(SqliteSchema.COL_ASSET_ID))
                                                .assetPublicId(rs.getString(assetPublicIdName))
                                                .snapshotId(rs.getLong(
                                                                SqliteSchema.COL_SNAPSHOT_ID))
                                                .currentRatioBp(rs.getLong(
                                                                SqliteSchema.COL_CURRENT_RATIO_BP))
                                                // .ratioDeltaBp(rs.getLong(
                                                // SqliteSchema.COL_RATIO_DELTA_BP))
                                                .build();

                                map.computeIfAbsent(pId, k -> new java.util.ArrayList<>())
                                                .add(item);
                        }
                        return map;
                });
        }
}
