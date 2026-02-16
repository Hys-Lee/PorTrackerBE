package com.PorTracker.PorTrackerBE.domain.asset.repository;

import com.PorTracker.PorTrackerBE.domain.asset.entity.AssetRecord;
import com.PorTracker.PorTrackerBE.global.constant.SqliteSchema;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AssetRepository {
        private static String sqlCurrencyPublicIdName = "currency_type_public_id";
        private static String sqlAssetTypePublicIdName = "asset_type_public_id";

        private static String BASE_SELECT_SQL = String.format(
                        "SELECT a.%s, a.%s, a.%s, a.%s, a.%s, a.%s, a.%s, a.%s, a.%s, c.%s as %s, at.%s as %s"
                                        + " FROM %s a JOIN %s c ON a.%s=c.%s JOIN %s at ON a.%s=at.%s",
                        // select
                        SqliteSchema.COL_ID, SqliteSchema.COL_PUBLIC_ID,
                        SqliteSchema.COL_CREATED_AT, SqliteSchema.COL_UPDATED_AT,
                        SqliteSchema.COL_DELETED_AT, SqliteSchema.COL_NAME,
                        SqliteSchema.COL_DESCRIPTION, SqliteSchema.COL_CURRENCY_ID,
                        SqliteSchema.COL_TYPE_ID, SqliteSchema.COL_PUBLIC_ID,
                        sqlCurrencyPublicIdName, SqliteSchema.COL_PUBLIC_ID,
                        sqlAssetTypePublicIdName,

                        // from
                        SqliteSchema.TABLE_ASSET, SqliteSchema.TABLE_CURRENCY_TYPE,
                        SqliteSchema.COL_CURRENCY_ID, SqliteSchema.COL_ID,
                        SqliteSchema.TABLE_ASSET_TYPE, SqliteSchema.COL_TYPE_ID,
                        SqliteSchema.COL_ID);

        public List<AssetRecord> findAll(JdbcTemplate jdbcTemplate) {
                return jdbcTemplate.query(BASE_SELECT_SQL, assetMapper);
        }

        public Optional<AssetRecord> findByPublicId(JdbcTemplate jdbcTemplate, String publicId) {
                String sql = BASE_SELECT_SQL + " WHERE a.public_id=?";
                return jdbcTemplate.query(sql, ps -> ps.setString(1, publicId), assetMapper)
                                .stream().findFirst();
        }

        private final RowMapper<AssetRecord> assetMapper = (rs, rowNum) -> AssetRecord.builder()
                        .id(rs.getLong(SqliteSchema.COL_ID))
                        .publicId(rs.getString(SqliteSchema.COL_PUBLIC_ID))
                        .createdAt(rs.getString(SqliteSchema.COL_CREATED_AT))
                        .updatedAt(rs.getString(SqliteSchema.COL_UPDATED_AT))
                        .deletedAt(rs.getString(SqliteSchema.COL_DELETED_AT))
                        .name(rs.getString(SqliteSchema.COL_NAME))
                        .description(rs.getString(SqliteSchema.COL_DESCRIPTION))
                        .currencyId(rs.getLong(SqliteSchema.COL_CURRENCY_ID))
                        .typeId(rs.getLong(SqliteSchema.COL_TYPE_ID))
                        .typePublicId(rs.getString(sqlAssetTypePublicIdName))
                        .currencyPublicId(rs.getString(sqlCurrencyPublicIdName)).build();

        public void updateByPublicId(JdbcTemplate jdbcTemplate, String publicId, String name,
                        String description, Long currencyId, Long typeId) {
                String sql = String.format(
                                "UPDATE %s SET %s = ?, %s = ?, %s = ?, %s = ?, %s = datetime('now') WHERE %s = ? AND %s IS NULL",
                                SqliteSchema.TABLE_ASSET, SqliteSchema.COL_NAME,
                                SqliteSchema.COL_DESCRIPTION, SqliteSchema.COL_CURRENCY_ID,
                                SqliteSchema.COL_TYPE_ID, SqliteSchema.COL_UPDATED_AT,
                                SqliteSchema.COL_PUBLIC_ID, SqliteSchema.COL_DELETED_AT);

                jdbcTemplate.update(sql, ps -> {
                        ps.setString(1, name);
                        ps.setString(2, description);
                        ps.setLong(3, currencyId);
                        ps.setLong(4, typeId);
                        ps.setString(5, publicId);
                });
        }

        public void deleteByPublicId(JdbcTemplate jdbcTemplate, String publicId) {
                String sql = String.format(
                                "UPDATE %s SET %s = datetime('now') WHERE %s = ? AND %s IS NULL",
                                SqliteSchema.TABLE_ASSET, SqliteSchema.COL_DELETED_AT,
                                SqliteSchema.COL_PUBLIC_ID, SqliteSchema.COL_DELETED_AT);

                jdbcTemplate.update(sql, ps -> ps.setString(1, publicId));
        }
}
