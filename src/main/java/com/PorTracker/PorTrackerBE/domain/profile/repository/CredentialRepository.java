package com.PorTracker.PorTrackerBE.domain.profile.repository;

import com.PorTracker.PorTrackerBE.domain.profile.entity.CredentialRecord;
import com.PorTracker.PorTrackerBE.global.constant.CentralSchema;
import com.PorTracker.PorTrackerBE.global.constant.SqliteSchema;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CredentialRepository {
    private final JdbcTemplate jdbcTemplate;

    public void saveGoogleToken(UUID userId, String accessToken, String refreshToken) {
        String provider = "'google'";
        String sql =
                String.format(
                        "INSERT INTO public.credential (%s,%s,%s,%s,%s)"
                                + " VALUES (?, %s, ?, ?, NOW())"
                                + " ON CONFLICT (%s)"
                                + " DO UPDATE SET %s=EXCLUDED.%s, %s = COALESCE(EXCLUDED.%s, public.credential.%s), %s = NOW()",
                        // insert
                        SqliteSchema.COL_ID,
                        SqliteSchema.COL_PROVIDER,
                        SqliteSchema.COL_ACCESS_TOKEN,
                        CentralSchema.COL_REFRESH_TOKEN,
                        SqliteSchema.COL_UPDATED_AT,

                        // values
                        provider,
                        // on
                        SqliteSchema.COL_ID,
                        // do
                        SqliteSchema.COL_ACCESS_TOKEN,
                        SqliteSchema.COL_ACCESS_TOKEN,
                        CentralSchema.COL_REFRESH_TOKEN,
                        CentralSchema.COL_REFRESH_TOKEN,
                        CentralSchema.COL_REFRESH_TOKEN,
                        SqliteSchema.COL_UPDATED_AT);

        jdbcTemplate.update(sql, userId, accessToken, refreshToken);
    }

    public String getAccessToken(UUID userId) {
        String sql =
                String.format(
                        "SELECT %s" + " FROM public.credential" + " WHERE %s = ?",
                        // select
                        SqliteSchema.COL_ACCESS_TOKEN,
                        // where
                        SqliteSchema.COL_ID);

        try {
            return jdbcTemplate.queryForObject(sql, String.class, userId);

        } catch (Exception e) {
            return null;
        }
    }

    private final RowMapper<CredentialRecord> credentialMapper =
            (rs, rowNum) ->
                    CredentialRecord.builder()
                            .id(UUID.fromString(rs.getString("id")))
                            .provider(rs.getString("provider"))
                            .accessToken(rs.getString("access_token"))
                            .refreshToken(rs.getString("refresh_token"))
                            .updatedAt(rs.getObject("updated_at", OffsetDateTime.class))
                            .build();

    public Optional<CredentialRecord> findByUserId(UUID userId) {
        String sql =
                String.format(
                        "SELECT %s, %s, %s, %s,%s" + " FROM public.credential" + " WHERE %s=?",
                        // select
                        CentralSchema.COL_ID,
                        CentralSchema.COL_PROVIDER,
                        CentralSchema.COL_ACCESS_TOKEN,
                        CentralSchema.COL_REFRESH_TOKEN,
                        CentralSchema.COL_UPDATED_AT,
                        // where
                        CentralSchema.COL_ID);

        return jdbcTemplate.query(sql, ps -> ps.setObject(1, userId), credentialMapper).stream()
                .findFirst();
    }

    public void updateAccessToken(UUID userId, String newAccessToken) {
        String sql =
                String.format(
                        "UPDATE public.credential SET %s = ?, %s = NOW()" + " WHERE %s = ?",
                        // update
                        CentralSchema.COL_ACCESS_TOKEN,
                        CentralSchema.COL_UPDATED_AT,
                        // where
                        CentralSchema.COL_ID);
        jdbcTemplate.update(sql, newAccessToken, userId);
    }
}
