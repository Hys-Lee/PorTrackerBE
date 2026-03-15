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
    private List<Importance> importance;
    private List<String> title;
    private List<Evaluation> evaluation;
    private List<MemoType> memoType;
    private List<String> actualId;
    private List<String> targetId;
    private String startDate;
    private String endDate;
    private Integer limit = 5;
    private Integer offset = 0;
}
