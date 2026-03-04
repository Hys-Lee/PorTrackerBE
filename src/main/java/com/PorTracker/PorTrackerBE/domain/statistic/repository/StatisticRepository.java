package com.PorTracker.PorTrackerBE.domain.statistic.repository;

import com.PorTracker.PorTrackerBE.domain.statistic.entity.GroupStatisticRecord;
import com.PorTracker.PorTrackerBE.global.constant.CentralSchema;

import java.time.OffsetDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StatisticRepository {

        @Qualifier("centralJdbcTemplate")
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<GroupStatisticRecord> statisticMapper =
            (rs, rowNum) ->
                    GroupStatisticRecord.builder()
                            .id(rs.getLong("id"))
                            .statType(rs.getString("stat_type"))
                            .period(rs.getString("period"))
                            .sampleCount(rs.getInt("sample_count"))
                            .sumAmountBp(rs.getLong("sum_amount_bp"))
                            .lastUpdatedAt(rs.getObject("last_updated_at", OffsetDateTime.class))
                            .build();

    public Optional<GroupStatisticRecord> findGroupStatistic(String statType, String period) {
        String sql =
                "SELECT id, stat_type, period, sample_count, sum_amount_bp, last_updated_at "
                        + "FROM public.group_statistic WHERE stat_type = ? AND period = ?";

        return jdbcTemplate
                .query(
                        sql,
                        ps -> {
                            ps.setString(1, statType);
                            ps.setString(2, period);
                        },
                        statisticMapper)
                .stream()
                .findFirst();
    }


    public boolean isAlreadyContributed(String contributionKey){
        String sql =String.format( "SELECT EXISTS(SELECT 1 FROM public.stat_contribution WHERE %s = ?)", CentralSchema.COL_CONTRIBUTION_KEY);
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, contributionKey));
    }

    public void saveContribution(String contributionKey){
        String sql = String.format( "INSERT INTO public.stat_contribution (%s, %s) VALUES (?, NOW())", CentralSchema.COL_CONTRIBUTION_KEY, CentralSchema.COL_UPDATED_AT);

        jdbcTemplate.update(sql, contributionKey);
    }

    public void updateGlobalStat(String statType, String period, Long amountBp){
        String sql = String.format(
                "INSERT INTO public.group_statistic (%s, %s, %s, %s, %s)"+ 
                " VALUES (?, ?, 1, ?, NOW())"+
                " ON CONFLICT (%s, %s)"+
                " DO UPDATE SET %s = public.group_statistic.%s +1, %s = public.group_statistic.%s + EXCLUDED.%s, %s = NOW()", 
                // insert
                CentralSchema.COL_STAT_TYPE, CentralSchema.COL_PERIOD, CentralSchema.COL_SAMPLE_COUNT, CentralSchema.COL_SUM_AMOUNT_BP, CentralSchema.COL_LAST_UPDATED_AT,

                // on conflict
                CentralSchema.COL_STAT_TYPE, CentralSchema.COL_PERIOD,
                // do update set
                CentralSchema.COL_SAMPLE_COUNT, CentralSchema.COL_SAMPLE_COUNT,
                CentralSchema.COL_SUM_AMOUNT_BP, CentralSchema.COL_SUM_AMOUNT_BP,CentralSchema.COL_SUM_AMOUNT_BP,
                CentralSchema.COL_LAST_UPDATED_AT
        );

        jdbcTemplate.update(sql, statType, period, amountBp);
    }

}
