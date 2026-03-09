package com.PorTracker.PorTrackerBE.domain.actual_portfolio.dto;

import com.PorTracker.PorTrackerBE.domain.actual_portfolio.entity.TransactionType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ActualPortfolioSearchRequest {
        private String assetId;
        private String currencyId;
        private TransactionType transactionType;
        private String startDate;
        private String endDate;
        private Integer limit=5;
        private Integer offset = 0;
}
