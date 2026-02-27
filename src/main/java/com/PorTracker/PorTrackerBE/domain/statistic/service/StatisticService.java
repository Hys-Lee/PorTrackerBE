package com.PorTracker.PorTrackerBE.domain.statistic.service;

import com.PorTracker.PorTrackerBE.domain.statistic.entity.GroupStatisticRecord;
import com.PorTracker.PorTrackerBE.domain.statistic.repository.StatisticRepository;
import com.PorTracker.PorTrackerBE.global.error.BusinessException;
import com.PorTracker.PorTrackerBE.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticService {

    private final StatisticRepository statisticRepository;

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
}
