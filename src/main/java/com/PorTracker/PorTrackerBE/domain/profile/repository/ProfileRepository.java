package com.PorTracker.PorTrackerBE.domain.profile.repository;

import com.PorTracker.PorTrackerBE.domain.profile.entity.ProfileRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ProfileRepository {

    // Supabase 중앙 DB용 JdbcTemplate 주입
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<ProfileRecord> profileMapper = (rs, rowNum) -> ProfileRecord.builder()
            .id(UUID.fromString(rs.getString("id")))
            .email(rs.getString("email"))
            .role(rs.getString("role"))
            .nickname(rs.getString("nickname"))
            .baseCurrencyId(rs.getLong("base_currency_id"))
            .createdAt(rs.getObject("created_at", OffsetDateTime.class))
            .updatedAt(rs.getObject("updated_at", OffsetDateTime.class))
            .build();

    public Optional<ProfileRecord> findById(UUID id) {
        String sql = "SELECT id, email, role, nickname, base_currency_id, created_at, updated_at FROM public.profile WHERE id = ?";
        return jdbcTemplate.query(sql, ps -> ps.setObject(1, id), profileMapper)
                .stream()
                .findFirst();
    }

    public void updateProfile(UUID id, String nickname, Long baseCurrencyId) {
        String sql = "UPDATE public.profile SET nickname = ?, base_currency_id = ?, updated_at = NOW() WHERE id = ?";
        jdbcTemplate.update(sql, ps -> {
            ps.setString(1, nickname);
            ps.setLong(2, baseCurrencyId);
            ps.setObject(3, id);
        });
    }
}