package com.PorTracker.PorTrackerBE.domain.statistic.repository;

import com.PorTracker.PorTrackerBE.domain.statistic.entity.GroupStatisticRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class StatisticRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<GroupStatisticRecord> statisticMapper = (rs, rowNum) -> GroupStatisticRecord.builder()
            .id(rs.getLong("id"))
            .statType(rs.getString("stat_type"))
            .period(rs.getString("period"))
            .sampleCount(rs.getInt("sample_count"))
            .sumAmountBp(rs.getLong("sum_amount_bp"))
            .lastUpdatedAt(rs.getObject("last_updated_at", OffsetDateTime.class))
            .build();

    public Optional<GroupStatisticRecord> findGroupStatistic(String statType, String period) {
        String sql = "SELECT id, stat_type, period, sample_count, sum_amount_bp, last_updated_at " +
                     "FROM public.group_statistic WHERE stat_type = ? AND period = ?";
                     
        return jdbcTemplate.query(sql, ps -> {
            ps.setString(1, statType);
            ps.setString(2, period);
        }, statisticMapper).stream().findFirst();
    }
}