package com.PorTracker.PorTrackerBE.domain.statistic.service;

import com.PorTracker.PorTrackerBE.domain.statistic.entity.GroupStatisticRecord;
import com.PorTracker.PorTrackerBE.domain.statistic.repository.StatisticRepository;
import com.PorTracker.PorTrackerBE.global.constant.CentralSchema;
import com.PorTracker.PorTrackerBE.global.constant.SqliteSchema;
import com.PorTracker.PorTrackerBE.global.error.BusinessException;
import com.PorTracker.PorTrackerBE.global.error.ErrorCode;
import com.PorTracker.PorTrackerBE.global.infra.sqlite.SqliteDatabaseManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticService {

    private final StatisticRepository statisticRepository;
    private final SqliteDatabaseManager sqliteManager;

    public GroupStatisticRecord getGroupStatistic(String statType, String period) {

        return statisticRepository
                .findGroupStatistic(statType, period)
                .orElseThrow(
                        () -> {
                            log.warn(
                                    "Statistic data not found for type: {}, period: {}",
                                    statType,
                                    period);
                            return new BusinessException(ErrorCode.NO_DATA);
                        });
    }


    @Transactional
    public void contributeTotalAsset(String userId){
        String period = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

        String statType = "TOTAL_ASSET";
        String contributionKey = userId + ":" + period + ":" + statType;

        if(statisticRepository.isAlreadyContributed(contributionKey)){
            log.info("[Stat] User {} already contributed for {}", userId, period);
            return;
        }

        // 유저 총 자산 계산 (usd)
        String calcSql = String.format( 
            "SELECT SUM(%s * %s / 100000)"+
            " FROM %s",
            // select
            SqliteSchema.COL_PRICE_BP, SqliteSchema.COL_AMOUNT_BP,
            // from
            SqliteSchema.TABLE_ACTUAL_PORTFOLIO
        );
        Long totalAssetBp = sqliteManager.getJdbcTemplate(userId).queryForObject(calcSql, Long.class);

        if (totalAssetBp == null || totalAssetBp == 0) return ;
        
        // 기여 기록하기
        statisticRepository.updateGlobalStat(statType, period, totalAssetBp);
        statisticRepository.saveContribution(contributionKey);

        log.info("[Stat] User {} contributed {} Bp to global statistics", userId, totalAssetBp);


    }

}
