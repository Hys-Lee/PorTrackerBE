package com.PorTracker.PorTrackerBE.domain.memo.dto;

import com.PorTracker.PorTrackerBE.domain.memo.entity.Evaluation;
import com.PorTracker.PorTrackerBE.domain.memo.entity.Importance;
import com.PorTracker.PorTrackerBE.domain.memo.entity.MemoType;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MemoSearchRequest {
    private List<Importance> importances;
    private List<String> titles;
    private List<Evaluation> evaluations;
    private List<MemoType> memoTypes;
    private List<String> actualIds;
    private List<String> targetIds;
    private String startDate;
    private String endDate;
    private Integer limit = 5;
    private Integer offset = 0;
}
