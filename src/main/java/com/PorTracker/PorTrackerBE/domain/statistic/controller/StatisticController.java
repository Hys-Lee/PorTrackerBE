package com.PorTracker.PorTrackerBE.domain.statistic.controller;

import com.PorTracker.PorTrackerBE.domain.statistic.dto.GroupStatisticResponse;
import com.PorTracker.PorTrackerBE.domain.statistic.entity.GroupStatisticRecord;
import com.PorTracker.PorTrackerBE.domain.statistic.service.StatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/statistic")
public class StatisticController {

    private final StatisticService statisticService;

    @GetMapping("/group")
    public ResponseEntity<GroupStatisticResponse> getGroupStatistic(
            @RequestParam String statType, @RequestParam String period) {

        GroupStatisticRecord record = statisticService.getGroupStatistic(statType, period);
        return ResponseEntity.ok(GroupStatisticResponse.from(record));
    }
}
